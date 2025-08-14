package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.event.DomainEvent
import at.mocode.core.domain.model.AggregateId
import at.mocode.core.domain.model.EventType
import at.mocode.core.domain.model.EventVersion
import at.mocode.infrastructure.eventstore.api.EventSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Consumer Resilience Tests - Important for Event-Processing reliability.
 */
@Testcontainers
class RedisEventConsumerResilienceTest {

    private val logger = LoggerFactory.getLogger(RedisEventConsumerResilienceTest::class.java)

    companion object {
        @Container
        val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
    }

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var serializer: EventSerializer
    private lateinit var properties: RedisEventStoreProperties
    private lateinit var eventStore: RedisEventStore
    private lateinit var consumer1: RedisEventConsumer
    private lateinit var consumer2: RedisEventConsumer

    @BeforeEach
    fun setUp() {
        val redisPort = redisContainer.getMappedPort(6379)
        val redisHost = redisContainer.host

        val redisConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        val connectionFactory = LettuceConnectionFactory(redisConfig)
        connectionFactory.afterPropertiesSet()

        redisTemplate = StringRedisTemplate(connectionFactory)

        serializer = JacksonEventSerializer().apply {
            registerEventType(ResilienceTestEvent::class.java, "ResilienceTestEvent")
            registerEventType(SlowTestEvent::class.java, "SlowTestEvent")
            registerEventType(FailingTestEvent::class.java, "FailingTestEvent")
        }

        properties = RedisEventStoreProperties().apply {
            streamPrefix = "test-stream:"
            allEventsStream = "all-events"
            consumerGroup = "resilience-test-group"
            consumerName = "resilience-consumer-1"
            claimIdleTimeout = java.time.Duration.ofMillis(100) // Short timeout for testing
            pollTimeout = java.time.Duration.ofMillis(50)
            maxBatchSize = 10
        }

        eventStore = RedisEventStore(redisTemplate, serializer, properties)
        consumer1 = RedisEventConsumer(redisTemplate, serializer, properties)

        // Create second consumer with different name for testing multiple consumers
        val properties2 = RedisEventStoreProperties().apply {
            streamPrefix = properties.streamPrefix
            allEventsStream = properties.allEventsStream
            consumerGroup = properties.consumerGroup
            consumerName = "resilience-consumer-2"
            claimIdleTimeout = properties.claimIdleTimeout
            pollTimeout = properties.pollTimeout
            maxBatchSize = properties.maxBatchSize
        }
        consumer2 = RedisEventConsumer(redisTemplate, serializer, properties2)

        cleanupRedis()
    }

    @AfterEach
    fun tearDown() {
        try {
            consumer1.shutdown()
            consumer2.shutdown()
        } catch (_: Exception) {
            // Ignore shutdown errors in tests
        }
        cleanupRedis()
    }

    private fun cleanupRedis() {
        val keys = redisTemplate.keys("${properties.streamPrefix}*")
        if (!keys.isNullOrEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    @Test
    fun `should handle multiple consumers processing events without conflicts`() {
        val aggregateId = UUID.randomUUID()
        val latch = CountDownLatch(2)
        val processedEvents = CopyOnWriteArrayList<DomainEvent>()

        // Both consumers will process events
        consumer1.registerEventHandler("ResilienceTestEvent") { event ->
            processedEvents.add(event)
            logger.debug("Consumer1 processed: {}", (event as ResilienceTestEvent).data)
            latch.countDown()
        }

        consumer2.registerEventHandler("ResilienceTestEvent") { event ->
            processedEvents.add(event)
            logger.debug("Consumer2 processed: {}", (event as ResilienceTestEvent).data)
            latch.countDown()
        }

        // Initialize both consumers
        consumer1.init()
        consumer2.init()

        // Publish test events
        val event1 = ResilienceTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            data = "Multi consumer event 1"
        )
        val event2 = ResilienceTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(2L),
            data = "Multi consumer event 2"
        )

        eventStore.appendToStream(listOf(event1, event2), aggregateId, 0)

        // Let both consumers poll
        consumer1.pollEvents()
        consumer2.pollEvents()

        // Wait for processing
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Events were not processed within timeout")

        // Verify that events were processed (by either consumer due to consumer groups)
        assertTrue(processedEvents.size >= 2, "Expected at least 2 processed events, got ${processedEvents.size}")

        println("[DEBUG_LOG] Processed ${processedEvents.size} events total")
    }

