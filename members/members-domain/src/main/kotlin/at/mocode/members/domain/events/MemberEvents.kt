package at.mocode.members.domain.events

import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Base interface for all member domain events.
 */
sealed interface MemberEvent {
    val eventId: String
    val memberId: Uuid
    val timestamp: Instant
    val eventType: String
}

/**
 * Event published when a new member is created.
 */
data class MemberCreatedEvent(
    override val eventId: String,
    override val memberId: Uuid,
    override val timestamp: Instant,
    val firstName: String,
    val lastName: String,
    val email: String,
    val membershipNumber: String,
    val membershipStartDate: LocalDate,
    val isActive: Boolean
) : MemberEvent {
    override val eventType: String = "MemberCreated"
}

/**
 * Event published when a member is updated.
 */
data class MemberUpdatedEvent(
    override val eventId: String,
    override val memberId: Uuid,
    override val timestamp: Instant,
    val firstName: String,
    val lastName: String,
    val email: String,
    val membershipNumber: String,
    val membershipStartDate: LocalDate,
    val membershipEndDate: LocalDate?,
    val isActive: Boolean,
    val changes: Map<String, Any?>
) : MemberEvent {
    override val eventType: String = "MemberUpdated"
}

/**
 * Event published when a member is deleted.
 */
data class MemberDeletedEvent(
    override val eventId: String,
    override val memberId: Uuid,
    override val timestamp: Instant,
    val membershipNumber: String,
    val firstName: String,
    val lastName: String
) : MemberEvent {
    override val eventType: String = "MemberDeleted"
}

/**
 * Event published when a member's membership is about to expire.
 */
data class MembershipExpiringEvent(
    override val eventId: String,
    override val memberId: Uuid,
    override val timestamp: Instant,
    val membershipNumber: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val membershipEndDate: LocalDate,
    val daysUntilExpiry: Int
) : MemberEvent {
    override val eventType: String = "MembershipExpiring"
}
