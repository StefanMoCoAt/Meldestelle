package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.CacheConfiguration
import at.mocode.infrastructure.cache.api.CacheEntry
import at.mocode.infrastructure.cache.api.CacheSerializer
import at.mocode.infrastructure.cache.api.ConnectionState
import at.mocode.infrastructure.cache.api.ConnectionStateListener
import at.mocode.infrastructure.cache.api.ConnectionStatusTracker
import at.mocode.infrastructure.cache.api.DistributedCache
import org.slf4j.LoggerFactory
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Redis implementation of DistributedCache with offline capability.
 */
class RedisDistributedCache(
    private val redisTemplate: RedisTemplate<String, ByteArray>,
    private val serializer: CacheSerializer,
    private val config: CacheConfiguration
) : DistributedCache, ConnectionStatusTracker {

    private val logger = LoggerFactory.getLogger(RedisDistributedCache::class.java)

    // Local cache for offline capability
    private val localCache = ConcurrentHashMap<String, CacheEntry<Any>>()

    // Set of keys that have been modified locally and need to be synchronized
    private val dirtyKeys = ConcurrentHashMap.newKeySet<String>()

    // Connection state
    private var connectionState = ConnectionState.DISCONNECTED
    private var lastStateChangeTime = Instant.now()

    // Connection state listeners
    private val connectionListeners = CopyOnWriteArrayList<ConnectionStateListener>()

    init {
        // Try to connect to Redis
        checkConnection()
    }

    //
    // DistributedCache implementation
    //

    override fun <T : Any> get(key: String, clazz: Class<T>): T? {
        val prefixedKey = addPrefix(key)

        // Try to get from local cache first
        val localEntry = localCache[prefixedKey] as? CacheEntry<T>
        if (localEntry != null) {
            if (localEntry.isExpired()) {
                localCache.remove(prefixedKey)
                return null
            }
            return localEntry.value
        }

        // If not in local cache and we're disconnected, return null
        if (!isConnected()) {
            return null
        }

        // Try to get from Redis
        try {
            val bytes = redisTemplate.opsForValue().get(prefixedKey) ?: return null
            val entry = serializer.deserializeEntry(bytes, clazz)

            // Store in local cache
            localCache[prefixedKey] = entry as CacheEntry<Any>

            return entry.value
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
            return null
        } catch (e: Exception) {
            logger.error("Error getting value from Redis for key $prefixedKey", e)
            return null
        }
    }

    override fun <T : Any> set(key: String, value: T, ttl: Duration?) {
        val prefixedKey = addPrefix(key)
        val expiresAt = ttl?.let { Instant.now().plus(it) } ?: config.defaultTtl?.let { Instant.now().plus(it) }

        val entry = CacheEntry(
            key = prefixedKey,
            value = value,
            expiresAt = expiresAt
        )

        // Store in local cache
        localCache[prefixedKey] = entry as CacheEntry<Any>

        // If we're disconnected, mark as dirty and return
        if (!isConnected()) {
            markDirty(key)
            return
        }

        // Try to store in Redis
        try {
            val bytes = serializer.serializeEntry(entry)
            redisTemplate.opsForValue().set(prefixedKey, bytes)

            if (ttl != null) {
                redisTemplate.expire(prefixedKey, ttl)
            } else if (config.defaultTtl != null) {
                val defaultTtl: Duration? = config.defaultTtl
                redisTemplate.expire(prefixedKey, defaultTtl)
            }
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
            markDirty(key)
        } catch (e: Exception) {
            logger.error("Error setting value in Redis for key $prefixedKey", e)
            markDirty(key)
        }
    }

    override fun delete(key: String) {
        val prefixedKey = addPrefix(key)

        // Remove from local cache
        localCache.remove(prefixedKey)

        // If we're disconnected, mark as dirty and return
        if (!isConnected()) {
            markDirty(key)
            return
        }

        // Try to delete from Redis
        try {
            redisTemplate.delete(prefixedKey)
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
            markDirty(key)
        } catch (e: Exception) {
            logger.error("Error deleting value from Redis for key $prefixedKey", e)
            markDirty(key)
        }
    }

    override fun exists(key: String): Boolean {
        val prefixedKey = addPrefix(key)

        // Check local cache first
        if (localCache.containsKey(prefixedKey)) {
            val entry = localCache[prefixedKey]
            if (entry != null && !entry.isExpired()) {
                return true
            }
            // Remove expired entry
            localCache.remove(prefixedKey)
        }

        // If we're disconnected, return false
        if (!isConnected()) {
            return false
        }

        // Check Redis
        try {
            return redisTemplate.hasKey(prefixedKey) ?: false
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
            return false
        } catch (e: Exception) {
            logger.error("Error checking if key exists in Redis for key $prefixedKey", e)
            return false
        }
    }

    override fun <T : Any> multiGet(keys: Collection<String>, clazz: Class<T>): Map<String, T> {
        val result = mutableMapOf<String, T>()

        // Get from local cache first
        val prefixedKeys = keys.map { addPrefix(it) }
        val localEntries = prefixedKeys.mapNotNull { key ->
            val entry = localCache[key] as? CacheEntry<T>
            if (entry != null && !entry.isExpired()) {
                key to entry.value
            } else {
                null
            }
        }.toMap()

        result.putAll(localEntries.mapKeys { removePrefix(it.key) })

        // If we're disconnected, return local entries
        if (!isConnected()) {
            return result
        }

        // Get missing keys from Redis
        val missingKeys = prefixedKeys.filter { !localEntries.containsKey(it) }
        if (missingKeys.isEmpty()) {
            return result
        }

        try {
            val redisEntries = redisTemplate.opsForValue().multiGet(missingKeys)
            if (redisEntries != null) {
                for (i in missingKeys.indices) {
                    val key = missingKeys[i]
                    val bytes = redisEntries[i]
                    if (bytes != null) {
                        try {
                            val entry = serializer.deserializeEntry(bytes, clazz)

                            // Store in local cache
                            localCache[key] = entry as CacheEntry<Any>

                            // Add to result
                            result[removePrefix(key)] = entry.value
                        } catch (e: Exception) {
                            logger.error("Error deserializing entry for key $key", e)
                        }
                    }
                }
            }
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
        } catch (e: Exception) {
            logger.error("Error getting multiple values from Redis", e)
        }

        return result
    }

    override fun <T : Any> multiSet(entries: Map<String, T>, ttl: Duration?) {
        // Store in local cache and prepare for Redis
        val redisBatch = mutableMapOf<String, ByteArray>()
        val expiresAt = ttl?.let { Instant.now().plus(it) } ?: config.defaultTtl?.let { Instant.now().plus(it) }

        for ((key, value) in entries) {
            val prefixedKey = addPrefix(key)
            val entry = CacheEntry(
                key = prefixedKey,
                value = value,
                expiresAt = expiresAt
            )

            // Store in local cache
            localCache[prefixedKey] = entry as CacheEntry<Any>

            // Prepare for Redis
            redisBatch[prefixedKey] = serializer.serializeEntry(entry)
        }

        // If we're disconnected, mark all as dirty and return
        if (!isConnected()) {
            entries.keys.forEach { markDirty(it) }
            return
        }

        // Try to store in Redis
        try {
            redisTemplate.opsForValue().multiSet(redisBatch)

            if (ttl != null || config.defaultTtl != null) {
                val duration = ttl ?: config.defaultTtl
                if (duration != null) {
                    for (key in redisBatch.keys) {
                        redisTemplate.expire(key, duration)
                    }
                }
            }
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
            entries.keys.forEach { markDirty(it) }
        } catch (e: Exception) {
            logger.error("Error setting multiple values in Redis", e)
            entries.keys.forEach { markDirty(it) }
        }
    }

    override fun multiDelete(keys: Collection<String>) {
        val prefixedKeys = keys.map { addPrefix(it) }

        // Remove from local cache
        prefixedKeys.forEach { localCache.remove(it) }

        // If we're disconnected, mark all as dirty and return
        if (!isConnected()) {
            keys.forEach { markDirty(it) }
            return
        }

        // Try to delete from Redis
        try {
            redisTemplate.delete(prefixedKeys)
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
            keys.forEach { markDirty(it) }
        } catch (e: Exception) {
            logger.error("Error deleting multiple values from Redis", e)
            keys.forEach { markDirty(it) }
        }
    }

    override fun synchronize(keys: Collection<String>?) {
        if (!isConnected()) {
            logger.debug("Cannot synchronize while disconnected")
            return
        }

        val keysToSync = keys ?: getDirtyKeys()
        if (keysToSync.isEmpty()) {
            logger.debug("No keys to synchronize")
            return
        }

        logger.debug("Synchronizing ${keysToSync.size} keys")

        for (key in keysToSync) {
            val prefixedKey = addPrefix(key)
            val localEntry = localCache[prefixedKey]

            if (localEntry == null) {
                // Entry was deleted locally, delete from Redis
                try {
                    redisTemplate.delete(prefixedKey)
                    dirtyKeys.remove(key)
                } catch (e: Exception) {
                    logger.error("Error deleting key $prefixedKey during synchronization", e)
                }
            } else {
                // Entry exists locally, update in Redis
                try {
                    val bytes = serializer.serializeEntry(localEntry)
                    redisTemplate.opsForValue().set(prefixedKey, bytes)

                    val ttl = localEntry.expiresAt?.let { Duration.between(Instant.now(), it) }
                    if (ttl != null && !ttl.isNegative) {
                        redisTemplate.expire(prefixedKey, ttl)
                    }

                    // Update local entry to mark as clean
                    localCache[prefixedKey] = localEntry.markClean() as CacheEntry<Any>
                    dirtyKeys.remove(key)
                } catch (e: Exception) {
                    logger.error("Error updating key $prefixedKey during synchronization", e)
                }
            }
        }
    }

    override fun markDirty(key: String) {
        dirtyKeys.add(key)

        val prefixedKey = addPrefix(key)
        val entry = localCache[prefixedKey]
        if (entry != null) {
            localCache[prefixedKey] = entry.markDirty() as CacheEntry<Any>
        }
    }

    override fun getDirtyKeys(): Collection<String> {
        return dirtyKeys.toList()
    }

    override fun clear() {
        // Clear local cache
        localCache.clear()
        dirtyKeys.clear()

        // If we're disconnected, return
        if (!isConnected()) {
            return
        }

        // Try to clear Redis
        try {
            val keys = redisTemplate.keys("${config.keyPrefix}*")
            if (keys != null && keys.isNotEmpty()) {
                redisTemplate.delete(keys)
            }
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
        } catch (e: Exception) {
            logger.error("Error clearing Redis cache", e)
        }
    }

    //
    // ConnectionStatusTracker implementation
    //

    override fun getConnectionState(): ConnectionState {
        return connectionState
    }

    override fun getLastStateChangeTime(): Instant {
        return lastStateChangeTime
    }

    override fun registerConnectionListener(listener: ConnectionStateListener) {
        connectionListeners.add(listener)
    }

    override fun unregisterConnectionListener(listener: ConnectionStateListener) {
        connectionListeners.remove(listener)
    }

    //
    // Helper methods
    //

    private fun addPrefix(key: String): String {
        return if (config.keyPrefix.isEmpty()) key else "${config.keyPrefix}:$key"
    }

    private fun removePrefix(key: String): String {
        return if (config.keyPrefix.isEmpty()) key else key.substring(config.keyPrefix.length + 1)
    }

    private fun handleConnectionFailure(e: Exception) {
        logger.warn("Redis connection failure: ${e.message}")
        setConnectionState(ConnectionState.DISCONNECTED)
    }

    private fun setConnectionState(newState: ConnectionState) {
        if (connectionState != newState) {
            val oldState = connectionState
            connectionState = newState
            lastStateChangeTime = Instant.now()

            logger.info("Cache connection state changed from $oldState to $newState")

            // Notify listeners
            val timestamp = lastStateChangeTime
            connectionListeners.forEach { listener ->
                try {
                    listener.onConnectionStateChanged(newState, timestamp)
                } catch (e: Exception) {
                    logger.error("Error notifying connection listener", e)
                }
            }

            // If reconnected, synchronize dirty keys
            if (oldState != ConnectionState.CONNECTED && newState == ConnectionState.CONNECTED) {
                synchronize(null)
            }
        }
    }

    /**
     * Periodically check the connection to Redis.
     */
    @Scheduled(fixedDelayString = "\${redis.connection-check-interval:10000}")
    fun checkConnection() {
        try {
            redisTemplate.hasKey("connection-test")
            setConnectionState(ConnectionState.CONNECTED)
        } catch (e: Exception) {
            setConnectionState(ConnectionState.DISCONNECTED)
        }
    }

    /**
     * Periodically clean up expired entries from the local cache.
     */
    @Scheduled(fixedDelayString = "\${redis.local-cache-cleanup-interval:60000}")
    fun cleanupLocalCache() {
        val now = Instant.now()
        val expiredKeys = localCache.entries
            .filter { it.value.expiresAt?.isBefore(now) ?: false }
            .map { it.key }

        expiredKeys.forEach { localCache.remove(it) }

        if (expiredKeys.isNotEmpty()) {
            logger.debug("Removed ${expiredKeys.size} expired entries from local cache")
        }
    }

    /**
     * Periodically synchronize dirty keys when connected.
     */
    @Scheduled(fixedDelayString = "\${redis.sync-interval:300000}")
    fun scheduledSync() {
        if (isConnected() && dirtyKeys.isNotEmpty()) {
            synchronize(null)
        }
    }
}
