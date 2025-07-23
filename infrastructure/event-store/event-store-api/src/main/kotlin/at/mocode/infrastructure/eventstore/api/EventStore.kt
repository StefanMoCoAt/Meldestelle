package at.mocode.infrastructure.eventstore.api

import at.mocode.core.domain.event.DomainEvent
import java.util.UUID

/**
 * Interface for an event store that persists domain events.
 */
interface EventStore {
    /**
     * Appends an event to the event store.
     *
     * @param event The event to append
     * @param streamId The ID of the event stream (typically the aggregate ID)
     * @param expectedVersion The expected version of the stream (for optimistic concurrency)
     * @return The new version of the stream
     * @throws ConcurrencyException if the expected version doesn't match the actual version
     */
    fun appendToStream(event: DomainEvent, streamId: UUID, expectedVersion: Long): Long

    /**
     * Appends multiple events to the event store.
     *
     * @param events The events to append
     * @param streamId The ID of the event stream (typically the aggregate ID)
     * @param expectedVersion The expected version of the stream (for optimistic concurrency)
     * @return The new version of the stream
     * @throws ConcurrencyException if the expected version doesn't match the actual version
     */
    fun appendToStream(events: List<DomainEvent>, streamId: UUID, expectedVersion: Long): Long

    /**
     * Reads events from a stream.
     *
     * @param streamId The ID of the event stream to read from
     * @param fromVersion The version to start reading from (inclusive)
     * @param toVersion The version to read to (inclusive), or null to read all events
     * @return The events in the stream
     */
    fun readFromStream(streamId: UUID, fromVersion: Long = 0, toVersion: Long? = null): List<DomainEvent>

    /**
     * Reads all events from all streams.
     *
     * @param fromPosition The position to start reading from (inclusive)
     * @param maxCount The maximum number of events to read, or null to read all events
     * @return The events in all streams
     */
    fun readAllEvents(fromPosition: Long = 0, maxCount: Int? = null): List<DomainEvent>

    /**
     * Gets the current version of a stream.
     *
     * @param streamId The ID of the event stream
     * @return The current version of the stream, or -1 if the stream doesn't exist
     */
    fun getStreamVersion(streamId: UUID): Long

    /**
     * Subscribes to events from a specific stream.
     *
     * @param streamId The ID of the event stream to subscribe to
     * @param fromVersion The version to start subscribing from (inclusive)
     * @param handler The handler to call for each event
     * @return A subscription that can be used to unsubscribe
     */
    fun subscribeToStream(streamId: UUID, fromVersion: Long = 0, handler: (DomainEvent) -> Unit): Subscription

    /**
     * Subscribes to all events from all streams.
     *
     * @param fromPosition The position to start subscribing from (inclusive)
     * @param handler The handler to call for each event
     * @return A subscription that can be used to unsubscribe
     */
    fun subscribeToAll(fromPosition: Long = 0, handler: (DomainEvent) -> Unit): Subscription
}

/**
 * Interface for a subscription to an event stream.
 */
interface Subscription {
    /**
     * Unsubscribes from the event stream.
     */
    fun unsubscribe()

    /**
     * Checks if the subscription is active.
     *
     * @return true if the subscription is active, false otherwise
     */
    fun isActive(): Boolean
}

/**
 * Exception thrown when there is a concurrency conflict in the event store.
 */
class ConcurrencyException(message: String) : RuntimeException(message)
