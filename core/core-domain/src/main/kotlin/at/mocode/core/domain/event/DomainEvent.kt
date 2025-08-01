package at.mocode.core.domain.event

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * Basis-Interface für alle Domänen-Events im System.
 * Ein Domänen-Event repräsentiert etwas fachlich Bedeutsames, das passiert ist.
 */
interface DomainEvent {
    val eventId: Uuid
    val aggregateId: Uuid
    val eventType: String
    val timestamp: Instant
    val version: Long
    val correlationId: Uuid?
    val causationId: Uuid?
}

/**
 * Abstrakte Basisklasse für Domänen-Events, um Boilerplate-Code zu reduzieren.
 */
@Serializable
abstract class BaseDomainEvent(
    @Serializable(with = UuidSerializer::class)
    override val aggregateId: Uuid,
    override val eventType: String,
    override val version: Long,
    @Serializable(with = UuidSerializer::class)
    override val eventId: Uuid = uuid4(),
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant = Clock.System.now(),
    @Serializable(with = UuidSerializer::class)
    override val correlationId: Uuid? = null,
    @Serializable(with = UuidSerializer::class)
    override val causationId: Uuid? = null
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
