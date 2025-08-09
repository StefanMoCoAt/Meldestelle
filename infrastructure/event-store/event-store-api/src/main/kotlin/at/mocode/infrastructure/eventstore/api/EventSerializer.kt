package at.mocode.infrastructure.eventstore.api

import at.mocode.core.domain.event.DomainEvent
import java.util.UUID

/**
 * Interface for serializing and deserializing domain events.
 */
interface EventSerializer {
    /**
     * Serializes a domain event to a map of strings to strings.
     * This format is suitable for storage in Redis Streams.
     *
     * @param event The event to serialize
     * @return A map of strings to strings representing the event
     */
    fun serialize(event: DomainEvent): Map<String, String>

    /**
     * Deserializes a map of strings to a domain event.
     *
     * @param data The map of strings to deserialize
     * @return The deserialized domain event
     */
    fun deserialize(data: Map<String, String>): DomainEvent

    /**
     * Gets the type of domain event.
     * This is used to determine the type of event when deserializing.
     *
     * @param event The event to get the type of
     * @return The type of the event as a string
     */
    fun getEventType(event: DomainEvent): String

    /**
     * Gets the type of domain event from a serialized map.
     *
     * @param data The serialized event data
     * @return The type of the event as a string
     */
    fun getEventType(data: Map<String, String>): String

    /**
     * Registers a domain event class with the serializer.
     * This is used to map event types to their corresponding classes.
     *
     * @param eventClass The class of the event to register
     * @param eventType The type of the event as a string
     */
    fun registerEventType(eventClass: Class<out DomainEvent>, eventType: String)

    /**
     * Gets the aggregate ID from a serialized event.
     *
     * @param data The serialized event data
     * @return The aggregate ID
     */
    fun getAggregateId(data: Map<String, String>): UUID

    /**
     * Gets the event ID from a serialized event.
     *
     * @param data The serialized event data
     * @return The event ID
     */
    fun getEventId(data: Map<String, String>): UUID

    /**
     * Gets the version from a serialized event.
     *
     * @param data The serialized event data
     * @return The version
     */
    fun getVersion(data: Map<String, String>): Long
}
