package at.mocode.infrastructure.gateway.config

import io.ktor.server.application.*
import io.ktor.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Cache implementation with local caching and Redis integration preparation.
 * This implementation focuses on local caching with proper expiration and statistics.
 * Redis integration can be added in a future update.
 */
class CachingConfig(
    private val redisHost: String = System.getenv("REDIS_HOST") ?: "localhost",
    private val redisPort: Int = System.getenv("REDIS_PORT")?.toIntOrNull() ?: 6379,
    private val defaultTtlMinutes: Long = 10
) {
    private val logger = Logger.getLogger(CachingConfig::class.java.name)

    // Cache entry with expiration time
    private data class CacheEntry<T>(
        val value: T,
        val expiresAt: Long
    )

    // Cache statistics tracking
    private data class CacheStats(
        var hits: Long = 0,
        var misses: Long = 0,
        var puts: Long = 0,
        var evictions: Long = 0
    )

    // Cache maps for different entity types
    private val masterDataCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    private val userCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    private val personCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    private val vereinCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    private val eventCache = ConcurrentHashMap<String, CacheEntry<Any>>()

    // Cache statistics
    private val cacheStats = ConcurrentHashMap<String, CacheStats>()

    // Scheduler for periodic cleanup and stats reporting
    private val scheduler = Executors.newScheduledThreadPool(1) { r ->
        val thread = Thread(r, "cache-maintenance-thread")
        thread.isDaemon = true
        thread
    }

    init {
        // Schedule periodic cleanup of expired entries
        scheduler.scheduleAtFixedRate(
            { cleanupExpiredEntries() },
            10, 10, TimeUnit.MINUTES
        )

        // Schedule periodic stats logging
        scheduler.scheduleAtFixedRate(
            { logCacheStats() },
            5, 30, TimeUnit.MINUTES
        )

        logger.info("CachingConfig initialized with Redis host: $redisHost, port: $redisPort")
    }

    /**
     * Get a value from cache
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(cacheName: String, key: String): T? {
        val stats = cacheStats.computeIfAbsent(cacheName) { CacheStats() }

        // Try local cache
        val localCache = getCacheMap(cacheName)
        val entry = localCache[key]

        if (entry != null) {
            // Check if entry is expired
            if (System.currentTimeMillis() > entry.expiresAt) {
                localCache.remove(key)
                stats.evictions++
                stats.misses++
                return null
            }

            stats.hits++
            return entry.value as T
        }

        stats.misses++
        return null
    }

    /**
     * Put a value in a cache with TTL in minutes
     */
    fun <T> put(cacheName: String, key: String, value: T, ttlMinutes: Long = defaultTtlMinutes) {
        val stats = cacheStats.computeIfAbsent(cacheName) { CacheStats() }
        stats.puts++

        // Store in a local cache
        val expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ttlMinutes)
        val entry = CacheEntry(value as Any, expiresAt)
        getCacheMap(cacheName)[key] = entry
    }

    /**
     * Remove a value from the cache
     */
    fun remove(cacheName: String, key: String) {
        // Remove from the local cache
        getCacheMap(cacheName).remove(key)
    }

    /**
     * Clear a specific cache
     */
    fun clearCache(cacheName: String) {
        // Clear local cache
        getCacheMap(cacheName).clear()
    }

    /**
     * Clear all caches
     */
    fun clearAllCaches() {
        // Clear all local caches
        masterDataCache.clear()
        userCache.clear()
        personCache.clear()
        vereinCache.clear()
        eventCache.clear()
    }

    /**
     * Get the appropriate cache map based on the cache name
     */
    private fun getCacheMap(cacheName: String): ConcurrentHashMap<String, CacheEntry<Any>> {
        return when (cacheName) {
            MASTER_DATA_CACHE -> masterDataCache
            USER_CACHE -> userCache
            PERSON_CACHE -> personCache
            VEREIN_CACHE -> vereinCache
            EVENT_CACHE -> eventCache
            else -> throw IllegalArgumentException("Unknown cache name: $cacheName")
        }
    }

    /**
     * Clean up expired entries from local caches
     */
    private fun cleanupExpiredEntries() {
        val now = System.currentTimeMillis()
        var totalRemoved = 0

        // Clean up each cache
        listOf(masterDataCache, userCache, personCache, vereinCache, eventCache).forEach { cache ->
            val iterator = cache.entries.iterator()
            var removed = 0

            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now > entry.value.expiresAt) {
                    iterator.remove()
                    removed++
                }
            }

            totalRemoved += removed
        }

        if (totalRemoved > 0) {
            logger.info("Cache cleanup completed: removed $totalRemoved expired entries")
        }
    }

    /**
     * Log cache statistics
     */
    private fun logCacheStats() {
        cacheStats.forEach { (cacheName, stats) ->
            val hitRatio = if (stats.hits + stats.misses > 0) {
                stats.hits.toDouble() / (stats.hits + stats.misses)
            } else {
                0.0
            }

            logger.info("Cache stats for $cacheName: hits=${stats.hits}, misses=${stats.misses}, " +
                    "puts=${stats.puts}, evictions=${stats.evictions}, hit-ratio=${String.format("%.2f", hitRatio * 100)}%")
        }
    }

    /**
     * Shutdown the cache manager and release resources
     */
    fun shutdown() {
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler.shutdownNow()
        }

        logger.info("CachingConfig shutdown completed")
    }

    companion object {
        // Cache names for different entities
        const val MASTER_DATA_CACHE = "masterDataCache"
        const val USER_CACHE = "userCache"
        const val PERSON_CACHE = "personCache"
        const val VEREIN_CACHE = "vereinCache"
        const val EVENT_CACHE = "eventCache"

        // List of all cache names
        val CACHE_NAMES = listOf(
            MASTER_DATA_CACHE,
            USER_CACHE,
            PERSON_CACHE,
            VEREIN_CACHE,
            EVENT_CACHE
        )

        // Default TTLs in minutes
        const val MASTER_DATA_TTL = 24 * 60L // 24 hours
        const val USER_TTL = 2 * 60L // 2 hours
        const val PERSON_TTL = 4 * 60L // 4 hours
        const val VEREIN_TTL = 12 * 60L // 12 hours
        const val EVENT_TTL = 6 * 60L // 6 hours

        // AttributeKey for storing in application
        val CACHING_CONFIG_KEY = AttributeKey<CachingConfig>("CachingConfig")
    }
}

/**
 * Extension function to install caching in the application.
 */
fun Application.configureCaching() {
    val redisHost = environment.config.propertyOrNull("redis.host")?.getString()
        ?: System.getenv("REDIS_HOST")
        ?: "localhost"

    val redisPort = environment.config.propertyOrNull("redis.port")?.getString()?.toIntOrNull()
        ?: System.getenv("REDIS_PORT")?.toIntOrNull()
        ?: 6379

    val cachingConfig = CachingConfig(
        redisHost = redisHost,
        redisPort = redisPort
    )

    // Store the caching config in the application attributes
    attributes.put(CachingConfig.CACHING_CONFIG_KEY, cachingConfig)

    // Register shutdown hook
    this.monitor.subscribe(ApplicationStopping) {
        cachingConfig.shutdown()
    }

    // Log cache configuration
    log.info("Cache configuration initialized: Redis host=$redisHost, port=$redisPort")
}

/**
 * Extension function to get the caching config from the application.
 */
fun Application.getCachingConfig(): CachingConfig {
    return attributes[CachingConfig.CACHING_CONFIG_KEY]
}
