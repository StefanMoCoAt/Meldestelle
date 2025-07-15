package at.mocode.events

import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Base interface for all domain events in the system
 */
@Serializable
sealed interface DomainEvent {
    val eventId: Uuid
    val aggregateId: Uuid
    val eventType: String
    val timestamp: Instant
    val version: Long
}

/**
 * Base class for domain events with common properties
 */
@Serializable
abstract class BaseDomainEvent(
    override val eventId: Uuid,
    override val aggregateId: Uuid,
    override val eventType: String,
    override val timestamp: Instant,
    override val version: Long = 1
) : DomainEvent
