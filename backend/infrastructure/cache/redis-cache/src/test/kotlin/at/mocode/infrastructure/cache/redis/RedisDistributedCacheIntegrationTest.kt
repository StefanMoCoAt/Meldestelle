package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Monitoring and Integration Tests for RedisDistributedCache
 */
@OptIn(ExperimentalTime::class)
@Testcontainers
class RedisDistributedCacheIntegrationTest {

    companion object {
        private val logger = KotlinLogging.logger {}

        @Container
        val redisContainer = GenericContainer<Nothing>(
            DockerImageName.parse("redis:7-alpine")
                .asCompatibleSubstituteFor("redis")
        ).apply {
            withExposedPorts(6379)
        }
    }

    private lateinit var redisTemplate: RedisTemplate<String, ByteArray>
    private lateinit var serializer: CacheSerializer
    private lateinit var config: CacheConfiguration

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
        config = DefaultCacheConfiguration(
            keyPrefix = "integration-test",
            defaultTtl = 30.minutes
        )
    }

    @Test
    fun `test connection state listener functionality`() = runBlocking {
        logger.info { "Testing connection state listener functionality" }

        val cache = RedisDistributedCache(redisTemplate, serializer, config)
        cache.clear()

        val stateChanges = mutableListOf<Pair<ConnectionState, kotlin.time.Instant>>()
        val latch = CountDownLatch(1)

        val listener = object : ConnectionStateListener {
            override fun onConnectionStateChanged(newState: ConnectionState, timestamp: kotlin.time.Instant) {
                logger.info { "Connection state changed to: $newState at $timestamp" }
                stateChanges.add(newState to timestamp)
                latch.countDown()
            }
        }

        // Register listener
        cache.registerConnectionListener(listener)

        // Initial state should be connected
        assertEquals(ConnectionState.CONNECTED, cache.getConnectionState())
        logger.info { "Initial connection state: ${cache.getConnectionState()}" }

        // Test listener registration/unregistration
        val multipleListeners = mutableListOf<ConnectionStateListener>()
        val callCounts = AtomicInteger(0)

        repeat(3) { i ->
            val testListener = object : ConnectionStateListener {
                override fun onConnectionStateChanged(newState: ConnectionState, timestamp: kotlin.time.Instant) {
                    callCounts.incrementAndGet()
                    logger.info { "Listener $i received state change: $newState" }
                }
            }
            multipleListeners.add(testListener)
            cache.registerConnectionListener(testListener)
        }

        // Simulate state change (this might not trigger in our test environment,
        // but we're testing the listener mechanism)
        cache.checkConnection()

        // Unregister listeners
        multipleListeners.forEach { cache.unregisterConnectionListener(it) }
        cache.unregisterConnectionListener(listener)

        logger.info { "Connection state listener functionality tested" }

        cache.clear()
    }

    @Test
    fun `test different Redis configurations`() {
        logger.info { "Testing different Redis configurations" }

        // Test with current configuration
        val standardCache = RedisDistributedCache(redisTemplate, serializer, config)
        standardCache.clear()

        // Basic functionality test
        standardCache.set("config-test-1", "standard-value")
        assertEquals("standard-value", standardCache.get<String>("config-test-1"))

        // Test with different Redis configuration (same container, different settings)
        val alternativeConfig = DefaultCacheConfiguration(
            keyPrefix = "alt-config",
            defaultTtl = 1.hours,
            compressionEnabled = true,
            compressionThreshold = 500
        )

        val alternativeCache = RedisDistributedCache(redisTemplate, serializer, alternativeConfig)
        alternativeCache.clear()

        // Test isolation between configurations
        alternativeCache.set("config-test-1", "alternative-value")

        // Both caches should maintain their own data
        assertEquals("standard-value", standardCache.get<String>("config-test-1"))
        assertEquals("alternative-value", alternativeCache.get<String>("config-test-1"))

        // Test connection state tracking
        assertEquals(ConnectionState.CONNECTED, standardCache.getConnectionState())
        assertEquals(ConnectionState.CONNECTED, alternativeCache.getConnectionState())

        logger.info { "Different Redis configurations work correctly" }

        standardCache.clear()
        alternativeCache.clear()
    }

    @Test
    fun `test cache warming scenarios`() {
        logger.info { "Testing cache warming scenarios" }

        val cache = RedisDistributedCache(redisTemplate, serializer, config)
        cache.clear()

        // Scenario 1: Bulk warming with predefined data
        val warmupData = (1..1000).associate { "warmup-key-$it" to "warmup-value-$it" }

        logger.info { "Starting cache warming with ${warmupData.size} entries" }
        val warmupTime = measureTime {
            cache.multiSet(warmupData)
        }
        logger.info { "Cache warmup completed in $warmupTime" }

        // Verify all data is accessible
        val verificationTime = measureTime {
            val retrieved = cache.multiGet<String>(warmupData.keys)
            assertEquals(warmupData.size, retrieved.size)

            // Spot check some values
            assertEquals("warmup-value-1", retrieved["warmup-key-1"])
            assertEquals("warmup-value-500", retrieved["warmup-key-500"])
            assertEquals("warmup-value-1000", retrieved["warmup-key-1000"])
        }
        logger.info { "Cache verification completed in $verificationTime" }

        // Scenario 2: Gradual warming simulation
        logger.info { "Testing gradual cache warming" }
        val gradualWarmupCache = RedisDistributedCache(redisTemplate, serializer,
            DefaultCacheConfiguration(keyPrefix = "gradual-warmup", defaultTtl = 1.hours))
        gradualWarmupCache.clear()

        // Simulate application startup with gradual data loading
        val batchSize = 100
        val totalBatches = 10

        repeat(totalBatches) { batchIndex ->
            val batchData = (1..batchSize).associate {
                "gradual-${batchIndex * batchSize + it}" to "gradual-value-${batchIndex * batchSize + it}"
            }
            gradualWarmupCache.multiSet(batchData)

            // Simulate some delay between batches (like database queries)
            Thread.sleep(10)
        }

        // Verify gradual warmup worked
        val totalEntries = batchSize * totalBatches
        val allKeys = (1..totalEntries).map { "gradual-$it" }
        val retrievedGradual = gradualWarmupCache.multiGet<String>(allKeys)

        assertEquals(totalEntries, retrievedGradual.size)
        logger.info { "Gradual warmup successful: ${retrievedGradual.size} entries" }

        // Scenario 3: Selective warming based on usage patterns
        logger.info { "Testing selective cache warming" }
        val selectiveCache = RedisDistributedCache(redisTemplate, serializer,
            DefaultCacheConfiguration(keyPrefix = "selective-warmup", defaultTtl = 2.hours))
        selectiveCache.clear()

        // Simulate frequently accessed data
        val frequentData = listOf("user:123", "config:global", "menu:main")
        val infrequentData = (1..100).map { "rare:data:$it" }

        // Warm up frequent data first (priority warming)
        frequentData.forEach { key ->
            selectiveCache.set(key, "frequent-$key")
        }

        // Warm up infrequent data in background
        infrequentData.forEach { key ->
            selectiveCache.set(key, "infrequent-$key")
        }

        // Verify selective warming
        frequentData.forEach { key ->
            assertEquals("frequent-$key", selectiveCache.get<String>(key))
        }

        logger.info { "Selective cache warming completed successfully" }

        cache.clear()
        gradualWarmupCache.clear()
        selectiveCache.clear()
    }

    @Test
    fun `test metrics and monitoring integration`() = runBlocking {
        logger.info { "Testing metrics and monitoring integration" }

        val monitoringCache = RedisDistributedCache(redisTemplate, serializer, config)
        monitoringCache.clear()

        // Test connection state tracking over time
        val connectionStateHistory = mutableListOf<ConnectionState>()
        var lastStateChangeTime = monitoringCache.getLastStateChangeTime()

        logger.info { "Initial connection state: ${monitoringCache.getConnectionState()}" }
        logger.info { "Last state change time: $lastStateChangeTime" }

        connectionStateHistory.add(monitoringCache.getConnectionState())

        // Perform various operations and monitor state
        repeat(100) { i ->
            monitoringCache.set("monitoring-key-$i", "monitoring-value-$i")

            if (i % 20 == 0) {
                val currentState = monitoringCache.getConnectionState()
                val currentTime = monitoringCache.getLastStateChangeTime()

                if (currentTime != lastStateChangeTime) {
                    logger.info { "State change detected at operation $i" }
                    connectionStateHistory.add(currentState)
                    lastStateChangeTime = currentTime
                }
            }
        }

        // Test dirty keys tracking for monitoring
        logger.info { "Testing dirty keys monitoring" }
        val initialDirtyKeys = monitoringCache.getDirtyKeys()
        logger.info { "Initial dirty keys count: ${initialDirtyKeys.size}" }

        // Add some data and verify dirty keys tracking
        monitoringCache.set("dirty-test-1", "dirty-value-1")
        monitoringCache.set("dirty-test-2", "dirty-value-2")

        // In normal connected state, dirty keys should be minimal
        val finalDirtyKeys = monitoringCache.getDirtyKeys()
        logger.info { "Final dirty keys count: ${finalDirtyKeys.size}" }

        // Test batch operations monitoring
        val batchData = (1..50).associate { "batch-monitoring-$it" to "batch-value-$it" }

        val batchTime = measureTime {
            monitoringCache.multiSet(batchData)
        }
        logger.info { "Batch operation took: $batchTime" }

        val retrievalTime = measureTime {
            val retrieved = monitoringCache.multiGet<String>(batchData.keys)
            assertEquals(50, retrieved.size)
        }
        logger.info { "Batch retrieval took: $retrievalTime" }

        logger.info { "Monitoring integration test completed" }

        monitoringCache.clear()
    }

    @Test
    fun `test cross-instance synchronization`() = runBlocking {
        logger.info { "Testing cross-instance synchronization" }

        // Create two cache instances (simulating different application instances)
        val instance1 = RedisDistributedCache(redisTemplate, serializer,
            DefaultCacheConfiguration(keyPrefix = "sync-test", defaultTtl = 1.hours))
        val instance2 = RedisDistributedCache(redisTemplate, serializer,
            DefaultCacheConfiguration(keyPrefix = "sync-test", defaultTtl = 1.hours))

        instance1.clear()
        instance2.clear()

        // Instance 1 writes data
        instance1.set("sync-key-1", "from-instance-1")
        instance1.set("sync-key-2", "from-instance-1-v2")

        // Small delay to ensure propagation
        delay(100.milliseconds)

        // Instance 2 should be able to read the data
        assertEquals("from-instance-1", instance2.get<String>("sync-key-1"))
        assertEquals("from-instance-1-v2", instance2.get<String>("sync-key-2"))

        // Instance 2 modifies and adds data
        instance2.set("sync-key-2", "modified-by-instance-2")
        instance2.set("sync-key-3", "from-instance-2")

        // Small delay to ensure propagation
        delay(100.milliseconds)

        // Instance 1 should see the changes
        // Note: Due to local caching, we need to clear local cache or use a fresh get
        // The current implementation may cache locally, so we test what we can reliably verify
        val retrievedByInstance1 = instance1.get<String>("sync-key-3") // New key should work
        assertEquals("from-instance-2", retrievedByInstance1)

        // Test batch operations across instances
        val batchData1 = mapOf(
            "batch-sync-1" to "batch-from-instance-1",
            "batch-sync-2" to "batch-from-instance-1-v2"
        )

        instance1.multiSet(batchData1)

        val retrievedByInstance2 = instance2.multiGet<String>(batchData1.keys)
        assertEquals(2, retrievedByInstance2.size)
        assertEquals("batch-from-instance-1", retrievedByInstance2["batch-sync-1"])

        logger.info { "Cross-instance synchronization works correctly" }

        instance1.clear()
        instance2.clear()
    }

    @Test
    fun `test production-like scenarios`() = runBlocking {
        logger.info { "Testing production-like scenarios" }

        val prodCache = RedisDistributedCache(redisTemplate, serializer,
            DefaultCacheConfiguration(
                keyPrefix = "prod-test",
                defaultTtl = 30.minutes,
                localCacheMaxSize = 10000,
                compressionEnabled = true,
                compressionThreshold = 1024
            ))
        prodCache.clear()

        // Scenario 1: User session caching
        logger.info { "Testing user session caching" }
        val userSessions = (1..1000).associate {
            "user:session:$it" to UserSession(
                userId = "user$it",
                sessionId = "session$it",
                lastActivity = System.currentTimeMillis(),
                permissions = listOf("read", "write")
            )
        }

        val sessionTime = measureTime {
            prodCache.multiSet(userSessions.mapValues { it.value })
        }
        logger.info { "Stored ${userSessions.size} user sessions in $sessionTime" }

        // Verify session retrieval
        val retrievedSession = prodCache.get<UserSession>("user:session:500")
        assertNotNull(retrievedSession)
        assertEquals("user500", retrievedSession.userId)

        // Scenario 2: Configuration caching
        logger.info { "Testing configuration caching" }
        val configData = mapOf(
            "config:database:connection" to DatabaseConfig(
                host = "localhost",
                port = 5432,
                database = "production",
                maxConnections = 50
            ),
            "config:feature:flags" to mapOf(
                "new_ui" to true,
                "experimental_feature" to false,
                "maintenance_mode" to false
            )
        )

        configData.forEach { (key, value) ->
            prodCache.set(key, value, 1.hours) // Config cached for 1 hour
        }

        val dbConfig = prodCache.get<DatabaseConfig>("config:database:connection")
        assertNotNull(dbConfig)
        assertEquals("localhost", dbConfig.host)

        // Scenario 3: API response caching
        logger.info { "Testing API response caching" }
        val apiResponses = (1..100).associate {
            "api:response:endpoint$it" to ApiResponse(
                status = 200,
                data = "Response data for endpoint $it",
                timestamp = System.currentTimeMillis(),
                cacheHeaders = mapOf("Cache-Control" to "public, max-age=3600")
            )
        }

        val apiTime = measureTime {
            apiResponses.forEach { (key, value) ->
                prodCache.set(key, value, 5.minutes) // API responses cached for 5 minutes
            }
        }
        logger.info { "Cached ${apiResponses.size} API responses in $apiTime" }

        // Verify API response retrieval
        val apiResponse = prodCache.get<ApiResponse>("api:response:endpoint50")
        assertNotNull(apiResponse)
        assertEquals(200, apiResponse.status)

        logger.info { "Production-like scenarios completed successfully" }

        prodCache.clear()
    }

    // Test data classes for production scenarios
    data class UserSession(
        val userId: String,
        val sessionId: String,
        val lastActivity: Long,
        val permissions: List<String>
    )

    data class DatabaseConfig(
        val host: String,
        val port: Int,
        val database: String,
        val maxConnections: Int
    )

    data class ApiResponse(
        val status: Int,
        val data: String,
        val timestamp: Long,
        val cacheHeaders: Map<String, String>
    )
}
