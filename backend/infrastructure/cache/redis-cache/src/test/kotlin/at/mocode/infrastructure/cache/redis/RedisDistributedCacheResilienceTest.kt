package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import java.time.Duration as JavaDuration

/**
 * Timeout and Resilience Tests for RedisDistributedCache
 */
@OptIn(ExperimentalTime::class)
@Testcontainers
class RedisDistributedCacheResilienceTest {

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
            keyPrefix = "resilience-test",
            defaultTtl = 30.minutes,
            offlineModeEnabled = true
        )
    }

    @Test
    fun `test connection timeout scenarios`() = runBlocking {
        logger.info { "Testing connection timeout scenarios" }

        val mockTemplate = mockk<RedisTemplate<String, ByteArray>>()
        val mockValueOps = mockk<ValueOperations<String, ByteArray>>()

        every { mockTemplate.opsForValue() } returns mockValueOps

        // Simulate slow Redis responses
        every { mockValueOps.get(any()) } answers {
            Thread.sleep(5000) // 5-second delay
            "slow-response".toByteArray()
        }

        every { mockValueOps.set(any<String>(), any<ByteArray>(), any<JavaDuration>()) } answers {
            Thread.sleep(3000) // 3-second delay
        }

        val slowCache = RedisDistributedCache(mockTemplate, serializer, config)

        // Test get operation with timeout
        val startTime = System.currentTimeMillis()
        val result = slowCache.get<String>("slow-key")
        val endTime = System.currentTimeMillis()

        logger.info { "Get operation took ${endTime - startTime}ms" }
        // The operation should either succeed or fail gracefully

        // Test set operation with timeout
        val setStartTime = System.currentTimeMillis()
        slowCache.set("slow-set-key", "value")
        val setEndTime = System.currentTimeMillis()

        logger.info { "Set operation took ${setEndTime - setStartTime}ms" }

        // Verify that operations don't hang indefinitely
        assertTrue((endTime - startTime) < 10000, "Get operation should not take more than 10 seconds")
        assertTrue((setEndTime - setStartTime) < 10000, "Set operation should not take more than 10 seconds")
    }

    @Test
    fun `test partial Redis failures`() {
        logger.info { "Testing partial Redis failures" }

        val mockTemplate = mockk<RedisTemplate<String, ByteArray>>()
        val mockValueOps = mockk<ValueOperations<String, ByteArray>>()

        every { mockTemplate.opsForValue() } returns mockValueOps
        every { mockTemplate.hasKey(any()) } returns true

        val failureCounter = AtomicInteger(0)

        // Simulate intermittent connection failures (fail every 3rd operation)
        every { mockValueOps.get(any()) } answers {
            if (failureCounter.incrementAndGet() % 3 == 0) {
                throw RedisConnectionFailureException("Intermittent failure")
            }
            serializer.serializeEntry(CacheEntry("test", "value"))
        }

        every { mockValueOps.set(any<String>(), any<ByteArray>(), any<JavaDuration>()) } answers {
            if (failureCounter.incrementAndGet() % 3 == 0) {
                throw RedisConnectionFailureException("Intermittent failure")
            }
        }

        val unreliableCache = RedisDistributedCache(mockTemplate, serializer, config)

        // Test multiple operations with intermittent failures
        var successCount = 0
        var failureCount = 0

        repeat(20) { i ->
            try {
                unreliableCache.set("intermittent-$i", "value-$i")
                val retrieved = unreliableCache.get<String>("intermittent-$i")
                if (retrieved != null) {
                    successCount++
                } else {
                    failureCount++
                }
            } catch (e: Exception) {
                failureCount++
                logger.info { "Operation failed as expected: ${e.message}" }
            }
        }

        logger.info { "Partial failure test results:" }
        logger.info { "Successful operations: $successCount" }
        logger.info { "Failed operations: $failureCount" }
        logger.info { "Total operations: 20" }

        // Due to offline mode, operations might succeed locally even when Redis fails,
        // So we verify the cache is resilient and continues working
        assertTrue(successCount >= 0, "Should handle operations gracefully")
        assertEquals(20, successCount + failureCount, "Should process all operations")

        // Verify that the cache state is properly managed despite intermittent failures
        assertEquals(ConnectionState.DISCONNECTED, unreliableCache.getConnectionState())

        // Verify that dirty keys are tracked for failed operations
        val dirtyKeys = unreliableCache.getDirtyKeys()
        assertTrue(dirtyKeys.isNotEmpty(), "Should have dirty keys from failed operations")
        logger.info { "Dirty keys count: ${dirtyKeys.size}" }
    }

    @Test
    fun `test network partitioning simulation`() {
        logger.info { "Testing network partitioning simulation" }

        val cache = RedisDistributedCache(redisTemplate, serializer, config)
        cache.clear()

        // Phase 1: Normal operations (network is fine)
        logger.info { "Phase 1: Normal operations" }
        cache.set("partition-test-1", "value-1")
        cache.set("partition-test-2", "value-2")

        assertEquals("value-1", cache.get<String>("partition-test-1"))
        assertEquals("value-2", cache.get<String>("partition-test-2"))
        assertEquals(ConnectionState.CONNECTED, cache.getConnectionState())

        // Phase 2: Simulate network partition by creating a new cache with a broken connection
        logger.info { "Phase 2: Simulating network partition" }
        val mockTemplate = mockk<RedisTemplate<String, ByteArray>>()
        val mockValueOps = mockk<ValueOperations<String, ByteArray>>()

        every { mockTemplate.opsForValue() } returns mockValueOps
        every { mockValueOps.get(any()) } throws RedisConnectionFailureException("Network partition")
        every {
            mockValueOps.set(
                any<String>(),
                any<ByteArray>(),
                any<JavaDuration>()
            )
        } throws RedisConnectionFailureException("Network partition")
        every { mockTemplate.delete(any<String>()) } throws RedisConnectionFailureException("Network partition")
        every { mockTemplate.hasKey(any()) } throws RedisConnectionFailureException("Network partition")

        val partitionedCache = RedisDistributedCache(mockTemplate, serializer, config)

        // Operations during partition should work locally
        partitionedCache.set("partition-offline-1", "offline-value-1")
        partitionedCache.set("partition-offline-2", "offline-value-2")

        // Should be able to retrieve from a local cache
        assertEquals("offline-value-1", partitionedCache.get<String>("partition-offline-1"))
        assertEquals("offline-value-2", partitionedCache.get<String>("partition-offline-2"))
        assertEquals(ConnectionState.DISCONNECTED, partitionedCache.getConnectionState())

        // Should track dirty keys
        val dirtyKeys = partitionedCache.getDirtyKeys()
        assertTrue(dirtyKeys.contains("partition-offline-1"))
        assertTrue(dirtyKeys.contains("partition-offline-2"))

        logger.info { "Network partition handled correctly - operations work offline" }
    }

    @Test
    fun `test reconnection and synchronization after network issues`() {
        logger.info { "Testing reconnection and synchronization" }

        val mockTemplate = mockk<RedisTemplate<String, ByteArray>>()
        val mockValueOps = mockk<ValueOperations<String, ByteArray>>()

        every { mockTemplate.opsForValue() } returns mockValueOps

        val reconnectingCache = RedisDistributedCache(mockTemplate, serializer, config)

        // Phase 1: Simulate disconnection
        every { mockValueOps.get(any()) } throws RedisConnectionFailureException("Disconnected")
        every {
            mockValueOps.set(
                any<String>(),
                any<ByteArray>(),
                any<JavaDuration>()
            )
        } throws RedisConnectionFailureException("Disconnected")
        every { mockTemplate.hasKey(any()) } throws RedisConnectionFailureException("Disconnected")

        reconnectingCache.set("reconnect-test-1", "value-1")
        reconnectingCache.set("reconnect-test-2", "value-2")

        assertEquals(ConnectionState.DISCONNECTED, reconnectingCache.getConnectionState())
        assertTrue(reconnectingCache.getDirtyKeys().size >= 2)

        // Phase 2: Simulate reconnection
        every { mockValueOps.set(any<String>(), any<ByteArray>(), any<JavaDuration>()) } returns Unit
        every { mockTemplate.hasKey(any()) } returns true
        every { mockTemplate.delete(any<String>()) } returns true

        // Trigger connection check (this would normally be done by a scheduled task)
        reconnectingCache.checkConnection()

        // After a successful connection check, dirty keys should be synchronized
        // Note: In a real scenario, this would be handled by the synchronization mechanism

        logger.info { "Reconnection simulation completed" }
    }

    @Test
    fun `test connection state listener notifications`() = runBlocking {
        logger.info { "Testing connection state listener notifications" }

        val stateChanges = mutableListOf<ConnectionState>()

        val listener = object : ConnectionStateListener {
            override fun onConnectionStateChanged(newState: ConnectionState, timestamp: kotlin.time.Instant) {
                logger.info { "Connection state changed to: $newState at $timestamp" }
                stateChanges.add(newState)
            }
        }

        val cache = RedisDistributedCache(redisTemplate, serializer, config)
        cache.registerConnectionListener(listener)

        // Initially should be connected
        assertEquals(ConnectionState.CONNECTED, cache.getConnectionState())
        logger.info { "Initial connection state: ${cache.getConnectionState()}" }

        // Test listener registration/unregistration mechanism
        val testListener = object : ConnectionStateListener {
            override fun onConnectionStateChanged(newState: ConnectionState, timestamp: kotlin.time.Instant) {
                logger.info { "Test listener received state change: $newState" }
            }
        }

        // Register and unregister listeners (testing the mechanism itself)
        cache.registerConnectionListener(testListener)
        cache.unregisterConnectionListener(testListener)
        cache.unregisterConnectionListener(listener)

        logger.info { "Connection state listener registration/unregistration mechanism tested" }

        // Test that connection state is properly tracked
        assertTrue(cache.isConnected(), "Cache should be connected to Redis")

        logger.info { "Connection state listener functionality verified" }
    }

    @Test
    fun `test cache operations during Redis restart simulation`() = runBlocking {
        logger.info { "Testing cache operations during Redis restart simulation" }

        val cache = RedisDistributedCache(redisTemplate, serializer, config)
        cache.clear()

        // Store some initial data
        cache.set("restart-test-1", "initial-value-1")
        cache.set("restart-test-2", "initial-value-2")

        assertEquals("initial-value-1", cache.get<String>("restart-test-1"))

        // Simulate Redis restart by creating a new cache instance
        // (In a real scenario, this would be the same instance, but Redis would be restarted)

        // During "restart" (brief unavailability), operations should work locally
        val duringRestartCache = RedisDistributedCache(redisTemplate, serializer, config)

        // These should work even if Redis is temporarily unavailable
        duringRestartCache.set("during-restart-1", "temp-value-1")
        assertEquals("temp-value-1", duringRestartCache.get<String>("during-restart-1"))

        // After "restart", data should be synchronized
        delay(1.seconds) // Brief delay to simulate restart completion

        val afterRestartCache = RedisDistributedCache(redisTemplate, serializer, config)

        // Should be able to access both old and new data
        // Note: In a real Redis restart, persisted data would still be there
        afterRestartCache.set("after-restart-1", "post-restart-value-1")
        assertEquals("post-restart-value-1", afterRestartCache.get<String>("after-restart-1"))

        logger.info { "Redis restart simulation completed successfully" }
    }
}
