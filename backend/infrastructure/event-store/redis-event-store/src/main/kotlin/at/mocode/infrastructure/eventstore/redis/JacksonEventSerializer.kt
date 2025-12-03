@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package at.mocode.infrastructure.eventstore.redis

import at.mocode.core.domain.event.DomainEvent
import at.mocode.infrastructure.eventstore.api.EventSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

/**
 * Jackson-basierte Implementierung des EventSerializer.
 */
class JacksonEventSerializer : EventSerializer {
    private val logger = LoggerFactory.getLogger(JacksonEventSerializer::class.java)

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(kotlinModule())
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    private val eventTypeToClass = ConcurrentHashMap<String, Class<out DomainEvent>>()
    private val eventClassToType = ConcurrentHashMap<Class<out DomainEvent>, String>()

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
        if (!eventClassToType.containsKey(event.javaClass)) {
            registerEventType(event.javaClass, eventType)
        }

        val eventData = objectMapper.writeValueAsString(event)
        return mapOf(
            EVENT_TYPE_FIELD to eventType,
            EVENT_ID_FIELD to event.eventId.value.toString(),
            AGGREGATE_ID_FIELD to event.aggregateId.value.toString(),
            VERSION_FIELD to event.version.value.toString(),
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
        return eventClassToType[event.javaClass] ?: event.javaClass.simpleName
    }

    override fun getEventType(data: Map<String, String>): String {
        return data[EVENT_TYPE_FIELD] ?: throw IllegalArgumentException("Event type is missing")
    }

    // KORRIGIERT: Parameterreihenfolge umgedreht
    override fun registerEventType(eventClass: Class<out DomainEvent>, eventType: String) {
        eventTypeToClass[eventType] = eventClass
        eventClassToType[eventClass] = eventType
        logger.debug("Registered event type: {} for class: {}", eventType, eventClass.name)
    }

    override fun getAggregateId(data: Map<String, String>): Uuid {
        val aggregateIdStr = data[AGGREGATE_ID_FIELD]
            ?: throw IllegalArgumentException("Aggregate ID is missing")
        return Uuid.parse(aggregateIdStr)
    }

    override fun getEventId(data: Map<String, String>): Uuid {
        val eventIdStr = data[EVENT_ID_FIELD]
            ?: throw IllegalArgumentException("Event ID is missing")
        return Uuid.parse(eventIdStr)
    }

    override fun getVersion(data: Map<String, String>): Long {
        val versionStr = data[VERSION_FIELD]
            ?: throw IllegalArgumentException("Version is missing")
        return versionStr.toLong()
    }
}
