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
    val eventId: Uuid
    val aggregateId: Uuid
    val eventType: String
    val timestamp: Instant
    val version: Long

    // OPTIMIZED: Added correlation and causation IDs for distributed tracing.
    /**
     * Tracks a chain of events initiated by a single user action across multiple services.
     */
    val correlationId: Uuid?

    /**
     * Tracks the direct cause of this event (the ID of the preceding event or command).
     */
    val causationId: Uuid?
}

/**
 * Abstract base class for domain events to reduce boilerplate code.
 */
abstract class BaseDomainEvent(
    override val aggregateId: Uuid,
    override val eventType: String,
    override val version: Long,
    override val eventId: Uuid = uuid4(),
    override val timestamp: Instant = Clock.System.now(),
    override val correlationId: Uuid? = null,
    override val causationId: Uuid? = null
) : DomainEvent

// ... (DomainEventPublisher and DomainEventHandler interfaces remain the same)
interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: List<DomainEvent>)
}

interface DomainEventHandler<T : DomainEvent> {
    suspend fun handle(event: T)
    fun canHandle(eventType: String): Boolean
}
