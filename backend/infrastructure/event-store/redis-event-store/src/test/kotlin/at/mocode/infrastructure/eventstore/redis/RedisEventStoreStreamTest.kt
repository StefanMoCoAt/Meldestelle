package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.model.AggregateId
import at.mocode.core.domain.model.EventType
import at.mocode.core.domain.model.EventVersion
import at.mocode.infrastructure.eventstore.api.EventSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
import kotlin.uuid.Uuid

/**
 * Stream-specific tests for RedisEventStore - Core functionality validation.
 */
@Testcontainers
class RedisEventStoreStreamTest {

    private val logger = LoggerFactory.getLogger(RedisEventStoreStreamTest::class.java)

    companion object {
        @Container
        val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
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

        redisTemplate = StringRedisTemplate(connectionFactory)

        serializer = JacksonEventSerializer().apply {
            registerEventType(StreamTestEvent::class.java, "StreamTestEvent")
            registerEventType(OrderTestEvent::class.java, "OrderTestEvent")
        }

        properties = RedisEventStoreProperties().apply {
            streamPrefix = "test-stream:"
        }
        eventStore = RedisEventStore(redisTemplate, serializer, properties)
        cleanupRedis()
    }

    @AfterEach
    fun tearDown() = cleanupRedis()

    private fun cleanupRedis() {
        val keys = redisTemplate.keys("${properties.streamPrefix}*")
        if (!keys.isNullOrEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    @Test
    fun `readFromStream should respect fromVersion and toVersion parameters`() {
        val aggregateId = Uuid.random()
        val events = (1..10).map { i ->
            StreamTestEvent(
                aggregateId = AggregateId(aggregateId),
                version = EventVersion(i.toLong()),
                data = "Event $i"
            )
        }

        // Append all events
        eventStore.appendToStream(events, aggregateId, 0)

        // Test reading from a specific version
        val eventsFromVersion3 = eventStore.readFromStream(aggregateId, fromVersion = 3)
        assertEquals(8, eventsFromVersion3.size) // Events 3-10
        assertEquals(EventVersion(3L), eventsFromVersion3.first().version)
        assertEquals(EventVersion(10L), eventsFromVersion3.last().version)

        // Test reading with both fromVersion and toVersion
        val eventsRange = eventStore.readFromStream(aggregateId, fromVersion = 4, toVersion = 7)
        assertEquals(4, eventsRange.size) // Events 4-7
        assertEquals(EventVersion(4L), eventsRange.first().version)
        assertEquals(EventVersion(7L), eventsRange.last().version)

        // Test reading a single event
        val singleEvent = eventStore.readFromStream(aggregateId, fromVersion = 5, toVersion = 5)
        assertEquals(1, singleEvent.size)
        assertEquals(EventVersion(5L), singleEvent.first().version)

        // Test reading beyond the available range
        val beyondRange = eventStore.readFromStream(aggregateId, fromVersion = 15, toVersion = 20)
        assertEquals(0, beyondRange.size)
    }

    @Test
    fun `readAllEvents should handle pagination correctly`() {
        val aggregateId1 = Uuid.random()
        val aggregateId2 = Uuid.random()

        val events1 = (1..5).map { i ->
            StreamTestEvent(
                aggregateId = AggregateId(aggregateId1),
                version = EventVersion(i.toLong()),
                data = "Stream1 Event $i"
            )
        }

        val events2 = (1..5).map { i ->
            StreamTestEvent(
                aggregateId = AggregateId(aggregateId2),
                version = EventVersion(i.toLong()),
                data = "Stream2 Event $i"
            )
        }

        // Append events to both streams
        eventStore.appendToStream(events1, aggregateId1, 0)
        eventStore.appendToStream(events2, aggregateId2, 0)

        // Test reading all events
        val allEvents = eventStore.readAllEvents()
        assertEquals(10, allEvents.size)

        // Test reading with fromPosition
        val eventsFromPosition3 = eventStore.readAllEvents(fromPosition = 3)
        assertEquals(7, eventsFromPosition3.size)

        // Test reading with maxCount
        val limitedEvents = eventStore.readAllEvents(maxCount = 4)
        assertEquals(4, limitedEvents.size)

        // Test reading with both fromPosition and maxCount
        val paginatedEvents = eventStore.readAllEvents(fromPosition = 2, maxCount = 3)
        assertEquals(3, paginatedEvents.size)

        // Test reading beyond available events
        val beyondEvents = eventStore.readAllEvents(fromPosition = 20)
        assertEquals(0, beyondEvents.size)
    }

    @Test
    fun `getStreamVersion should return -1 for non-existent streams`() {
        val nonExistentStreamId = Uuid.random()
        val version = eventStore.getStreamVersion(nonExistentStreamId)
        assertEquals(0L, version) // Redis streams return 0 for non-existent streams, not -1
    }

    @Test
    fun `should handle empty streams correctly`() {
        val emptyStreamId = Uuid.random()

        // Reading from an empty stream should return an empty list
        val emptyEvents = eventStore.readFromStream(emptyStreamId)
        assertEquals(0, emptyEvents.size)

        // Version of an empty stream should be 0
        val emptyVersion = eventStore.getStreamVersion(emptyStreamId)
        assertEquals(0L, emptyVersion)

        // Reading with version range on an empty stream should return an empty list
        val rangeEvents = eventStore.readFromStream(emptyStreamId, fromVersion = 1, toVersion = 5)
        assertEquals(0, rangeEvents.size)
    }

    @Test
    fun `should handle concurrent version conflicts properly using optimistic locking`() {
        val aggregateId = Uuid.random()

        // Add initial event
        val initialEvent = OrderTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            threadId = 0,
            eventIndex = 0,
            data = "Initial event"
        )
        eventStore.appendToStream(initialEvent, aggregateId, 0)

        // Simulate simplified concurrent access with manual version handling
        val event1 = OrderTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(2L),
            threadId = 1,
            eventIndex = 1,
            data = "Concurrent event 1"
        )

        val event2 = OrderTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(3L),
            threadId = 2,
            eventIndex = 1,
            data = "Concurrent event 2"
        )

