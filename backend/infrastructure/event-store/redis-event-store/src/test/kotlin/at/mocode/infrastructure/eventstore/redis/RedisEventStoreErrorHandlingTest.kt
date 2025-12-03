package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.model.AggregateId
import at.mocode.core.domain.model.EventType
import at.mocode.core.domain.model.EventVersion
import at.mocode.infrastructure.eventstore.api.ConcurrencyException
import at.mocode.infrastructure.eventstore.api.EventSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Simplified error handling tests for RedisEventStore using Testcontainers.
 * Tests real scenarios without complex mocking.
 */
@Testcontainers
class RedisEventStoreErrorHandlingTest {

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
            registerEventType(TestErrorEvent::class.java, "TestErrorEvent")
            registerEventType(LargePayloadEvent::class.java, "LargePayloadEvent")
            registerEventType(ComplexErrorEvent::class.java, "ComplexErrorEvent")
        }

        properties = RedisEventStoreProperties().apply {
            streamPrefix = "test-stream:"
            allEventsStream = "all-events"
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
    fun `should handle large event payloads correctly without memory issues`() {
        val aggregateId = Uuid.random()

        // Create an event with a very large payload (1MB)
        val largeData = "X".repeat(1024 * 1024) // 1MB of data
        val largeMetadata = (1..1000).associate { "key$it" to "value$it".repeat(100) } // Additional large metadata

        val largeEvent = LargePayloadEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            largeData = largeData,
            metadata = largeMetadata
        )

        // Should handle serialization and storage of large payloads without exception
        assertDoesNotThrow {
            val version = eventStore.appendToStream(largeEvent, aggregateId, 0)
            assertEquals(1L, version)
        }

        // Should be able to read back the large event correctly
        val retrievedEvents = eventStore.readFromStream(aggregateId)
        assertEquals(1, retrievedEvents.size)

        val retrievedEvent = retrievedEvents[0] as LargePayloadEvent
        assertEquals(largeData, retrievedEvent.largeData)
        assertEquals(largeMetadata, retrievedEvent.metadata)
        assertEquals(EventVersion(1L), retrievedEvent.version)
    }

    @Test
    fun `should handle multiple large events in sequence`() {
        val aggregateId = Uuid.random()
        val numberOfLargeEvents = 10
        val sizePerEvent = 100 * 1024 // 100KB per event

        // Create multiple large events
        val largeEvents = (1..numberOfLargeEvents).map { i ->
            LargePayloadEvent(
                aggregateId = AggregateId(aggregateId),
                version = EventVersion(i.toLong()),
                largeData = "Event$i-".repeat(sizePerEvent / 10),
                metadata = mapOf("eventNumber" to "$i", "size" to "$sizePerEvent")
            )
        }

        // Append all large events
        assertDoesNotThrow {
            eventStore.appendToStream(largeEvents, aggregateId, 0)
        }

        // Verify all events can be retrieved
        val allEvents = eventStore.readFromStream(aggregateId)
        assertEquals(numberOfLargeEvents, allEvents.size)

        // Verify each event's integrity
        allEvents.forEachIndexed { index, event ->
            val largeEvent = event as LargePayloadEvent
            assertEquals(EventVersion((index + 1).toLong()), largeEvent.version)
            assertTrue(largeEvent.largeData.startsWith("Event${index + 1}-"))
            assertEquals("${index + 1}", largeEvent.metadata["eventNumber"])
        }
    }

    @Test
    fun `should handle corrupted data gracefully during deserialization by skipping bad events`() {
        val aggregateId = Uuid.random()
        val streamKey = "test-stream:$aggregateId"

        // First, add a valid event
        val validEvent = TestErrorEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            data = "valid event"
        )
        eventStore.appendToStream(validEvent, aggregateId, 0)

        // Manually corrupt data in Redis by adding malformed JSON
        val corruptedEventData = mapOf(
            "eventType" to "TestErrorEvent",
            "eventData" to "{\"corrupted\":\"json\",\"missing\":", // Invalid JSON - missing closing brace
            "aggregateId" to aggregateId.toString(),
            "version" to "2",
            "eventId" to Uuid.random().toString(),
            "timestamp" to Clock.System.now().toString()
        )

        // Directly add corrupted data to the Redis stream
        redisTemplate.opsForStream<String, String>().add(streamKey, corruptedEventData)

        // Add another valid event after the corrupted one
        val validEvent2 = TestErrorEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(3L),
            data = "another valid event"
        )
        eventStore.appendToStream(validEvent2, aggregateId, 2)

        // Reading should skip corrupted events and return only valid ones
        val events = eventStore.readFromStream(aggregateId)

        // Should return only the valid events (corrupted event should be skipped)
        assertEquals(2, events.size)

        val firstEvent = events[0] as TestErrorEvent
        assertEquals("valid event", firstEvent.data)
        assertEquals(EventVersion(1L), firstEvent.version)

        val secondEvent = events[1] as TestErrorEvent
        assertEquals("another valid event", secondEvent.data)
        assertEquals(EventVersion(3L), secondEvent.version)
    }

    @Test
    fun `should handle unregistered event types gracefully during read operations`() {
        val aggregateId = Uuid.random()
        val streamKey = "test-stream:$aggregateId"

        // Add a valid registered event first
        val validEvent = TestErrorEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            data = "valid registered event"
        )
        eventStore.appendToStream(validEvent, aggregateId, 0)

        // Manually add event data for an unregistered event type
        val unregisteredEventData = mapOf(
            "eventType" to "UnknownEventType", // Not registered in serializer
            "eventData" to """{"someField": "someValue", "aggregateId": {"value": "$aggregateId"}, "version": {"value": 2}}""",
            "aggregateId" to aggregateId.toString(),
            "version" to "2",
            "eventId" to Uuid.random().toString(),
            "timestamp" to Clock.System.now().toString()
        )

        redisTemplate.opsForStream<String, String>().add(streamKey, unregisteredEventData)

        // Add another valid event
        val validEvent2 = TestErrorEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(3L),
            data = "final valid event"
        )
        eventStore.appendToStream(validEvent2, aggregateId, 2)

        // Reading should skip unregistered events and return only valid ones
        val events = eventStore.readFromStream(aggregateId)

        assertEquals(2, events.size)
        assertEquals("valid registered event", (events[0] as TestErrorEvent).data)
        assertEquals("final valid event", (events[1] as TestErrorEvent).data)
    }

    @Test
    fun `should handle concurrent version conflicts properly with retry logic`() {
        val aggregateId = Uuid.random()

        // Create an initial event
        val event1 = TestErrorEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            data = "initial event"
        )
        eventStore.appendToStream(event1, aggregateId, 0)

        // Try to append two events with the same expected version (simulating concurrent access)
        val event2 = TestErrorEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(2L),
            data = "concurrent event 1"
        )

        val event3 = TestErrorEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(2L), // Same version - will conflict
            data = "concurrent event 2"
        )

        // First append should succeed
        val version2 = eventStore.appendToStream(event2, aggregateId, 1)
        assertEquals(2L, version2)

        // Second append with the same expected version should fail
        assertThrows<ConcurrencyException> {
            eventStore.appendToStream(event3, aggregateId, 1) // Still expecting version 1
        }

        // But should succeed with a correct expected version
        val correctedEvent3 = event3.copy(version = EventVersion(3L))
        val version3 = eventStore.appendToStream(correctedEvent3, aggregateId, 2)
        assertEquals(3L, version3)

        // Verify all events are in the stream
        val allEvents = eventStore.readFromStream(aggregateId)
        assertEquals(3, allEvents.size)
        assertEquals(listOf(1L, 2L, 3L), allEvents.map { it.version.value })
    }

    @Test
    fun `should handle complex nested object serialization correctly`() {
        val aggregateId = Uuid.random()

        val complexEvent = ComplexErrorEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            nestedData = ComplexNestedData(
                id = 42,
                name = "Complex Test",
                subObjects = listOf(
                    SubObject("sub1", 1, mapOf("key1" to "value1")),
                    SubObject("sub2", 2, mapOf("key2" to "value2", "key3" to "value3"))
                ),
                metadata = mapOf(
                    "level1" to mapOf("level2" to mapOf("level3" to "deep value")),
                    "array" to listOf("item1", "item2", "item3")
                )
            )
        )

        // Should handle complex serialization without issues
        assertDoesNotThrow {
            eventStore.appendToStream(complexEvent, aggregateId, 0)
        }

        // Should deserialize a complex object correctly
        val retrievedEvents = eventStore.readFromStream(aggregateId)
        assertEquals(1, retrievedEvents.size)

        val retrievedEvent = retrievedEvents[0] as ComplexErrorEvent
        assertEquals(42, retrievedEvent.nestedData.id)
        assertEquals("Complex Test", retrievedEvent.nestedData.name)
        assertEquals(2, retrievedEvent.nestedData.subObjects.size)
        assertEquals("sub1", retrievedEvent.nestedData.subObjects[0].name)
        assertEquals(2, retrievedEvent.nestedData.subObjects[1].value)
        assertTrue(retrievedEvent.nestedData.metadata.containsKey("level1"))
    }

    // Test event classes
    @Serializable
    data class TestErrorEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(Uuid.random()),
        @Transient override val version: EventVersion = EventVersion(0),
        val data: String
    ) : BaseDomainEvent(aggregateId, EventType("TestErrorEvent"), version)

    @Serializable
    data class LargePayloadEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(Uuid.random()),
        @Transient override val version: EventVersion = EventVersion(0),
        val largeData: String,
        val metadata: Map<String, String>
    ) : BaseDomainEvent(aggregateId, EventType("LargePayloadEvent"), version)

    @Serializable
    data class ComplexErrorEvent(
        @Transient override val aggregateId: AggregateId = AggregateId(Uuid.random()),
        @Transient override val version: EventVersion = EventVersion(0),
        val nestedData: ComplexNestedData
    ) : BaseDomainEvent(aggregateId, EventType("ComplexErrorEvent"), version)

    @Serializable
    data class ComplexNestedData(
        val id: Int,
        val name: String,
        val subObjects: List<SubObject>,
        val metadata: Map<String, Any>
    )

    @Serializable
    data class SubObject(
        val name: String,
        val value: Int,
        val properties: Map<String, String>
    )
}
