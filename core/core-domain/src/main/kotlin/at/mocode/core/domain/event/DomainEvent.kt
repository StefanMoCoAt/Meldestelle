package at.mocode.core.domain.event

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Base interface for all domain events in the system.
 * A domain event represents something significant that has happened in a specific domain.
 */
interface DomainEvent {

    /**
     * Unique identifier for this event instance.
     */
    val eventId: Uuid

    /**
     * Identifier of the aggregate that the event belongs to.
     */
    val aggregateId: Uuid

    val eventType: String

    /**
     * Timestamp when the event occurred.
     */
    val timestamp: Instant

    /**
     * Version of the aggregate after the event was applied.
     */
    val version: Long
}

/**
 * Abstract base class for domain events to reduce boilerplate code.
 */
abstract class BaseDomainEvent(
    override val aggregateId: Uuid,

    override val eventType: String,

    override val version: Long,

    override val eventId: Uuid = uuid4(),

    override val timestamp: Instant = Clock.System.now()


) : DomainEvent

/**
 * Interface for a component that can publish domain events, typically to a message bus like Kafka.
 */
interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: List<DomainEvent>)
}

/**
 * Interface for a component that can handle (react to) a specific type of domain event.
 */
interface DomainEventHandler<T : DomainEvent> {
    suspend fun handle(event: T)
    fun canHandle(eventType: String): Boolean
}
