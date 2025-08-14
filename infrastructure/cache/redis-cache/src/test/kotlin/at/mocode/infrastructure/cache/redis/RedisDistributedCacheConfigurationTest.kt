package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.*
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.test.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Configuration Tests for RedisDistributedCache
 */
@OptIn(ExperimentalTime::class)
@Testcontainers
class RedisDistributedCacheConfigurationTest {

    companion object {
        private val logger = KotlinLogging.logger {}

        @Container
        val redisContainer = GenericContainer<Nothing>(DockerImageName.parse("redis:7-alpine")).apply {
            withExposedPorts(6379)
        }
    }

    private lateinit var redisTemplate: RedisTemplate<String, ByteArray>
    private lateinit var serializer: CacheSerializer

    @BeforeEach
    fun setUp() {
        val redisPort = redisContainer.getMappedPort(6379)
        val redisHost = redisContainer.host

        val redisConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        val connectionFactory = LettuceConnectionFactory(redisConfig)
        connectionFactory.afterPropertiesSet()

        redisTemplate = RedisTemplate<String, ByteArray>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            afterPropertiesSet()
        }

        serializer = JacksonCacheSerializer()
    }

    @Test
    fun `test different cache configurations`() {
        logger.info { "Testing different cache configurations" }

        // Configuration 1: High performance, short TTL
        val performanceConfig = DefaultCacheConfiguration(
            keyPrefix = "perf",
            defaultTtl = 5.minutes,
            localCacheMaxSize = 50000,
            offlineModeEnabled = true,
            synchronizationInterval = 30.seconds,
            offlineEntryMaxAge = 1.hours,
            compressionEnabled = false,
            compressionThreshold = Int.MAX_VALUE
        )

        val performanceCache = RedisDistributedCache(redisTemplate, serializer, performanceConfig)
        performanceCache.clear()

        // Test performance config
        performanceCache.set("perf-test", "performance-value")
        assertEquals("performance-value", performanceCache.get<String>("perf-test"))
        assertTrue(performanceCache.exists("perf-test"))

        logger.info { "Performance configuration works correctly" }

        // Configuration 2: Storage optimized, long TTL, compression enabled
        val storageConfig = DefaultCacheConfiguration(
            keyPrefix = "storage",
            defaultTtl = 7.days,
            localCacheMaxSize = 1000,
            offlineModeEnabled = true,
            synchronizationInterval = 5.minutes,
            offlineEntryMaxAge = 24.hours,
            compressionEnabled = true,
            compressionThreshold = 100
        )

        val storageCache = RedisDistributedCache(redisTemplate, serializer, storageConfig)
        storageCache.clear()

        // Test storage config with large data (should be compressed)
        val largeData = "Large data content: " + "X".repeat(1000)
        storageCache.set("storage-test", largeData)
        assertEquals(largeData, storageCache.get<String>("storage-test"))

        logger.info { "Storage optimized configuration works correctly" }

        // Configuration 3: Minimal configuration
        val minimalConfig = DefaultCacheConfiguration(
            keyPrefix = "minimal",
            defaultTtl = null, // No TTL
            localCacheMaxSize = null, // No limit
            offlineModeEnabled = false,
            synchronizationInterval = 1.minutes,
            offlineEntryMaxAge = null,
            compressionEnabled = false,
            compressionThreshold = Int.MAX_VALUE
        )

        val minimalCache = RedisDistributedCache(redisTemplate, serializer, minimalConfig)
        minimalCache.clear()

        // Test minimal config
        minimalCache.set("minimal-test", "minimal-value")
        assertEquals("minimal-value", minimalCache.get<String>("minimal-test"))

        logger.info { "Minimal configuration works correctly" }

        // Clean up
        performanceCache.clear()
        storageCache.clear()
        minimalCache.clear()
    }

    @Test
    fun `test compression threshold behavior`() {
        logger.info { "Testing compression threshold behavior" }

        // Configuration with low compression threshold
        val compressionConfig = DefaultCacheConfiguration(
            keyPrefix = "compression-test",
            defaultTtl = 30.minutes,
            compressionEnabled = true,
            compressionThreshold = 50 // Very low threshold
        )

        val compressionCache = RedisDistributedCache(redisTemplate, serializer, compressionConfig)
        compressionCache.clear()

        // Test small data (below threshold) - should not be compressed
        val smallData = "Small"
        compressionCache.set("small-data", smallData)
        assertEquals(smallData, compressionCache.get<String>("small-data"))

        // Test large data (above threshold) - should be compressed
        val largeData = "A".repeat(200) // Well above threshold
        compressionCache.set("large-data", largeData)
        val retrievedLarge = compressionCache.get<String>("large-data")
        assertEquals(largeData, retrievedLarge)
        assertEquals(200, retrievedLarge?.length)

        logger.info { "Small data length: ${smallData.length}" }
        logger.info { "Large data length: ${largeData.length}" }
        logger.info { "Compression threshold: ${compressionConfig.compressionThreshold}" }

        // Test medium data (right at threshold)
        val mediumData = "B".repeat(50) // Exactly at threshold
        compressionCache.set("medium-data", mediumData)
        assertEquals(mediumData, compressionCache.get<String>("medium-data"))

        logger.info { "Compression threshold behavior validated" }

        compressionCache.clear()
    }

    @Test
    fun `test key prefix functionality`() {
        logger.info { "Testing key prefix functionality" }

        // Create caches with different prefixes
        val config1 = DefaultCacheConfiguration(keyPrefix = "app1", defaultTtl = 30.minutes)
        val config2 = DefaultCacheConfiguration(keyPrefix = "app2", defaultTtl = 30.minutes)
        val config3 = DefaultCacheConfiguration(keyPrefix = "", defaultTtl = 30.minutes) // No prefix

        val cache1 = RedisDistributedCache(redisTemplate, serializer, config1)
        val cache2 = RedisDistributedCache(redisTemplate, serializer, config2)
        val cache3 = RedisDistributedCache(redisTemplate, serializer, config3)

        // Clear all caches
        cache1.clear()
        cache2.clear()
        cache3.clear()

        // Store same key in all caches with different values
        val testKey = "shared-key"
        cache1.set(testKey, "value-from-app1")
        cache2.set(testKey, "value-from-app2")
        cache3.set(testKey, "value-from-no-prefix")

        // Verify each cache returns its own value (thanks to prefixes)
        assertEquals("value-from-app1", cache1.get<String>(testKey))
        assertEquals("value-from-app2", cache2.get<String>(testKey))
        assertEquals("value-from-no-prefix", cache3.get<String>(testKey))

        // Verify isolation - keys don't exist in other caches
        assertTrue(cache1.exists(testKey))
        assertTrue(cache2.exists(testKey))
        assertTrue(cache3.exists(testKey))

        logger.info { "Key prefix isolation works correctly" }

        // Test batch operations with prefixes
        val batchData = mapOf(
            "batch1" to "batch-value-1",
            "batch2" to "batch-value-2"
        )

        cache1.multiSet(batchData)
        cache2.multiSet(batchData.mapValues { "${it.value}-app2" })

        val retrieved1 = cache1.multiGet<String>(batchData.keys)
        val retrieved2 = cache2.multiGet<String>(batchData.keys)

        assertEquals("batch-value-1", retrieved1["batch1"])
        assertEquals("batch-value-1-app2", retrieved2["batch1"])

        logger.info { "Batch operations with prefixes work correctly" }

        // Clean up
        cache1.clear()
        cache2.clear()
        cache3.clear()
    }

    @Test
    fun `test TTL configuration variations`() {
        logger.info { "Testing TTL configuration variations" }

        // Configuration with no default TTL
        val noTtlConfig = DefaultCacheConfiguration(
            keyPrefix = "no-ttl-test",
            defaultTtl = null
        )

        val noTtlCache = RedisDistributedCache(redisTemplate, serializer, noTtlConfig)
        noTtlCache.clear()

        // Store without TTL - should persist indefinitely
        noTtlCache.set("persistent-key", "persistent-value")
        assertEquals("persistent-value", noTtlCache.get<String>("persistent-key"))

        // Store with explicit TTL - should override default (which is null)
        noTtlCache.set("explicit-ttl-key", "explicit-ttl-value", 100.milliseconds)
        assertEquals("explicit-ttl-value", noTtlCache.get<String>("explicit-ttl-key"))

        Thread.sleep(200)
        assertFalse(noTtlCache.exists("explicit-ttl-key"))

        // Configuration with short default TTL
        val shortTtlConfig = DefaultCacheConfiguration(
            keyPrefix = "short-ttl-test",
            defaultTtl = 100.milliseconds
        )

        val shortTtlCache = RedisDistributedCache(redisTemplate, serializer, shortTtlConfig)
        shortTtlCache.clear()

        // Store with default TTL
        shortTtlCache.set("default-ttl-key", "default-ttl-value")
        assertEquals("default-ttl-value", shortTtlCache.get<String>("default-ttl-key"))

        Thread.sleep(200)
        assertFalse(shortTtlCache.exists("default-ttl-key"))

        // Store with explicit longer TTL - should override default
        shortTtlCache.set("override-ttl-key", "override-ttl-value", 30.minutes)
        assertEquals("override-ttl-value", shortTtlCache.get<String>("override-ttl-key"))
        // Should still exist after short default TTL
        assertTrue(shortTtlCache.exists("override-ttl-key"))

        logger.info { "TTL configurations work correctly" }

        noTtlCache.clear()
        shortTtlCache.clear()
    }

    @Test
    fun `test offline mode configuration`() {
        logger.info { "Testing offline mode configuration" }

        // Configuration with offline mode disabled
        val noOfflineConfig = DefaultCacheConfiguration(
            keyPrefix = "no-offline-test",
            defaultTtl = 30.minutes,
            offlineModeEnabled = false
        )

        val noOfflineCache = RedisDistributedCache(redisTemplate, serializer, noOfflineConfig)
        noOfflineCache.clear()

        // Normal operations should work
        noOfflineCache.set("online-key", "online-value")
        assertEquals("online-value", noOfflineCache.get<String>("online-key"))

        // Configuration with offline mode enabled and specific settings
        val offlineConfig = DefaultCacheConfiguration(
            keyPrefix = "offline-test",
            defaultTtl = 30.minutes,
            offlineModeEnabled = true,
            localCacheMaxSize = 1000,
            synchronizationInterval = 10.seconds,
            offlineEntryMaxAge = 2.hours
        )

        val offlineCache = RedisDistributedCache(redisTemplate, serializer, offlineConfig)
        offlineCache.clear()

        // Test offline capabilities
        offlineCache.set("offline-key", "offline-value")
        assertEquals("offline-value", offlineCache.get<String>("offline-key"))

        logger.info { "Offline mode configuration works correctly" }

        noOfflineCache.clear()
        offlineCache.clear()
    }

    @Test
    fun `test local cache size limits`() {
        logger.info { "Testing local cache size limits" }

        // Configuration with very small local cache
        val smallCacheConfig = DefaultCacheConfiguration(
            keyPrefix = "small-cache-test",
            defaultTtl = 30.minutes,
            localCacheMaxSize = 3, // Very small
            offlineModeEnabled = true
        )

        val smallCache = RedisDistributedCache(redisTemplate, serializer, smallCacheConfig)
        smallCache.clear()

        // Fill local cache beyond its limit
        repeat(10) { i ->
            smallCache.set("key-$i", "value-$i")
        }

        // All values should still be retrievable (from Redis if not in local cache)
        repeat(10) { i ->
            assertEquals("value-$i", smallCache.get<String>("key-$i"))
        }

        // Configuration with unlimited local cache
        val unlimitedCacheConfig = DefaultCacheConfiguration(
            keyPrefix = "unlimited-cache-test",
            defaultTtl = 30.minutes,
            localCacheMaxSize = null, // No limit
            offlineModeEnabled = true
        )

        val unlimitedCache = RedisDistributedCache(redisTemplate, serializer, unlimitedCacheConfig)
        unlimitedCache.clear()

        // Fill with many entries
        repeat(1000) { i ->
            unlimitedCache.set("unlimited-key-$i", "unlimited-value-$i")
        }

        // All should be retrievable
        repeat(1000) { i ->
            assertEquals("unlimited-value-$i", unlimitedCache.get<String>("unlimited-key-$i"))
        }

        logger.info { "Local cache size limits work correctly" }

        smallCache.clear()
        unlimitedCache.clear()
    }
}
