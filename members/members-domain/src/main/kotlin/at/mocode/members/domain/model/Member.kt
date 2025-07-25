package at.mocode.members.domain.model

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.KotlinLocalDateSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

/**
 * Domain model representing a member in the member management system.
 *
 * This entity represents a member of the organization with their personal
 * information and membership details.
 *
 * @property memberId Unique internal identifier for this member (UUID).
 * @property firstName First name of the member.
 * @property lastName Last name of the member.
 * @property email Email address of the member.
 * @property phone Phone number of the member (optional).
 * @property dateOfBirth Date of birth of the member (optional).
 * @property membershipNumber Unique membership number.
 * @property membershipStartDate Date when membership started.
 * @property membershipEndDate Date when membership ends (optional).
 * @property isActive Whether the membership is currently active.
 * @property address Address of the member (optional).
 * @property emergencyContact Emergency contact information (optional).
 * @property createdAt Timestamp when this record was created.
 * @property updatedAt Timestamp when this record was last updated.
 */
@Serializable
data class Member(
    @Serializable(with = UuidSerializer::class)
    val memberId: Uuid = uuid4(),

    // Personal Information
    var firstName: String,
    var lastName: String,
    var email: String,
    var phone: String? = null,

    @Serializable(with = KotlinLocalDateSerializer::class)
    var dateOfBirth: LocalDate? = null,

    // Membership Information
    var membershipNumber: String,

    @Serializable(with = KotlinLocalDateSerializer::class)
    var membershipStartDate: LocalDate,

    @Serializable(with = KotlinLocalDateSerializer::class)
    var membershipEndDate: LocalDate? = null,

    var isActive: Boolean = true,

    // Additional Information
    var address: String? = null,
    var emergencyContact: String? = null,

    // Audit Fields
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
) {
    /**
     * Returns the full name of the member.
     */
    fun getFullName(): String {
        return "$firstName $lastName"
    }

    /**
     * Checks if the membership is currently valid.
     */
    fun isMembershipValid(): Boolean {
        if (!isActive) return false

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return membershipEndDate?.let { endDate ->
            today <= endDate
        } ?: true // If no end date, membership is valid indefinitely
    }

    /**
     * Validates that the member data is consistent.
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (firstName.isBlank()) {
            errors.add("First name is required")
        }

        if (lastName.isBlank()) {
            errors.add("Last name is required")
        }

        if (email.isBlank()) {
            errors.add("Email is required")
        } else if (!isValidEmail(email)) {
            errors.add("Email format is invalid")
        }

        if (membershipNumber.isBlank()) {
            errors.add("Membership number is required")
        }

        membershipEndDate?.let { endDate ->
            if (endDate < membershipStartDate) {
                errors.add("Membership end date cannot be before start date")
            }
        }

        return errors
    }

    /**
     * Creates a copy of this member with updated timestamp.
     */
    fun withUpdatedTimestamp(): Member {
        return this.copy(updatedAt = Clock.System.now())
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}
