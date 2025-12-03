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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.toJavaDuration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
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

    private var lastStateChangeTime = Clock.System.now()

    // Connection state listeners
    private val connectionListeners = CopyOnWriteArrayList<ConnectionStateListener>()

    // Performance metrics tracking
    private var totalOperations = 0L
    private var successfulOperations = 0L
    private var lastMetricsLogTime = Clock.System.now()

    init {
        // Try to connect to Redis
        checkConnection()
    }

    override fun <T : Any> get(key: String, clazz: Class<T>): T? {
        val prefixedKey = addPrefix(key)

        // Try to get from the local cache first
        val localEntry = localCache[prefixedKey] as? CacheEntry<*>
        if (localEntry != null) {
            if (localEntry.isExpired()) {
                localCache.remove(prefixedKey)
                return null
            }
            @Suppress("UNCHECKED_CAST")
            return localEntry.value as T?
        }

        // If not in the local cache, and we're disconnected, return null
        if (!isConnected()) {
            return null
        }

        // Try to get from Redis
        try {
            val bytes = redisTemplate.opsForValue().get(prefixedKey) ?: run {
                trackOperation(true) // successful operation, just no data
                return null
            }
            val entry = serializer.deserializeEntry(bytes, clazz)

            // Store in a local cache
            @Suppress("UNCHECKED_CAST")
            localCache[prefixedKey] = entry as CacheEntry<Any>
            enforceLocalCacheSize()

            trackOperation(true)
            return entry.value
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
            trackOperation(false)
            return null
        } catch (e: Exception) {
            logger.error("Error getting value from Redis for key $prefixedKey", e)
            trackOperation(false)
            return null
        }
    }

    override fun <T : Any> set(key: String, value: T, ttl: Duration?) {
        val prefixedKey = addPrefix(key)
        // KORREKTUR: Logik verwendet jetzt kotlin.time
        val expiresAt = ttl?.let { Clock.System.now() + it } ?: config.defaultTtl?.let { Clock.System.now() + it }

        val entry = CacheEntry(
            key = prefixedKey,
            value = value,
            expiresAt = expiresAt
        )

        @Suppress("UNCHECKED_CAST")
        localCache[prefixedKey] = entry as CacheEntry<Any>
        enforceLocalCacheSize()

        if (!isConnected()) {
            markDirty(key)
            return
        }

        try {
            val bytes = serializer.serializeEntry(entry)
            val effectiveTtl = ttl ?: config.defaultTtl
            if (effectiveTtl != null) {
                // KORREKTUR: Konvertierung zu java.time.Duration für RedisTemplate
                redisTemplate.opsForValue().set(prefixedKey, bytes, effectiveTtl.toJavaDuration())
            } else {
                redisTemplate.opsForValue().set(prefixedKey, bytes)
            }
            trackOperation(true)
        } catch (e: RedisConnectionFailureException) {
            handleConnectionFailure(e)
            markDirty(key)
            trackOperation(false)
        } catch (e: Exception) {
            logger.error("Error setting value in Redis for key $prefixedKey", e)
            markDirty(key)
            trackOperation(false)
        }
    }

    override fun delete(key: String) {
        val prefixedKey = addPrefix(key)

        // Remove from the local cache
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

        // Check the local cache first
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

        // Get from the local cache first
        val prefixedKeys = keys.map { addPrefix(it) }
        val localEntries = prefixedKeys.mapNotNull { key ->
            @Suppress("UNCHECKED_CAST")
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

                            // Store in a local cache
                            @Suppress("UNCHECKED_CAST")
                            localCache[key] = entry as CacheEntry<Any>
                            enforceLocalCacheSize()

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

    // ... (multiSet ebenfalls anpassen)
    override fun <T : Any> multiSet(entries: Map<String, T>, ttl: Duration?) {
        val redisBatch = mutableMapOf<String, ByteArray>()
        val expiresAt = ttl?.let { Clock.System.now() + it } ?: config.defaultTtl?.let { Clock.System.now() + it }

        for ((key, value) in entries) {
            val prefixedKey = addPrefix(key)
            val entry = CacheEntry(
                key = prefixedKey,
                value = value,
                expiresAt = expiresAt
            )
            @Suppress("UNCHECKED_CAST")
            localCache[prefixedKey] = entry as CacheEntry<Any>
            enforceLocalCacheSize()
            redisBatch[prefixedKey] = serializer.serializeEntry(entry)
        }

        if (!isConnected()) {
            entries.keys.forEach { markDirty(it) }
            return
        }

        try {
            redisTemplate.opsForValue().multiSet(redisBatch)
            val effectiveTtl = ttl ?: config.defaultTtl
            if (effectiveTtl != null) {
                redisTemplate.executePipelined { connection ->
                    redisBatch.keys.forEach { key ->
                        connection.keyCommands().pExpire(key.toByteArray(), effectiveTtl.inWholeMilliseconds)
                    }
                    null
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

        // Remove from the local cache
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

                    // Die 'set'-Methode erwartet kein TTL-Argument hier
                    redisTemplate.opsForValue().set(prefixedKey, bytes)

                    // So wird die Dauer zwischen zwei Instants berechnet
                    val ttl = localEntry.expiresAt?.let { it - Clock.System.now() }

                    // 'isNegative' wird zu '< Duration.ZERO'
                    if (ttl != null && ttl > Duration.ZERO) {
                        // KORREKTUR: 'expire' braucht eine java.time.Duration
                        redisTemplate.expire(prefixedKey, ttl.toJavaDuration())
                    }

                    // Update local entry to mark as clean
                    localCache[prefixedKey] = localEntry.markClean()
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
            localCache[prefixedKey] = entry.markDirty()
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

    /**
     * Erzwingt die maximale Größe des lokalen Caches, indem die am längsten nicht
     * mehr modifizierten Einträge entfernt werden.
     */
    private fun enforceLocalCacheSize() {
        val max = config.localCacheMaxSize ?: return
        val overflow = localCache.size - max
        if (overflow <= 0) return
        val toEvict = localCache.entries
            .sortedBy { it.value.lastModifiedAt }
            .take(overflow)
            .map { it.key }
        toEvict.forEach { localCache.remove(it) }
        logger.debug("Evicted ${toEvict.size} entries to enforce local cache size limit $max")
    }

    private fun handleConnectionFailure(e: Exception) {
        logger.warn("Redis connection failure: ${e.message}")
        setConnectionState(ConnectionState.DISCONNECTED)
    }

    private fun setConnectionState(newState: ConnectionState) {
        if (connectionState != newState) {
            val oldState = connectionState
            connectionState = newState
            lastStateChangeTime = Clock.System.now()

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
     * Prüft periodisch die Verbindung zu Redis.
     */
    @Scheduled(fixedDelayString = "\${redis.connection-check-interval:10000}")
    fun checkConnection() {
        try {
            redisTemplate.hasKey("connection-test")
            setConnectionState(ConnectionState.CONNECTED)
        } catch (_: Exception) {
            setConnectionState(ConnectionState.DISCONNECTED)
        }
    }

    /**
     * Bereinigt periodisch abgelaufene Einträge aus dem lokalen Cache.
     */
    @Scheduled(fixedDelayString = "\${redis.local-cache-cleanup-interval:60000}")
    fun cleanupLocalCache() {
        val now = Clock.System.now()
        val expiredKeys = localCache.entries
            .filter { it.value.expiresAt?.let { exp -> exp < now } ?: false }
            .map { it.key }

        expiredKeys.forEach { localCache.remove(it) }

        if (expiredKeys.isNotEmpty()) {
            logger.debug("Removed ${expiredKeys.size} expired entries from local cache")
        }
    }

    /**
     * Synchronisiert periodisch schmutzige Schlüssel, sobald verbunden.
     */
    @Scheduled(fixedDelayString = "\${redis.sync-interval:300000}")
    fun scheduledSync() {
        if (isConnected() && dirtyKeys.isNotEmpty()) {
            synchronize(null)
        }
    }

    //
    // Performance monitoring and optimization methods
    //

    /**
     * Zeichnet eine Cache-Operation für Metriken auf.
     */
    private fun trackOperation(success: Boolean) {
        synchronized(this) {
            totalOperations++
            if (success) successfulOperations++
        }
    }

    /**
     * Liefert aktuelle Performance-Metriken.
     */
    fun getPerformanceMetrics(): Map<String, Any> {
        val now = Clock.System.now()
        val successRate = if (totalOperations > 0) {
            (successfulOperations.toDouble() / totalOperations.toDouble()) * 100.0
        } else 0.0

        return mapOf(
            "totalOperations" to totalOperations,
            "successfulOperations" to successfulOperations,
            "successRate" to String.format("%.1f%%", successRate),
            "dirtyKeysCount" to dirtyKeys.size,
            "localCacheSize" to localCache.size,
            "connectionState" to connectionState.name,
            "lastStateChangeTime" to lastStateChangeTime,
            "uptimeSinceLastMetrics" to (now - lastMetricsLogTime)
        )
    }

    /**
     * Loggt Performance-Metriken (periodisch aufgerufen).
     */
    @Scheduled(fixedDelayString = "\${redis.metrics-log-interval:300000}")
    fun logPerformanceMetrics() {
        val metrics = getPerformanceMetrics()
        logger.info("Cache performance metrics: $metrics")
        lastMetricsLogTime = Clock.System.now()
    }

    /**
     * Cache-Warming-Helfer – lädt angegebene Schlüssel vor.
     */
    fun warmCache(keys: Collection<String>, dataLoader: (String) -> Any?) {
        logger.info("Starting cache warming for ${keys.size} keys")
        var warmedCount = 0
        val startTime = Clock.System.now()

        keys.forEach { key ->
            if (!exists(key)) {
                val data = dataLoader(key)
                if (data != null) {
                    set(key, data, config.defaultTtl)
                    warmedCount++
                }
            }
        }

        val duration = Clock.System.now() - startTime
        logger.info("Cache warming completed: $warmedCount/${keys.size} keys loaded in $duration")
    }

    /**
     * Bulk-Cache-Warming mit Batch-Operationen.
     */
    fun warmCacheBulk(keyDataMap: Map<String, Any>, ttl: Duration? = null) {
        logger.info("Starting bulk cache warming for ${keyDataMap.size} entries")
        val startTime = Clock.System.now()

        multiSet(keyDataMap, ttl ?: config.defaultTtl)

        val duration = Clock.System.now() - startTime
        logger.info("Bulk cache warming completed: ${keyDataMap.size} entries loaded in $duration")
    }

    /**
     * Liefert den Cache-Gesundheitsstatus.
     */
    fun getHealthStatus(): Map<String, Any> {
        val metrics = getPerformanceMetrics()
        val successRate = metrics["successRate"] as String
        val successRateValue = successRate.replace("%", "").toDoubleOrNull() ?: 0.0

        return mapOf(
            "healthy" to (connectionState == ConnectionState.CONNECTED && successRateValue >= 90.0),
            "connectionState" to connectionState.name,
            "successRate" to successRate,
            "localCacheUtilization" to if (config.localCacheMaxSize != null) {
                "${localCache.size}/${config.localCacheMaxSize}"
            } else "${localCache.size}/unlimited",
            "dirtyKeysCount" to dirtyKeys.size,
            "lastHealthCheck" to Clock.System.now()
        )
    }
}
