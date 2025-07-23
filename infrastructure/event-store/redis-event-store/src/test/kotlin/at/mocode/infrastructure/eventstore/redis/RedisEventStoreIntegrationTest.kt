package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.EventStore
import at.mocode.infrastructure.eventstore.api.Subscription
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for Redis Event Store.
 *
 * These tests verify the interaction between the Redis Event Store, Event Consumer, and Event Serializer
 * in a more realistic scenario.
 */
@Testcontainers
class RedisEventStoreIntegrationTest {

    companion object {
        @Container
        val redisContainer = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
    }

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var serializer: EventSerializer
    private lateinit var properties: RedisEventStoreProperties
    private lateinit var eventStore: EventStore
    private lateinit var eventConsumer: RedisEventConsumer

    @BeforeEach
    fun setUp() {
        val redisPort = redisContainer.getMappedPort(6379)
        val redisHost = redisContainer.host

        val redisConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        val connectionFactory = LettuceConnectionFactory(redisConfig)
        connectionFactory.afterPropertiesSet()

        redisTemplate = StringRedisTemplate()
        redisTemplate.setConnectionFactory(connectionFactory)
        redisTemplate.afterPropertiesSet()

        serializer = JacksonEventSerializer()

        // Register test event types
        serializer.registerEventType(TestCreatedEvent::class.java, "TestCreated")
        serializer.registerEventType(TestUpdatedEvent::class.java, "TestUpdated")

        properties = RedisEventStoreProperties(
            host = redisHost,
            port = redisPort,
            streamPrefix = "test-stream:",
            allEventsStream = "all-events",
            consumerGroup = "test-group",
            consumerName = "test-consumer",
            createConsumerGroupIfNotExists = true
        )

        eventStore = RedisEventStore(redisTemplate, serializer, properties)
        eventConsumer = RedisEventConsumer(redisTemplate, serializer, properties)

        // Clear all streams
        val keys = redisTemplate.keys("${properties.streamPrefix}*")
        if (keys != null && keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    @AfterEach
    fun tearDown() {
        // Clear all streams
        val keys = redisTemplate.keys("${properties.streamPrefix}*")
        if (keys != null && keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    @Test
    fun `test event publishing and consuming with consumer groups`() {
        // Create an aggregate ID
        val aggregateId = UUID.randomUUID()

        // Create events
        val event1 = TestCreatedEvent(
            aggregateId = aggregateId,
            version = 1,
            name = "Test Entity"
        )

        val event2 = TestUpdatedEvent(
            aggregateId = aggregateId,
            version = 2,
            name = "Updated Test Entity"
        )

        // Set up a latch to wait for events
        val latch = CountDownLatch(2)
        val receivedEvents = mutableListOf<DomainEvent>()

        // Register a handler for TestCreatedEvent
        eventConsumer.registerEventHandler("TestCreated") { event ->
            receivedEvents.add(event)
            latch.countDown()
        }

        // Register a handler for TestUpdatedEvent
        eventConsumer.registerEventHandler("TestUpdated") { event ->
            receivedEvents.add(event)
            latch.countDown()
        }

        // Initialize the consumer
        eventConsumer.init()

        // Append events to the stream
        eventStore.appendToStream(event1, aggregateId, -1)
        eventStore.appendToStream(event2, aggregateId, 1)

        // Manually trigger event polling
        eventConsumer.pollEvents()

        // Wait for events to be processed
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for events")

        // Verify that both events were received
        assertEquals(2, receivedEvents.size)

        // Verify the first event
        val receivedEvent1 = receivedEvents[0] as TestCreatedEvent
        assertEquals(aggregateId, receivedEvent1.aggregateId)
        assertEquals(1, receivedEvent1.version)
        assertEquals("Test Entity", receivedEvent1.name)

        // Verify the second event
        val receivedEvent2 = receivedEvents[1] as TestUpdatedEvent
        assertEquals(aggregateId, receivedEvent2.aggregateId)
        assertEquals(2, receivedEvent2.version)
        assertEquals("Updated Test Entity", receivedEvent2.name)

        // Clean up
        eventConsumer.shutdown()
    }

    @Test
    fun `test event subscription and publishing`() {
        // Create an aggregate ID
        val aggregateId = UUID.randomUUID()

        // Set up a latch to wait for events
        val latch = CountDownLatch(2)
        val receivedEvents = mutableListOf<DomainEvent>()

        // Subscribe to the stream
        val subscription = eventStore.subscribeToStream(aggregateId) { event ->
            receivedEvents.add(event)
            latch.countDown()
        }

        // Create events
        val event1 = TestCreatedEvent(
            aggregateId = aggregateId,
            version = 1,
            name = "Test Entity"
        )

        val event2 = TestUpdatedEvent(
            aggregateId = aggregateId,
            version = 2,
            name = "Updated Test Entity"
        )

        // Append events to the stream
        eventStore.appendToStream(event1, aggregateId, -1)
        eventStore.appendToStream(event2, aggregateId, 1)

        // Wait for events to be received
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for events")

        // Verify that both events were received
        assertEquals(2, receivedEvents.size)

        // Verify the first event
        val receivedEvent1 = receivedEvents[0] as TestCreatedEvent
        assertEquals(aggregateId, receivedEvent1.aggregateId)
        assertEquals(1, receivedEvent1.version)
        assertEquals("Test Entity", receivedEvent1.name)

        // Verify the second event
        val receivedEvent2 = receivedEvents[1] as TestUpdatedEvent
        assertEquals(aggregateId, receivedEvent2.aggregateId)
        assertEquals(2, receivedEvent2.version)
        assertEquals("Updated Test Entity", receivedEvent2.name)

        // Clean up
        subscription.unsubscribe()
    }

    @Test
    fun `test multiple consumers with consumer groups`() {
        // Create an aggregate ID
        val aggregateId = UUID.randomUUID()

        // Create events
        val event1 = TestCreatedEvent(
            aggregateId = aggregateId,
            version = 1,
            name = "Test Entity"
        )

        val event2 = TestUpdatedEvent(
            aggregateId = aggregateId,
            version = 2,
            name = "Updated Test Entity"
        )

        // Set up latches to wait for events
        val latch1 = CountDownLatch(2)
        val latch2 = CountDownLatch(2)
        val receivedEvents1 = mutableListOf<DomainEvent>()
        val receivedEvents2 = mutableListOf<DomainEvent>()

        // Create a second consumer with a different consumer name
        val properties2 = properties.copy(consumerName = "test-consumer-2")
        val eventConsumer2 = RedisEventConsumer(redisTemplate, serializer, properties2)

        // Register handlers for the first consumer
        eventConsumer.registerAllEventsHandler { event ->
            receivedEvents1.add(event)
            latch1.countDown()
        }

        // Register handlers for the second consumer
        eventConsumer2.registerAllEventsHandler { event ->
            receivedEvents2.add(event)
            latch2.countDown()
        }

        // Initialize the consumers
        eventConsumer.init()
        eventConsumer2.init()

        // Append events to the stream
        eventStore.appendToStream(event1, aggregateId, -1)
        eventStore.appendToStream(event2, aggregateId, 1)

        // Manually trigger event polling
        eventConsumer.pollEvents()
        eventConsumer2.pollEvents()

        // Wait for events to be processed by both consumers
        assertTrue(latch1.await(5, TimeUnit.SECONDS), "Timed out waiting for events on consumer 1")
        assertTrue(latch2.await(5, TimeUnit.SECONDS), "Timed out waiting for events on consumer 2")

        // Verify that both consumers received both events
        assertEquals(2, receivedEvents1.size)
        assertEquals(2, receivedEvents2.size)

        // Clean up
        eventConsumer.shutdown()
        eventConsumer2.shutdown()
    }

    // Test event classes
    class TestCreatedEvent(
        override val eventId: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
        override val aggregateId: UUID,
        override val version: Long,
        val name: String
    ) : BaseDomainEvent(eventId, timestamp, aggregateId, version)

    class TestUpdatedEvent(
        override val eventId: UUID = UUID.randomUUID(),
        override val timestamp: Instant = Instant.now(),
        override val aggregateId: UUID,
        override val version: Long,
        val name: String
    ) : BaseDomainEvent(eventId, timestamp, aggregateId, version)
}