    @Test
    fun `should handle consumer group creation and recovery`() {
        // Test that a consumer group is created automatically during init()
        val aggregateId = UUID.randomUUID()
        val latch = CountDownLatch(1)
        val receivedEvents = CopyOnWriteArrayList<DomainEvent>()

        // Register handler before init
        consumer1.registerEventHandler("ResilienceTestEvent") { receivedEvent ->
            receivedEvents.add(receivedEvent)
            latch.countDown()
        }

        // Init should create consumer groups automatically
        consumer1.init()

        // Add an event after initialization
        val event = ResilienceTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            data = "Group creation test"
        )
        eventStore.appendToStream(event, aggregateId, 0)

        // Consumer should be able to process events from the automatically created group
        consumer1.pollEvents()

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Event was not processed")
        assertEquals(1, receivedEvents.size)
        assertEquals("Group creation test", (receivedEvents[0] as ResilienceTestEvent).data)
    }

    @Test
    fun `should process events exactly once in consumer group`() {
        val aggregateId = UUID.randomUUID()
        val numberOfEvents = 10
        val processedEvents = ConcurrentHashMap<String, AtomicInteger>()
        val latch = CountDownLatch(numberOfEvents)

        // Register the same handler on both consumers
        val handler = { event: DomainEvent ->
            val testEvent = event as ResilienceTestEvent
            processedEvents.computeIfAbsent(testEvent.data) { AtomicInteger(0) }.incrementAndGet()
            logger.debug("Processed: {}", testEvent.data)
            latch.countDown()
        }

        consumer1.registerEventHandler("ResilienceTestEvent", handler)
        consumer2.registerEventHandler("ResilienceTestEvent", handler)

        // Initialize both consumers
        consumer1.init()
        consumer2.init()

        // Create and append events
        val events = (1..numberOfEvents).map { i ->
            ResilienceTestEvent(
                aggregateId = AggregateId(aggregateId),
                version = EventVersion(i.toLong()),
                data = "Exactly-once event $i"
            )
        }

        eventStore.appendToStream(events, aggregateId, 0)

        // Start polling from both consumers simultaneously
        val executor = Executors.newFixedThreadPool(2)

        executor.submit {
            repeat(5) {
                consumer1.pollEvents()
                Thread.sleep(50)
            }
        }

        executor.submit {
            repeat(5) {
                consumer2.pollEvents()
                Thread.sleep(50)
            }
        }

        // Wait for all events to be processed
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Not all events were processed in time")
        executor.shutdown()

        // Verify each event was processed exactly once across both consumers
        assertEquals(numberOfEvents, processedEvents.size)
        processedEvents.forEach { (eventData, count) ->
            assertEquals(1, count.get(), "Event '$eventData' was processed ${count.get()} times instead of exactly once")
        }

        logger.debug("All {} events processed exactly once", numberOfEvents)
    }

    @Test
    fun `should handle slow event handlers gracefully`() {
        val aggregateId = UUID.randomUUID()
        val processedEvents = CopyOnWriteArrayList<String>()
        val latch = CountDownLatch(3)

        // Register a slow handler
        consumer1.registerEventHandler("SlowTestEvent") { event ->
            val slowEvent = event as SlowTestEvent
            processedEvents.add("Started: ${slowEvent.data}")
            Thread.sleep(slowEvent.processingTimeMs) // Simulate slow processing
            processedEvents.add("Completed: ${slowEvent.data}")
            latch.countDown()
        }

        consumer1.init()

        // Create events with different processing times
        val events = listOf(
            SlowTestEvent(AggregateId(aggregateId), EventVersion(1L), "Fast event", 10),
            SlowTestEvent(AggregateId(aggregateId), EventVersion(2L), "Medium event", 100),
            SlowTestEvent(AggregateId(aggregateId), EventVersion(3L), "Slow event", 200)
        )

        eventStore.appendToStream(events, aggregateId, 0)

        // Start processing
        val startTime = System.currentTimeMillis()
        consumer1.pollEvents()

        // Wait for processing to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Slow events were not processed within timeout")
        val totalTime = System.currentTimeMillis() - startTime

        // Verify all events were processed
        assertEquals(6, processedEvents.size) // 3 started + 3 completed
        assertTrue(processedEvents.contains("Started: Fast event"))
        assertTrue(processedEvents.contains("Completed: Fast event"))
        assertTrue(processedEvents.contains("Started: Medium event"))
        assertTrue(processedEvents.contains("Completed: Medium event"))
        assertTrue(processedEvents.contains("Started: Slow event"))
        assertTrue(processedEvents.contains("Completed: Slow event"))

        logger.debug("Processed {} slow events in {}ms", events.size, totalTime)
        processedEvents.forEach { logger.debug("Event: {}", it) }
    }

    @Test
    fun `should handle consumer restart correctly`() {
        val aggregateId = UUID.randomUUID()
        val firstPhaseEvents = mutableListOf<DomainEvent>()
        val secondPhaseEvents = mutableListOf<DomainEvent>()

        // First processing session
        val firstLatch = CountDownLatch(1)
        consumer1.registerEventHandler("ResilienceTestEvent") { event ->
            firstPhaseEvents.add(event)
            firstLatch.countDown()
        }

        consumer1.init()

        // Add and process the first event
        val event1 = ResilienceTestEvent(AggregateId(aggregateId), EventVersion(1L), "Before restart")
        eventStore.appendToStream(event1, aggregateId, 0)

        consumer1.pollEvents()
        assertTrue(firstLatch.await(3, TimeUnit.SECONDS), "First event not processed")

        // Verify the first phase
        assertEquals(1, firstPhaseEvents.size)
        assertEquals("Before restart", (firstPhaseEvents[0] as ResilienceTestEvent).data)

        // Simulate shutdown and restart - create new consumer to ensure a clean state
        consumer1.shutdown()

        // Create a fresh consumer instance for restart simulation
        val restartedConsumer = RedisEventConsumer(redisTemplate, serializer, properties)
        val secondLatch = CountDownLatch(1)
        restartedConsumer.registerEventHandler("ResilienceTestEvent") { event ->
            secondPhaseEvents.add(event)
            secondLatch.countDown()
        }

        restartedConsumer.init()

        // Add and process a second event after restart
        val event2 = ResilienceTestEvent(AggregateId(aggregateId), EventVersion(2L), "After restart")
        eventStore.appendToStream(event2, aggregateId, 1)

        restartedConsumer.pollEvents()
        assertTrue(secondLatch.await(3, TimeUnit.SECONDS), "Second event not processed after restart")

        // Verify the second phase
        assertEquals(1, secondPhaseEvents.size)
        assertEquals("After restart", (secondPhaseEvents[0] as ResilienceTestEvent).data)

        // Cleanup
        restartedConsumer.shutdown()

        logger.debug("Successfully handled consumer restart")
        logger.debug("First phase events: {}", firstPhaseEvents.map { (it as ResilienceTestEvent).data })
        logger.debug("Second phase events: {}", secondPhaseEvents.map { (it as ResilienceTestEvent).data })
    }

    @Test
    fun `should handle event handler exceptions gracefully without stopping processing`() {
        val aggregateId = UUID.randomUUID()
        val processedEvents = CopyOnWriteArrayList<String>()
        val latch = CountDownLatch(3) // Expecting 3 events to be processed (2 success + 1 failure)

        // Register a handler that fails on specific events
        consumer1.registerEventHandler("FailingTestEvent") { event ->
            val failingEvent = event as FailingTestEvent
            if (failingEvent.shouldFail) {
                processedEvents.add("Failed: ${failingEvent.data}")
                latch.countDown()
                throw RuntimeException("Simulated handler failure for: ${failingEvent.data}")
            } else {
                processedEvents.add("Success: ${failingEvent.data}")
                latch.countDown()
            }
        }

        consumer1.init()

        // Create events - some that will fail, some that will succeed
        val events = listOf(
            FailingTestEvent(AggregateId(aggregateId), EventVersion(1L), "Event 1", false),
            FailingTestEvent(AggregateId(aggregateId), EventVersion(2L), "Event 2", true), // Will fail
            FailingTestEvent(AggregateId(aggregateId), EventVersion(3L), "Event 3", false)
        )

        eventStore.appendToStream(events, aggregateId, 0)
        consumer1.pollEvents()

        // Wait for processing
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Events were not processed within timeout")

        // Verify that both successful and failed events were attempted
        assertEquals(3, processedEvents.size)
        assertTrue(processedEvents.contains("Success: Event 1"))
        assertTrue(processedEvents.contains("Failed: Event 2"))
        assertTrue(processedEvents.contains("Success: Event 3"))

        logger.debug("Handler exceptions handled gracefully:")
        processedEvents.forEach { logger.debug("Event result: {}", it) }
    }

    // Test event classes
    @Serializable
    data class ResilienceTestEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(UUID.randomUUID()),
        @Transient override val version: EventVersion = EventVersion(0),
        val data: String
    ) : BaseDomainEvent(aggregateId, EventType("ResilienceTestEvent"), version)

    @Serializable
    data class SlowTestEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(UUID.randomUUID()),
        @Transient override val version: EventVersion = EventVersion(0),
        val data: String,
        val processingTimeMs: Long
    ) : BaseDomainEvent(aggregateId, EventType("SlowTestEvent"), version)

    @Serializable
    data class FailingTestEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(UUID.randomUUID()),
        @Transient override val version: EventVersion = EventVersion(0),
        val data: String,
        val shouldFail: Boolean
    ) : BaseDomainEvent(aggregateId, EventType("FailingTestEvent"), version)
}