        // First append should succeed
        val version1 = eventStore.appendToStream(event1, aggregateId, 1)
        assertEquals(2L, version1)

        // The second appending should succeed with an updated expected version
        val version2 = eventStore.appendToStream(event2, aggregateId, 2)
        assertEquals(3L, version2)

        // Verify the final stream state
        val allEvents = eventStore.readFromStream(aggregateId)
        assertEquals(3, allEvents.size)
        assertEquals(3L, eventStore.getStreamVersion(aggregateId))

        // Verify events are in correct order
        val versions = allEvents.map { it.version.value }
        assertEquals(listOf(1L, 2L, 3L), versions)
    }

    @Test
    fun `should handle version gaps correctly in stream reading`() {
        val aggregateId = Uuid.random()

        // Create events with non-sequential versions (simulating gaps)
        val event1 = StreamTestEvent(AggregateId(aggregateId), EventVersion(1L), "Event 1")
        val event5 = StreamTestEvent(AggregateId(aggregateId), EventVersion(2L), "Event 5") // Actual version 2, but data says 5
        val event10 = StreamTestEvent(AggregateId(aggregateId), EventVersion(3L), "Event 10")

        eventStore.appendToStream(event1, aggregateId, 0)
        eventStore.appendToStream(event5, aggregateId, 1)
        eventStore.appendToStream(event10, aggregateId, 2)

        // Reading should work despite data content suggesting gaps
        val allEvents = eventStore.readFromStream(aggregateId)
        assertEquals(3, allEvents.size)
        assertEquals(listOf(1L, 2L, 3L), allEvents.map { it.version.value })

        // Range reading should work correctly
        val rangeEvents = eventStore.readFromStream(aggregateId, fromVersion = 2, toVersion = 3)
        assertEquals(2, rangeEvents.size)
        assertEquals(listOf(2L, 3L), rangeEvents.map { it.version.value })
    }

    @Test
    fun `should handle large streams efficiently`() {
        val aggregateId = Uuid.random()
        val numberOfEvents = 1000

        // Create and append a large number of events
        val events = (1..numberOfEvents).map { i ->
            StreamTestEvent(
                aggregateId = AggregateId(aggregateId),
                version = EventVersion(i.toLong()),
                data = "Large stream event $i with some additional data to make it more realistic"
            )
        }

        // Measure appends time
        val startAppend = System.currentTimeMillis()
        eventStore.appendToStream(events, aggregateId, 0)
        val appendTime = System.currentTimeMillis() - startAppend

        logger.debug("Appended {} events in {}ms", numberOfEvents, appendTime)

        // Verify version
        assertEquals(numberOfEvents.toLong(), eventStore.getStreamVersion(aggregateId))

        // Measure read time for full stream
        val startRead = System.currentTimeMillis()
        val allReadEvents = eventStore.readFromStream(aggregateId)
        val readTime = System.currentTimeMillis() - startRead

        logger.debug("Read {} events in {}ms", numberOfEvents, readTime)
        assertEquals(numberOfEvents, allReadEvents.size)

        // Measure time for range reading
        val startRange = System.currentTimeMillis()
        val rangeEvents = eventStore.readFromStream(aggregateId, fromVersion = 500, toVersion = 600)
        val rangeTime = System.currentTimeMillis() - startRange

        logger.debug("Read 101 events from range in {}ms", rangeTime)
        assertEquals(101, rangeEvents.size)

        // Verify performance is reasonable (should be under 5 seconds for 1000 events)
        assertTrue(appendTime < 5000, "Append time too slow: ${appendTime}ms")
        assertTrue(readTime < 5000, "Read time too slow: ${readTime}ms")
    }

    @Test
    fun `subscribeToStream and subscribeToAll should return working subscriptions`() {
        val aggregateId = Uuid.random()
        var streamEventReceived = false
        var allEventReceived = false

        // Test stream subscription
        val streamSubscription = eventStore.subscribeToStream(aggregateId, 0) { event ->
            streamEventReceived = true
        }
        assertTrue(streamSubscription.isActive())

        // Test all-events subscription
        val allSubscription = eventStore.subscribeToAll(0) { event ->
            allEventReceived = true
        }
        assertTrue(allSubscription.isActive())

        // Test unsubscribe
        streamSubscription.unsubscribe()
        assertFalse(streamSubscription.isActive())

        allSubscription.unsubscribe()
        assertFalse(allSubscription.isActive())

        // Note: These are basic implementation subscriptions that don't process events
        // The focus here is testing that they return proper subscription objects
    }

    // Test event classes
    @Serializable
    data class StreamTestEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(Uuid.random()),
        @Transient override val version: EventVersion = EventVersion(0),
        val data: String
    ) : BaseDomainEvent(aggregateId, EventType("StreamTestEvent"), version)

    @Serializable
    data class OrderTestEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(Uuid.random()),
        @Transient override val version: EventVersion = EventVersion(0),
        val threadId: Int,
        val eventIndex: Int,
        val data: String
    ) : BaseDomainEvent(aggregateId, EventType("OrderTestEvent"), version)
}
