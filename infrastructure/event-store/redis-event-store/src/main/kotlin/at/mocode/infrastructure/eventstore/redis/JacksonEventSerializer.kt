package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.EventSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Jackson-based implementation of EventSerializer.
 */
class JacksonEventSerializer : EventSerializer {
    private val logger = LoggerFactory.getLogger(JacksonEventSerializer::class.java)

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    // Maps from event type to event class
    private val eventTypeToClass = ConcurrentHashMap<String, Class<out DomainEvent>>()

    // Maps from event class to event type
    private val eventClassToType = ConcurrentHashMap<Class<out DomainEvent>, String>()

    // Standard field names in serialized events
    companion object {
        const val EVENT_TYPE_FIELD = "eventType"
        const val EVENT_ID_FIELD = "eventId"
        const val AGGREGATE_ID_FIELD = "aggregateId"
        const val VERSION_FIELD = "version"
        const val TIMESTAMP_FIELD = "timestamp"
        const val EVENT_DATA_FIELD = "eventData"
    }

    override fun serialize(event: DomainEvent): Map<String, String> {
        val eventType = getEventType(event)

        // Register the event type if not already registered
        if (!eventClassToType.containsKey(event.javaClass)) {
            registerEventType(event.javaClass, eventType)
        }

        // Serialize the event data
        val eventData = objectMapper.writeValueAsString(event)

        // Create a map with the event metadata and data
        return mapOf(
            EVENT_TYPE_FIELD to eventType,
            EVENT_ID_FIELD to event.eventId.toString(),
            AGGREGATE_ID_FIELD to event.aggregateId.toString(),
            VERSION_FIELD to event.version.toString(),
            TIMESTAMP_FIELD to event.timestamp.toString(),
            EVENT_DATA_FIELD to eventData
        )
    }

    override fun deserialize(data: Map<String, String>): DomainEvent {
        val eventType = getEventType(data)
        val eventClass = eventTypeToClass[eventType]
            ?: throw IllegalArgumentException("Unknown event type: $eventType")

        val eventData = data[EVENT_DATA_FIELD]
            ?: throw IllegalArgumentException("Event data is missing")

        return objectMapper.readValue(eventData, eventClass)
    }

    override fun getEventType(event: DomainEvent): String {
        // Use the registered type if available
        val registeredType = eventClassToType[event.javaClass]
        if (registeredType != null) {
            return registeredType
        }

        // Otherwise, use the simple class name
        val type = event.javaClass.simpleName
        registerEventType(event.javaClass, type)
        return type
    }

    override fun getEventType(data: Map<String, String>): String {
        return data[EVENT_TYPE_FIELD]
            ?: throw IllegalArgumentException("Event type is missing")
    }

    override fun registerEventType(eventClass: Class<out DomainEvent>, eventType: String) {
        eventTypeToClass[eventType] = eventClass
        eventClassToType[eventClass] = eventType
        logger.debug("Registered event type: $eventType for class: ${eventClass.name}")
    }

    override fun getAggregateId(data: Map<String, String>): UUID {
        val aggregateIdStr = data[AGGREGATE_ID_FIELD]
            ?: throw IllegalArgumentException("Aggregate ID is missing")

        return UUID.fromString(aggregateIdStr)
    }

    override fun getEventId(data: Map<String, String>): UUID {
        val eventIdStr = data[EVENT_ID_FIELD]
            ?: throw IllegalArgumentException("Event ID is missing")

        return UUID.fromString(eventIdStr)
    }

    override fun getVersion(data: Map<String, String>): Long {
        val versionStr = data[VERSION_FIELD]
            ?: throw IllegalArgumentException("Version is missing")

        return versionStr.toLong()
    }
}
