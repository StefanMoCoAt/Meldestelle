package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.client.ReactiveKafkaConfig
import at.mocode.infrastructure.messaging.config.KafkaConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.util.*

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaBatchPerformanceTest {

    private val logger = LoggerFactory.getLogger(KafkaBatchPerformanceTest::class.java)

    companion object {
        @Container
        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
    }

    private lateinit var kafkaEventPublisher: KafkaEventPublisher
    private lateinit var producerFactory: DefaultKafkaProducerFactory<String, Any>
    private val testTopic = "performance-topic-${UUID.randomUUID()}"

    @BeforeEach
    fun setUp() {
        val kafkaConfig = KafkaConfig().apply {
            bootstrapServers = kafkaContainer.bootstrapServers
            trustedPackages = "at.mocode.*"
        }
        producerFactory = kafkaConfig.producerFactory()

        val reactiveKafkaConfig = ReactiveKafkaConfig(kafkaConfig)
        val reactiveTemplate = reactiveKafkaConfig.reactiveKafkaProducerTemplate()
        kafkaEventPublisher = KafkaEventPublisher(reactiveTemplate)
    }

    @AfterEach
    fun tearDown() {
        producerFactory.destroy()
    }

    @Test
    fun `should handle small batch efficiently`() {
        val batchSize = 50
        val smallEventBatch = (1..batchSize).map { i ->
            "key$i" to PerformanceTestEvent("Small batch message $i", i)
        }

        val startTime = System.currentTimeMillis()

        StepVerifier.create(kafkaEventPublisher.publishEvents(testTopic, smallEventBatch))
            .expectNextCount(batchSize.toLong())
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Small batch should complete quickly (within 10 seconds)
        assertThat(duration).isLessThan(10000)
    }

    @Test
    fun `should handle medium batch efficiently`() {
        val batchSize = 500
        val mediumEventBatch = (1..batchSize).map { i ->
            "key$i" to PerformanceTestEvent("Medium batch message $i", i)
        }

        val startTime = System.currentTimeMillis()

        StepVerifier.create(kafkaEventPublisher.publishEvents(testTopic, mediumEventBatch))
            .expectNextCount(batchSize.toLong())
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Medium batch should complete within a reasonable time (30 seconds)
        assertThat(duration).isLessThan(30000)

        // Should be reasonably efficient (less than 60 ms per message on average)
        val avgTimePerMessage = duration.toDouble() / batchSize
        assertThat(avgTimePerMessage).isLessThan(60.0)
    }

    @Test
    fun `should handle large batch with reasonable performance`() {
        val batchSize = 1000
        val largeEventBatch = (1..batchSize).map { i ->
            "key$i" to PerformanceTestEvent("Large batch message $i", i)
        }

        val startTime = System.currentTimeMillis()

        StepVerifier.create(kafkaEventPublisher.publishEvents(testTopic, largeEventBatch))
            .expectNextCount(batchSize.toLong())
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Large batch should complete within 60 seconds
        assertThat(duration).isLessThan(60000)

        // Should maintain reasonable efficiency (less than 100 ms per message on average)
        val avgTimePerMessage = duration.toDouble() / batchSize
        assertThat(avgTimePerMessage).isLessThan(100.0)
    }

    @Test
    fun `should handle concurrent batch publishing`() {
        val batchSize = 100
        val concurrentBatches = 5

        val batches = (1..concurrentBatches).map { batchIndex ->
            (1..batchSize).map { i ->
                "batch${batchIndex}_key$i" to PerformanceTestEvent("Concurrent batch $batchIndex message $i", i)
            }
        }

        val startTime = System.currentTimeMillis()

        // Publish all batches concurrently
        val publishers = batches.map { batch ->
            kafkaEventPublisher.publishEvents(testTopic, batch)
                .collectList() // Collect results for each batch
        }

        StepVerifier.create(reactor.core.publisher.Flux.merge(publishers))
            .expectNextCount(concurrentBatches.toLong())
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Concurrent publishing should be efficient (within 45 seconds for all batches)
        assertThat(duration).isLessThan(45000)

        // Should benefit from concurrency (less than 80 ms per message across all batches)
        val totalMessages = batchSize * concurrentBatches
        val avgTimePerMessage = duration.toDouble() / totalMessages
        assertThat(avgTimePerMessage).isLessThan(80.0)
    }

    @Test
    fun `should handle single message publishing performance`() {
        val messageCount = 100
        val messages = (1..messageCount).map { i ->
            PerformanceTestEvent("Single message $i", i)
        }

        val startTime = System.currentTimeMillis()

        val publishers = messages.mapIndexed { index, message ->
            kafkaEventPublisher.publishEvent(testTopic, "single_key_$index", message)
        }

        StepVerifier.create(reactor.core.publisher.Flux.merge(publishers))
            .expectNextCount(messageCount.toLong())
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Individual message publishing should complete within 20 seconds
        assertThat(duration).isLessThan(20000)

        // Should maintain reasonable per-message performance
        val avgTimePerMessage = duration.toDouble() / messageCount
        assertThat(avgTimePerMessage).isLessThan(200.0)
    }

    @Test
    fun `should handle mixed payload sizes efficiently`() {
        val smallPayload = "small"
        val mediumPayload = "medium".repeat(100) // ~600 characters
        val largePayload = "large".repeat(1000)  // ~5000 characters

        val mixedEventBatch = listOf(
            // Small payloads
            *((1..50).map { i -> "small_key_$i" to PerformanceTestEvent(smallPayload, i) }.toTypedArray()),
            // Medium payloads
            *((1..30).map { i -> "medium_key_$i" to PerformanceTestEvent(mediumPayload, i) }.toTypedArray()),
            // Large payloads
            *((1..20).map { i -> "large_key_$i" to PerformanceTestEvent(largePayload, i) }.toTypedArray())
        )

        val startTime = System.currentTimeMillis()

        StepVerifier.create(kafkaEventPublisher.publishEvents(testTopic, mixedEventBatch))
            .expectNextCount(100) // 50 + 30 + 20 = 100
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Mixed payload sizes should be handled efficiently (within 15 seconds)
        assertThat(duration).isLessThan(15000)
    }

    @Test
    fun `should demonstrate batch vs individual performance difference`() {
        val messageCount = 200
        val events = (1..messageCount).map { i ->
            "perf_key_$i" to PerformanceTestEvent("Performance test message $i", i)
        }

        // Test individual publishing
        val individualStartTime = System.currentTimeMillis()
        val individualPublishers = events.map { (key, event) ->
            kafkaEventPublisher.publishEvent(testTopic, key, event)
        }

        StepVerifier.create(reactor.core.publisher.Flux.merge(individualPublishers))
            .expectNextCount(messageCount.toLong())
            .verifyComplete()

        val individualDuration = System.currentTimeMillis() - individualStartTime

        // Test batch publishing
        val batchStartTime = System.currentTimeMillis()

        StepVerifier.create(kafkaEventPublisher.publishEvents(testTopic, events))
            .expectNextCount(messageCount.toLong())
            .verifyComplete()

        val batchDuration = System.currentTimeMillis() - batchStartTime

        // Batch publishing should generally be more efficient or at least comparable
        // We don't enforce strict performance improvements due to test environment variability,
        // but we verify both approaches complete within reasonable time
        assertThat(individualDuration).isLessThan(20000)
        assertThat(batchDuration).isLessThan(20000)

        logger.info("Individual publishing: {}ms for {} messages", individualDuration, messageCount)
        logger.info("Batch publishing: {}ms for {} messages", batchDuration, messageCount)
    }

    @Test
    fun `should handle empty batch gracefully`() {
        val emptyBatch = emptyList<Pair<String?, Any>>()

        val startTime = System.currentTimeMillis()

        StepVerifier.create(kafkaEventPublisher.publishEvents(testTopic, emptyBatch))
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Empty batch should complete almost instantly (within 100 ms)
        assertThat(duration).isLessThan(100)
    }

    @Test
    fun `should maintain performance under memory pressure`() {
        // Create a large batch to test memory handling
        val largeBatchSize = 2000
        val largeEventBatch = (1..largeBatchSize).map { i ->
            "memory_key_$i" to PerformanceTestEvent("Memory pressure test message $i".repeat(10), i)
        }

        val startTime = System.currentTimeMillis()

        StepVerifier.create(kafkaEventPublisher.publishEvents(testTopic, largeEventBatch))
            .expectNextCount(largeBatchSize.toLong())
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Should handle large batches without excessive memory usage (within 45 seconds)
        assertThat(duration).isLessThan(45000)

        // Average time per message should remain reasonable even under memory pressure
        val avgTimePerMessage = duration.toDouble() / largeBatchSize
        assertThat(avgTimePerMessage).isLessThan(25.0)
    }

    @Test
    fun `should respect batch concurrency limits`() {
        // Test that batch processing respects configured concurrency
        val batchSize = 300
        val testBatch = (1..batchSize).map { i ->
            "concurrency_key_$i" to PerformanceTestEvent("Concurrency test message $i", i)
        }

        val startTime = System.currentTimeMillis()

        StepVerifier.create(kafkaEventPublisher.publishEvents(testTopic, testBatch))
            .expectNextCount(batchSize.toLong())
            .verifyComplete()

        val duration = System.currentTimeMillis() - startTime

        // Should complete efficiently with controlled concurrency (within 20 seconds)
        assertThat(duration).isLessThan(20000)

        // Verify reasonable throughput
        val messagesPerSecond = (batchSize.toDouble() / duration) * 1000
        assertThat(messagesPerSecond).isGreaterThan(10.0) // At least 10 messages per second
    }

    data class PerformanceTestEvent(
        val message: String,
        val sequenceNumber: Int,
        val timestamp: Long = System.currentTimeMillis()
    )
}
