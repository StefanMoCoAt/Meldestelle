@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.core.domain.event

import at.mocode.core.domain.model.*
import at.mocode.core.domain.serialization.KotlinInstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Basis-Interface für alle Domain-Events im System.
 * Ein Domain-Event beschreibt ein fachlich relevantes Ereignis, das stattgefunden hat.
 */
@OptIn(ExperimentalTime::class)
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
 * Abstrakte Basisklasse für Domain-Events, um Boilerplate zu reduzieren.
 */
@Serializable
@OptIn(ExperimentalTime::class)
abstract class BaseDomainEvent(
    override val aggregateId: AggregateId,
    override val eventType: EventType,
    override val version: EventVersion,
    override val eventId: EventId = EventId(Uuid.random()),
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant,
    override val correlationId: CorrelationId? = null,
    override val causationId: CausationId? = null
) : DomainEvent {

    constructor(
        aggregateId: AggregateId,
        eventType: EventType,
        version: EventVersion,
        eventId: EventId = EventId(Uuid.random()),
        correlationId: CorrelationId? = null,
        causationId: CausationId? = null
    ) : this(
        aggregateId = aggregateId,
        eventType = eventType,
        version = version,
        eventId = eventId,
        timestamp = createTimestamp(),
        correlationId = correlationId,
        causationId = causationId
    )

    companion object {
        @OptIn(ExperimentalTime::class)
        private fun createTimestamp(): Instant = Clock.System.now()
    }
}

/**
 * Schnittstelle für einen Publisher, der Domain-Events veröffentlichen kann.
 */
interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: List<DomainEvent>)
}

/**
 * Schnittstelle für einen Handler, der auf bestimmte Domain-Events reagieren kann.
 */
interface DomainEventHandler<T : DomainEvent> {
    suspend fun handle(event: T)
    fun canHandle(eventType: EventType): Boolean

    /**
     * Rückwärtskompatible Methode für String-basierte Prüfung des Event-Typs.
     */
    fun canHandle(eventType: String): Boolean = canHandle(EventType(eventType))
}
