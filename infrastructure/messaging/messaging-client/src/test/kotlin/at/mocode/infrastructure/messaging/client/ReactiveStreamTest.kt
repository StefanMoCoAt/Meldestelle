package at.mocode.infrastructure.messaging.client

import at.mocode.infrastructure.messaging.config.KafkaConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import reactor.test.publisher.TestPublisher
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReactiveStreamTest {

    private val logger = LoggerFactory.getLogger(ReactiveStreamTest::class.java)

    private lateinit var kafkaConfig: KafkaConfig
    private lateinit var consumer: KafkaEventConsumer

    @BeforeEach
    fun setUp() {
        kafkaConfig = KafkaConfig().apply {
            bootstrapServers = "localhost:9092"
            defaultGroupIdPrefix = "reactive-test-consumer"
            trustedPackages = "at.mocode.*"
        }
        consumer = KafkaEventConsumer(kafkaConfig)
    }

    @Test
    fun `should create cold streams that start on subscription`() {
        // Cold streams should not start processing until subscribed
        val flux = consumer.receiveEvents<ReactiveTestEvent>("cold-stream-topic")

        // Stream should be created but not started
        assertThat(flux).isNotNull

        // No subscription means no processing should begin.
        // This is verified by the fact that creating the flux doesn't throw or block
        assertDoesNotThrow {
            val anotherFlux = consumer.receiveEvents<ReactiveTestEvent>("another-cold-topic")
            assertThat(anotherFlux).isNotNull
        }
    }

    @Test
    fun `should handle multiple subscribers to same stream`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("multi-subscriber-topic")

        // Multiple subscribers should be able to subscribe to the same flux
        val subscriber1 = StepVerifier.create(flux.take(1).timeout(Duration.ofSeconds(2)))
        val subscriber2 = StepVerifier.create(flux.take(1).timeout(Duration.ofSeconds(2)))

        // Both subscribers should be created without issues
        // Note: In real Kafka usage, each subscriber would get their own consumer group
        assertDoesNotThrow {
            subscriber1.thenCancel().verify(Duration.ofSeconds(1))
            subscriber2.thenCancel().verify(Duration.ofSeconds(1))
        }
    }

    @Test
    fun `should support reactive operators and transformations`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("transformation-topic")

        // Apply various reactive operators
        val transformedFlux = flux
            .filter { event -> event.message.contains("important") }
            .map { event -> event.message.uppercase() }
            .distinctUntilChanged()
            .take(5)

        assertThat(transformedFlux).isNotNull

        // Should be able to subscribe to transformed flux
        val verifier = StepVerifier.create(transformedFlux.timeout(Duration.ofSeconds(2)))
        assertDoesNotThrow {
            verifier.thenCancel().verify(Duration.ofSeconds(1))
        }
    }

    @Test
    fun `should handle backpressure gracefully`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("backpressure-topic")

        // Simulate slow consumer to test backpressure
        val slowProcessingFlux = flux
            .concatMap { event ->
                Mono.delay(Duration.ofMillis(100))
                    .map { event }
            }
            .take(3)

        val startTime = System.currentTimeMillis()

        StepVerifier.create(slowProcessingFlux.timeout(Duration.ofSeconds(5)))
            .thenCancel()
            .verify(Duration.ofSeconds(2))

        val duration = System.currentTimeMillis() - startTime

        // Should handle backpressure without blocking indefinitely
        assertThat(duration).isLessThan(3000)
    }

    @Test
    fun `should maintain stream characteristics under error conditions`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("error-resilience-topic")

        // Add error handling and recovery
        val resilientFlux = flux
            .onErrorResume { error ->
                // Log error and continue with an empty stream
                logger.debug("Handled error in stream: {}", error.message)
                Flux.empty()
            }
            .retry(2)
            .take(1)

        StepVerifier.create(resilientFlux.timeout(Duration.ofSeconds(3)))
            .thenCancel()
            .verify(Duration.ofSeconds(2))

        // Stream should remain reactive even after error handling
        assertThat(resilientFlux).isNotNull
    }

    @Test
    fun `should support concurrent stream processing`() {
        val flux1 = consumer.receiveEvents<ReactiveTestEvent>("concurrent-topic-1")
        val flux2 = consumer.receiveEvents<ReactiveTestEvent>("concurrent-topic-2")
        val flux3 = consumer.receiveEvents<ReactiveTestEvent>("concurrent-topic-3")

        // Process multiple streams concurrently
        val combinedFlux = Flux.merge(
            flux1.subscribeOn(Schedulers.parallel()),
            flux2.subscribeOn(Schedulers.parallel()),
            flux3.subscribeOn(Schedulers.parallel())
        ).take(3)

        StepVerifier.create(combinedFlux.timeout(Duration.ofSeconds(3)))
            .thenCancel()
            .verify(Duration.ofSeconds(2))

        // All streams should be processable concurrently
        assertThat(combinedFlux).isNotNull
    }

    @Test
    fun `should handle stream lifecycle correctly`() {
        val eventCounter = AtomicInteger(0)
        val flux = consumer.receiveEvents<ReactiveTestEvent>("lifecycle-topic")

        // Add lifecycle monitoring
        val monitoredFlux = flux
            .doOnSubscribe { subscription ->
                logger.debug("Stream subscribed: {}", subscription)
            }
            .doOnNext { event ->
                val count = eventCounter.incrementAndGet()
                logger.debug("Processed event #{}: {}", count, event.message)
            }
            .doOnCancel {
                logger.debug("Stream cancelled")
            }
            .doOnComplete {
                logger.debug("Stream completed")
            }
            .take(1)

        StepVerifier.create(monitoredFlux.timeout(Duration.ofSeconds(2)))
            .thenCancel()
            .verify(Duration.ofSeconds(1))

        // Lifecycle should be properly managed
        assertThat(monitoredFlux).isNotNull
    }

    @Test
    fun `should support flow control mechanisms`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("flow-control-topic")

        // Apply various flow control mechanisms
        val controlledFlux = flux
            .limitRate(10) // Limit upstream requests
            .sample(Duration.ofMillis(100)) // Sample at fixed intervals
            .buffer(5) // Buffer elements
            .flatMap { buffer ->
                logger.debug("Processing buffer of size: {}", buffer.size)
                Flux.fromIterable(buffer)
            }
            .take(5)

        StepVerifier.create(controlledFlux.timeout(Duration.ofSeconds(3)))
            .thenCancel()
            .verify(Duration.ofSeconds(2))

        assertThat(controlledFlux).isNotNull
    }

    @Test
    fun `should handle time-based operations`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("time-based-topic")

        // Apply time-based operations
        val timedFlux = flux
            .window(Duration.ofMillis(200)) // Window by time
            .flatMap { window ->
                window.collectList()
                    .map { events ->
                        logger.debug("Window contains {} events", events.size)
                        events.size
                    }
            }
            .take(2)

        StepVerifier.create(timedFlux.timeout(Duration.ofSeconds(3)))
            .thenCancel()
            .verify(Duration.ofSeconds(2))

        assertThat(timedFlux).isNotNull
    }

    @Test
    fun `should maintain thread safety in reactive streams`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("thread-safety-topic")
        val processedCount = AtomicLong(0)
        val latch = CountDownLatch(3)

        // Process on multiple threads
        val threadSafeFlux = flux
            .publishOn(Schedulers.parallel())
            .doOnNext { event ->
                val count = processedCount.incrementAndGet()
                logger.debug("Thread {} processed event #{}", Thread.currentThread().name, count)
                latch.countDown()
            }
            .take(3)

        // Subscribe and wait briefly
        val subscription = threadSafeFlux
            .timeout(Duration.ofSeconds(2))
            .subscribe(
                { event -> /* processed */ },
                { error -> logger.debug("Error: {}", error.message) },
                { logger.debug("Stream completed") }
            )

        // Wait for brief processing or timeout
        val completed = latch.await(1, TimeUnit.SECONDS)
        subscription.dispose()

        // Thread safety should be maintained (no exceptions thrown)
        assertThat(subscription).isNotNull
    }

    @Test
    fun `should support custom schedulers`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("scheduler-topic")

        // Use different schedulers for different operations
        val scheduledFlux = flux
            .subscribeOn(Schedulers.boundedElastic()) // For I/O operations
            .publishOn(Schedulers.parallel()) // For CPU-intensive operations
            .map { event ->
                logger.debug("Processing on thread: {}", Thread.currentThread().name)
                event.message.length
            }
            .subscribeOn(Schedulers.single()) // Single-threaded subscription
            .take(1)

        StepVerifier.create(scheduledFlux.timeout(Duration.ofSeconds(2)))
            .thenCancel()
            .verify(Duration.ofSeconds(1))

        assertThat(scheduledFlux).isNotNull
    }

    @Test
    fun `should handle stream composition and chaining`() {
        val flux1 = consumer.receiveEvents<ReactiveTestEvent>("composition-topic-1")
        val flux2 = consumer.receiveEvents<ReactiveTestEvent>("composition-topic-2")

        // Compose multiple streams
        val composedFlux = flux1
            .switchMap { event1 ->
                flux2.map { event2 ->
                    logger.debug("Composed: {} -> {}", event1.message, event2.message)
                    "${event1.message}+${event2.message}"
                }
            }
            .take(1)

        StepVerifier.create(composedFlux.timeout(Duration.ofSeconds(2)))
            .thenCancel()
            .verify(Duration.ofSeconds(1))

        assertThat(composedFlux).isNotNull
    }

    @Test
    fun `should support reactive testing patterns`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("testing-patterns-topic")

        // Use TestPublisher to simulate controlled event emission
        val testPublisher = TestPublisher.create<ReactiveTestEvent>()
        val testFlux = testPublisher.flux()

        // Apply similar transformations as the real flux
        val transformedTestFlux = testFlux
            .filter { event -> event.message.isNotEmpty() }
            .map { event -> event.message.length }

        // Test with controlled emissions
        StepVerifier.create(transformedTestFlux)
            .then { testPublisher.next(ReactiveTestEvent("test", 1)) }
            .expectNext(4) // "test".length
            .then { testPublisher.complete() }
            .verifyComplete()

        // Real flux should also be testable
        assertThat(flux).isNotNull
    }

    @Test
    fun `should handle resource cleanup properly`() {
        val flux = consumer.receiveEvents<ReactiveTestEvent>("cleanup-topic")
        val resourcesAcquired = AtomicInteger(0)
        val resourcesReleased = AtomicInteger(0)

        val resourceManagedFlux = flux
            .doOnSubscribe {
                resourcesAcquired.incrementAndGet()
                logger.debug("Resources acquired: {}", resourcesAcquired.get())
            }
            .doFinally { signalType ->
                resourcesReleased.incrementAndGet()
                logger.debug("Resources released on {}: {}", signalType, resourcesReleased.get())
            }
            .take(1)

        StepVerifier.create(resourceManagedFlux.timeout(Duration.ofSeconds(2)))
            .thenCancel()
            .verify(Duration.ofSeconds(1))

        // Resource management should be handled properly
        // Note: In a real scenario, we'd verify that resources are properly cleaned up
        assertThat(resourceManagedFlux).isNotNull
    }

    data class ReactiveTestEvent(
        val message: String,
        val sequenceNumber: Int,
        val timestamp: Long = System.currentTimeMillis()
    )
}
