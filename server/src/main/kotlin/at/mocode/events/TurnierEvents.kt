package at.mocode.events

import at.mocode.model.Turnier
import at.mocode.serializers.UuidSerializer
import at.mocode.serializers.KotlinInstantSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Event published when a new tournament is created
 */
@Serializable
data class TurnierCreatedEvent(
    @Serializable(with = UuidSerializer::class)
    override val eventId: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    override val aggregateId: Uuid,
    override val eventType: String = "TurnierCreated",
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant = Clock.System.now(),
    override val version: Long = 1,
    val turnier: Turnier,
    @Serializable(with = UuidSerializer::class)
    val createdBy: Uuid? = null
) : DomainEvent

/**
 * Event published when a tournament is updated
 */
@Serializable
data class TurnierUpdatedEvent(
    @Serializable(with = UuidSerializer::class)
    override val eventId: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    override val aggregateId: Uuid,
    override val eventType: String = "TurnierUpdated",
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant = Clock.System.now(),
    override val version: Long = 1,
    val previousTurnier: Turnier,
    val updatedTurnier: Turnier,
    @Serializable(with = UuidSerializer::class)
    val updatedBy: Uuid? = null
) : DomainEvent

/**
 * Event published when a tournament is deleted
 */
@Serializable
data class TurnierDeletedEvent(
    @Serializable(with = UuidSerializer::class)
    override val eventId: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    override val aggregateId: Uuid,
    override val eventType: String = "TurnierDeleted",
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant = Clock.System.now(),
    override val version: Long = 1,
    val deletedTurnier: Turnier,
    @Serializable(with = UuidSerializer::class)
    val deletedBy: Uuid? = null
) : DomainEvent

/**
 * Event published when tournament registration opens
 */
@Serializable
data class TurnierRegistrationOpenedEvent(
    @Serializable(with = UuidSerializer::class)
    override val eventId: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    override val aggregateId: Uuid,
    override val eventType: String = "TurnierRegistrationOpened",
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant = Clock.System.now(),
    override val version: Long = 1,
    val turnierId: Uuid,
    val turnierTitel: String,
    @Serializable(with = KotlinInstantSerializer::class)
    val registrationDeadline: Instant?
) : DomainEvent

/**
 * Event published when tournament registration closes
 */
@Serializable
data class TurnierRegistrationClosedEvent(
    @Serializable(with = UuidSerializer::class)
    override val eventId: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    override val aggregateId: Uuid,
    override val eventType: String = "TurnierRegistrationClosed",
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant = Clock.System.now(),
    override val version: Long = 1,
    val turnierId: Uuid,
    val turnierTitel: String,
    val totalRegistrations: Int = 0
) : DomainEvent

/**
 * Event published when a tournament status changes (e.g., from planned to active to completed)
 */
@Serializable
data class TurnierStatusChangedEvent(
    @Serializable(with = UuidSerializer::class)
    override val eventId: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    override val aggregateId: Uuid,
    override val eventType: String = "TurnierStatusChanged",
    @Serializable(with = KotlinInstantSerializer::class)
    override val timestamp: Instant = Clock.System.now(),
    override val version: Long = 1,
    val turnierId: Uuid,
    val turnierTitel: String,
    val previousStatus: String,
    val newStatus: String,
    @Serializable(with = UuidSerializer::class)
    val changedBy: Uuid? = null
) : DomainEvent
