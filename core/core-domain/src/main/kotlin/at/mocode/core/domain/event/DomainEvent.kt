package at.mocode.core.domain.event

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

/**
 * Interface for all domain events in the system.
 * Domain events represent something that happened in the domain that domain experts care about.
 */
interface DomainEvent {
    /**
     * Unique identifier for this event instance.
     */
    val eventId: Uuid

    /**
     * Timestamp when the event occurred.
     */
    val timestamp: java.time.Instant

    /**
     * Identifier of the aggregate that the event belongs to.
     */
    val aggregateId: Uuid

    /**
     * Version of the aggregate after the event was applied.
     */
    val version: Long
}

/**
 * Base implementation of the DomainEvent interface.
 * Provides default implementations for common properties.
 */
abstract class BaseDomainEvent(
    override val eventId: Uuid = uuid4(),
    override val timestamp: java.time.Instant = java.time.Instant.now(),
    override val aggregateId: Uuid,
    override val version: Long
) : DomainEvent
