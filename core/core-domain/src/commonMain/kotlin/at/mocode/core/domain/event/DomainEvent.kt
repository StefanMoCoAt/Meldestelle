package at.mocode.core.domain.event

import at.mocode.core.domain.model.*
import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

/**
 * Basis-Interface für alle Domänen-Events im System.
 * Ein Domänen-Event repräsentiert etwas fachlich Bedeutsames, das passiert ist.
 */
interface DomainEvent {
    val eventId: EventId
    val aggregateId: AggregateId
    val eventType: EventType
    val timestamp: Instant
    val version: EventVersion
    val correlationId: CorrelationId?
    val causationId: CausationId?
}

/**
 * Abstrakte Basisklasse für Domänen-Events, um Boilerplate-Code zu reduzieren.
 */
@Serializable
@OptIn(ExperimentalTime::class)
abstract class BaseDomainEvent(
    override val aggregateId: AggregateId,
    override val eventType: EventType,
    override val version: EventVersion,
    override val eventId: EventId = EventId(uuid4()),
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant = Clock.System.now(),
    override val correlationId: CorrelationId? = null,
    override val causationId: CausationId? = null
) : DomainEvent

/**
 * Interface für einen Publisher, der Domänen-Events veröffentlichen kann.
 */
interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: List<DomainEvent>)
}

/**
 * Interface für einen Handler, der auf bestimmte Domänen-Events reagieren kann.
 */
interface DomainEventHandler<T : DomainEvent> {
    suspend fun handle(event: T)
    fun canHandle(eventType: String): Boolean
}
