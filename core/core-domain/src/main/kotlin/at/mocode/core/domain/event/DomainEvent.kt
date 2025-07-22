package at.mocode.core.domain.event

import java.time.Instant
import java.util.UUID

/**
 * Interface for all domain events in the system.
 * Domain events represent something that happened in the domain that domain experts care about.
 */
interface DomainEvent {
    /**
     * Unique identifier for this event instance.
     */
    val eventId: UUID

    /**
     * Timestamp when the event occurred.
     */
    val timestamp: Instant

    /**
     * Identifier of the aggregate that the event belongs to.
     */
    val aggregateId: UUID

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
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: Instant = Instant.now(),
    override val aggregateId: UUID,
    override val version: Long
) : DomainEvent
