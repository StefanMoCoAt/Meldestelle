package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.ConcurrencyException
import at.mocode.infrastructure.eventstore.api.EventSerializer
import at.mocode.infrastructure.eventstore.api.Subscription
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Testcontainers
class RedisEventStoreTest {

    companion object {
        @Container
        val redisContainer = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
    }

    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var serializer: EventSerializer
    private lateinit var properties: RedisEventStoreProperties
    private lateinit var eventStore: RedisEventStore

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
            allEventsStream = "all-events"
        )

        eventStore = RedisEventStore(redisTemplate, serializer, properties)

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
    fun `test append and read events`() {
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

        // Append events
        val version1 = eventStore.appendToStream(event1, aggregateId, -1)
        assertEquals(1, version1)

        val version2 = eventStore.appendToStream(event2, aggregateId, 1)
        assertEquals(2, version2)

        // Read events
        val events = eventStore.readFromStream(aggregateId)
        assertEquals(2, events.size)

        val firstEvent = events[0] as TestCreatedEvent
        assertEquals(aggregateId, firstEvent.aggregateId)
        assertEquals(1, firstEvent.version)
        assertEquals("Test Entity", firstEvent.name)

        val secondEvent = events[1] as TestUpdatedEvent
        assertEquals(aggregateId, secondEvent.aggregateId)
        assertEquals(2, secondEvent.version)
        assertEquals("Updated Test Entity", secondEvent.name)
    }

    @Test
    fun `test append events with concurrency conflict`() {
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

        // Append first event
        val version1 = eventStore.appendToStream(event1, aggregateId, -1)
        assertEquals(1, version1)

        // Try to append second event with wrong expected version
        assertThrows<ConcurrencyException> {
            eventStore.appendToStream(event2, aggregateId, 0)
        }

        // Append second event with correct expected version
        val version2 = eventStore.appendToStream(event2, aggregateId, 1)
        assertEquals(2, version2)
    }

    @Test
    fun `test append multiple events at once`() {
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

        // Append events
        val version = eventStore.appendToStream(listOf(event1, event2), aggregateId, -1)
        assertEquals(2, version)

        // Read events
        val events = eventStore.readFromStream(aggregateId)
        assertEquals(2, events.size)
    }

    @Test
    fun `test read all events`() {
        val aggregate1Id = UUID.randomUUID()
        val aggregate2Id = UUID.randomUUID()

        // Create events for first aggregate
        val event1 = TestCreatedEvent(
            aggregateId = aggregate1Id,
            version = 1,
            name = "Test Entity 1"
        )

        val event2 = TestUpdatedEvent(
            aggregateId = aggregate1Id,
            version = 2,
            name = "Updated Test Entity 1"
        )

        // Create events for second aggregate
        val event3 = TestCreatedEvent(
            aggregateId = aggregate2Id,
            version = 1,
            name = "Test Entity 2"
        )

        // Append events
        eventStore.appendToStream(event1, aggregate1Id, -1)
        eventStore.appendToStream(event2, aggregate1Id, 1)
        eventStore.appendToStream(event3, aggregate2Id, -1)

        // Read all events
        val allEvents = eventStore.readAllEvents()
        assertEquals(3, allEvents.size)
    }

    @Test
    fun `test subscribe to stream`() {
        val aggregateId = UUID.randomUUID()
        val latch = CountDownLatch(2)
        val receivedEvents = mutableListOf<DomainEvent>()

        // Subscribe to stream
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

        // Append events
        eventStore.appendToStream(event1, aggregateId, -1)
        eventStore.appendToStream(event2, aggregateId, 1)

        // Wait for events to be received
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(2, receivedEvents.size)

        // Unsubscribe
        subscription.unsubscribe()
        assertFalse(subscription.isActive())
    }

    @Test
    fun `test subscribe to all events`() {
        val aggregate1Id = UUID.randomUUID()
        val aggregate2Id = UUID.randomUUID()
        val latch = CountDownLatch(3)
        val receivedEvents = mutableListOf<DomainEvent>()

        // Subscribe to all events
        val subscription = eventStore.subscribeToAll { event ->
            receivedEvents.add(event)
            latch.countDown()
        }

        // Create events for first aggregate
        val event1 = TestCreatedEvent(
            aggregateId = aggregate1Id,
            version = 1,
            name = "Test Entity 1"
        )

        val event2 = TestUpdatedEvent(
            aggregateId = aggregate1Id,
            version = 2,
            name = "Updated Test Entity 1"
        )

        // Create events for second aggregate
        val event3 = TestCreatedEvent(
            aggregateId = aggregate2Id,
            version = 1,
            name = "Test Entity 2"
        )

        // Append events
        eventStore.appendToStream(event1, aggregate1Id, -1)
        eventStore.appendToStream(event2, aggregate1Id, 1)
        eventStore.appendToStream(event3, aggregate2Id, -1)

        // Wait for events to be received
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(3, receivedEvents.size)

        // Unsubscribe
        subscription.unsubscribe()
        assertFalse(subscription.isActive())
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
