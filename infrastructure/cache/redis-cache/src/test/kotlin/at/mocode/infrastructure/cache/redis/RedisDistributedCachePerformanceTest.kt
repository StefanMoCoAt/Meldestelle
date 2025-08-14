package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.measureTime
import java.util.concurrent.atomic.AtomicInteger

/**
 * Performance and Load Tests for RedisDistributedCache
 */
@Testcontainers
class RedisDistributedCachePerformanceTest {

    companion object {
        private val logger = KotlinLogging.logger {}

        @Container
        val redisContainer = GenericContainer<Nothing>(DockerImageName.parse("redis:7-alpine")).apply {
            withExposedPorts(6379)
        }
    }

    private lateinit var redisTemplate: RedisTemplate<String, ByteArray>
    private lateinit var serializer: CacheSerializer
    private lateinit var config: CacheConfiguration
    private lateinit var cache: RedisDistributedCache

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
            keyPrefix = "perf-test",
            defaultTtl = 30.minutes
        )

        cache = RedisDistributedCache(redisTemplate, serializer, config)
        cache.clear()
    }

    @Test
    fun `test cache performance with high concurrent access`() = runTest {
        logger.info { "Starting concurrent access test" }
        val numberOfCoroutines = 100
        val operationsPerCoroutine = 50
        val successCounter = AtomicInteger(0)
        val errorCounter = AtomicInteger(0)

        val time = measureTime {
            val jobs = (1..numberOfCoroutines).map { coroutineId ->
                launch {
                    repeat(operationsPerCoroutine) { operationId ->
                        try {
                            val key = "concurrent-$coroutineId-$operationId"
                            val value = "value-$coroutineId-$operationId"

                            // Set operation
                            cache.set(key, value)

                            // Get operation
                            val retrieved = cache.get<String>(key)
                            if (retrieved == value) {
                                successCounter.incrementAndGet()
                            } else {
                                errorCounter.incrementAndGet()
                                logger.warn { "Mismatch: expected $value, got $retrieved" }
                            }
                        } catch (e: Exception) {
                            errorCounter.incrementAndGet()
                            logger.warn { "Error in operation: ${e.message}" }
                        }
                    }
                }
            }
            jobs.joinAll()
        }

        val totalOperations = numberOfCoroutines * operationsPerCoroutine
        val successRate = successCounter.get().toDouble() / totalOperations
        val operationsPerSecond = totalOperations / time.inWholeSeconds

        logger.info { "Performance test completed" }
        logger.info { "Total operations: $totalOperations" }
        logger.info { "Successful operations: ${successCounter.get()}" }
        logger.info { "Failed operations: ${errorCounter.get()}" }
        logger.info { "Success rate: ${successRate * 100}%" }
        logger.info { "Total time: $time" }
        logger.info { "Operations per second: $operationsPerSecond" }

        assertTrue(successRate > 0.95, "Success rate should be > 95%, but was ${successRate * 100}%")
    }

    @Test
    fun `test cache behavior under memory pressure`() {
        logger.info { "Starting memory pressure test" }

        // Create cache with limited local cache size
        val limitedConfig = DefaultCacheConfiguration(
            keyPrefix = "memory-test",
            localCacheMaxSize = 100, // Very small local cache
            defaultTtl = 30.minutes
        )
        val limitedCache = RedisDistributedCache(redisTemplate, serializer, limitedConfig)

        // Fill cache with more entries than local cache can hold
        val numberOfEntries = 500
        val largeValue = "A".repeat(1000) // 1KB per entry

        val time = measureTime {
            repeat(numberOfEntries) { i ->
                val key = "memory-pressure-$i"
                limitedCache.set(key, largeValue)
            }
        }

        logger.info { "Inserted $numberOfEntries entries in $time" }

        // Verify that entries are still retrievable (should come from Redis)
        var retrievedCount = 0
        repeat(numberOfEntries) { i ->
            val key = "memory-pressure-$i"
            val retrieved = limitedCache.get<String>(key)
            if (retrieved == largeValue) {
                retrievedCount++
            }
        }

        logger.info { "Successfully retrieved $retrievedCount out of $numberOfEntries entries" }
        assertTrue(retrievedCount > numberOfEntries * 0.9,
                  "Should retrieve > 90% of entries, but retrieved only ${retrievedCount * 100.0 / numberOfEntries}%")

        limitedCache.clear()
    }

    @Test
    fun `test bulk operations performance`() {
        logger.info { "Starting bulk operations performance test" }

        val batchSize = 1000
        val entries = (1..batchSize).associate {
            "bulk-$it" to "bulk-value-$it"
        }

        // Test multiSet performance
        val setTime = measureTime {
            cache.multiSet(entries)
        }

        // Test multiGet performance
        val getTime = measureTime {
            val retrieved = cache.multiGet<String>(entries.keys)
            assertEquals(batchSize, retrieved.size)
        }

        val setRatePerSec = if (setTime.inWholeSeconds > 0) batchSize / setTime.inWholeSeconds else batchSize * 1000 / maxOf(1, setTime.inWholeMilliseconds)
        val getRatePerSec = if (getTime.inWholeSeconds > 0) batchSize / getTime.inWholeSeconds else batchSize * 1000 / maxOf(1, getTime.inWholeMilliseconds)

        logger.info { "Bulk operations performance completed" }
        logger.info { "MultiSet ${batchSize} entries: $setTime" }
        logger.info { "MultiGet ${batchSize} entries: $getTime" }
        logger.info { "Set rate: $setRatePerSec entries/sec" }
        logger.info { "Get rate: $getRatePerSec entries/sec" }

        assertTrue(setTime.inWholeSeconds < 10, "MultiSet should complete within 10 seconds")
        assertTrue(getTime.inWholeSeconds < 10, "MultiGet should complete within 10 seconds")
    }
}
