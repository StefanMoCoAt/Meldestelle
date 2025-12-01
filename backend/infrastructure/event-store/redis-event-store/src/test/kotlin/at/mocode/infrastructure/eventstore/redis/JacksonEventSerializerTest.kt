package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.model.*
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Tests for JacksonEventSerializer - Critical for data integrity.
 */
class JacksonEventSerializerTest {

    private lateinit var serializer: JacksonEventSerializer

    @BeforeEach
    fun setUp() {
        serializer = JacksonEventSerializer()
        // Register test event types
        serializer.registerEventType(ComplexTestEvent::class.java, "ComplexTestEvent")
        serializer.registerEventType(SimpleTestEvent::class.java, "SimpleTestEvent")
    }

    @Test
    fun `should serialize and deserialize simple event correctly`() {
        val aggregateId = Uuid.random()
        val eventId = Uuid.random()
        val timestamp = Clock.System.now()

        val event = SimpleTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            name = "Test Event",
            eventId = EventId(eventId),
            timestamp = timestamp
        )

        val serialized = serializer.serialize(event)
        val deserialized = serializer.deserialize(serialized) as SimpleTestEvent

        assertEquals(event.aggregateId, deserialized.aggregateId)
        assertEquals(event.version, deserialized.version)
        assertEquals(event.name, deserialized.name)
        assertEquals(event.eventId, deserialized.eventId)
        assertEquals(event.timestamp, deserialized.timestamp)
    }

    @Test
    fun `should handle serialization of complex event types with nested objects`() {
        val aggregateId = Uuid.random()
        val eventId = Uuid.random()
        val timestamp = Clock.System.now()
        val correlationId = Uuid.random()

        val event = ComplexTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(5L),
            complexData = ComplexData(
                id = 42,
                name = "Complex Name",
                values = listOf("value1", "value2", "value3"),
                metadata = mapOf("key1" to "value1", "key2" to "value2")
            ),
            eventId = EventId(eventId),
            timestamp = timestamp,
            correlationId = CorrelationId(correlationId)
        )

        val serialized = serializer.serialize(event)
        val deserialized = serializer.deserialize(serialized) as ComplexTestEvent

        assertEquals(event.aggregateId, deserialized.aggregateId)
        assertEquals(event.version, deserialized.version)
        assertEquals(event.complexData.id, deserialized.complexData.id)
        assertEquals(event.complexData.name, deserialized.complexData.name)
        assertEquals(event.complexData.values, deserialized.complexData.values)
        assertEquals(event.complexData.metadata, deserialized.complexData.metadata)
        assertEquals(event.correlationId, deserialized.correlationId)
    }

    @Test
    fun `should throw exception for unregistered event types during deserialization`() {
        val aggregateId = Uuid.random()
        val unregisteredEvent = UnregisteredTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            data = "unregistered data"
        )

        // Serialization should work (auto-registration)
        val serialized = serializer.serialize(unregisteredEvent)

        // Create a new serializer without the event type registered
        val newSerializer = JacksonEventSerializer()

        // Deserialization should fail
        assertThrows<IllegalArgumentException> {
            newSerializer.deserialize(serialized)
        }
    }

    @Test
    fun `should handle null optional values gracefully`() {
        val aggregateId = Uuid.random()
        val event = SimpleTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            name = "Test Event",
            correlationId = null, // Null correlation ID
            causationId = null // Null causation ID
        )

        val serialized = serializer.serialize(event)
        val deserialized = serializer.deserialize(serialized) as SimpleTestEvent

        assertEquals(event.aggregateId, deserialized.aggregateId)
        assertEquals(event.version, deserialized.version)
        assertEquals(event.name, deserialized.name)
        assertNull(deserialized.correlationId)
        assertNull(deserialized.causationId)
    }

    @Test
    fun `should preserve event metadata correctly in serialization`() {
        val aggregateId = Uuid.random()
        val eventId = Uuid.random()
        val timestamp = Clock.System.now()
        val correlationId = Uuid.random()
        val causationId = Uuid.random()

        val event = SimpleTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(3L),
            name = "Metadata Test",
            eventId = EventId(eventId),
            timestamp = timestamp,
            correlationId = CorrelationId(correlationId),
            causationId = CausationId(causationId)
        )

        val serialized = serializer.serialize(event)

        // Verify metadata fields are present in serialized form
        assertEquals("SimpleTestEvent", serialized[JacksonEventSerializer.EVENT_TYPE_FIELD])
        assertEquals(eventId.toString(), serialized[JacksonEventSerializer.EVENT_ID_FIELD])
        assertEquals(aggregateId.toString(), serialized[JacksonEventSerializer.AGGREGATE_ID_FIELD])
        assertEquals("3", serialized[JacksonEventSerializer.VERSION_FIELD])
        assertEquals(timestamp.toString(), serialized[JacksonEventSerializer.TIMESTAMP_FIELD])
        assertNotNull(serialized[JacksonEventSerializer.EVENT_DATA_FIELD])

        // Verify metadata extraction methods work
        assertEquals(aggregateId, serializer.getAggregateId(serialized))
        assertEquals(eventId, serializer.getEventId(serialized))
        assertEquals(3L, serializer.getVersion(serialized))
        assertEquals("SimpleTestEvent", serializer.getEventType(serialized))
    }

    @Test
    fun `should handle missing required metadata fields by throwing exceptions`() {
        val incompleteData = mapOf("someField" to "someValue")

        assertThrows<IllegalArgumentException> {
            serializer.getEventType(incompleteData)
        }

        assertThrows<IllegalArgumentException> {
            serializer.getAggregateId(incompleteData)
        }

        assertThrows<IllegalArgumentException> {
            serializer.getEventId(incompleteData)
        }

        assertThrows<IllegalArgumentException> {
            serializer.getVersion(incompleteData)
        }
    }

    @Test
    fun `should auto-register event types during serialization`() {
        val newSerializer = JacksonEventSerializer()
        val aggregateId = Uuid.random()

        val event = SimpleTestEvent(
            aggregateId = AggregateId(aggregateId),
            version = EventVersion(1L),
            name = "Auto-registration Test"
        )

        // First, serialization should auto-register the event type
        val serialized = newSerializer.serialize(event)

        // Later deserialization should work
        val deserialized = newSerializer.deserialize(serialized) as SimpleTestEvent
        assertEquals(event.name, deserialized.name)
    }

    @Test
    fun `should handle UUID conversion correctly`() {
        val testMap = mapOf(
            JacksonEventSerializer.AGGREGATE_ID_FIELD to "123e4567-e89b-12d3-a456-426614174000",
            JacksonEventSerializer.EVENT_ID_FIELD to "987fcdeb-51a2-43d1-9f12-123456789abc",
            JacksonEventSerializer.VERSION_FIELD to "42"
        )

        val aggregateId = serializer.getAggregateId(testMap)
        val eventId = serializer.getEventId(testMap)
        val version = serializer.getVersion(testMap)

        assertEquals(Uuid.parse("123e4567-e89b-12d3-a456-426614174000"), aggregateId)
        assertEquals(Uuid.parse("987fcdeb-51a2-43d1-9f12-123456789abc"), eventId)
        assertEquals(42L, version)
    }

    @Test
    fun `should throw exception for invalid UUID formats`() {
        val invalidUuidMap = mapOf(
            JacksonEventSerializer.AGGREGATE_ID_FIELD to "invalid-uuid-format",
            JacksonEventSerializer.EVENT_ID_FIELD to "also-invalid",
            JacksonEventSerializer.VERSION_FIELD to "42"
        )

        assertThrows<IllegalArgumentException> {
            serializer.getAggregateId(invalidUuidMap)
        }

        assertThrows<IllegalArgumentException> {
            serializer.getEventId(invalidUuidMap)
        }
    }

    // Test event classes
    data class SimpleTestEvent(
        override val aggregateId: AggregateId,
        override val version: EventVersion,
        val name: String,
        override val eventType: EventType = EventType("SimpleTestEvent"),
        override val eventId: EventId = EventId(Uuid.random()),
        override val timestamp: Instant = Clock.System.now(),
        override val correlationId: CorrelationId? = null,
        override val causationId: CausationId? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)

    data class ComplexTestEvent(
        override val aggregateId: AggregateId,
        override val version: EventVersion,
        val complexData: ComplexData,
        override val eventType: EventType = EventType("ComplexTestEvent"),
        override val eventId: EventId = EventId(Uuid.random()),
        override val timestamp: Instant = Clock.System.now(),
        override val correlationId: CorrelationId? = null,
        override val causationId: CausationId? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)

    data class UnregisteredTestEvent(
        override val aggregateId: AggregateId,
        override val version: EventVersion,
        val data: String,
        override val eventType: EventType = EventType("UnregisteredTestEvent"),
        override val eventId: EventId = EventId(Uuid.random()),
        override val timestamp: Instant = Clock.System.now(),
        override val correlationId: CorrelationId? = null,
        override val causationId: CausationId? = null
    ) : BaseDomainEvent(aggregateId, eventType, version, eventId, timestamp, correlationId, causationId)

    @Serializable
    data class ComplexData(
        val id: Int,
        val name: String,
        val values: List<String>,
        val metadata: Map<String, String>
    )
}
