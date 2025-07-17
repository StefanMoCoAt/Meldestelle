package at.mocode.events.domain.model

import at.mocode.enums.SparteE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.KotlinLocalDateSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable

/**
 * Domain model representing an event/competition in the event management system.
 *
 * This entity represents a sporting event that can contain multiple tournaments
 * and competitions. It serves as the main aggregate root for event planning.
 *
 * @property veranstaltungId Unique internal identifier for this event (UUID).
 * @property name Name of the event.
 * @property beschreibung Description of the event.
 * @property startDatum Start date of the event.
 * @property endDatum End date of the event.
 * @property ort Location where the event takes place.
 * @property veranstalterVereinId ID of the organizing club/association.
 * @property sparten List of sport disciplines included in this event.
 * @property istAktiv Whether the event is currently active.
 * @property istOeffentlich Whether the event is public.
 * @property maxTeilnehmer Maximum number of participants (optional).
 * @property anmeldeschluss Registration deadline.
 * @property createdAt Timestamp when this record was created.
 * @property updatedAt Timestamp when this record was last updated.
 */
@Serializable
data class Veranstaltung(
    @Serializable(with = UuidSerializer::class)
    val veranstaltungId: Uuid = uuid4(),

    // Basic Information
    var name: String,
    var beschreibung: String? = null,

    // Dates
    @Serializable(with = KotlinLocalDateSerializer::class)
    var startDatum: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var endDatum: LocalDate,

    // Location and Organization
    var ort: String,
    @Serializable(with = UuidSerializer::class)
    var veranstalterVereinId: Uuid,

    // Event Details
    var sparten: List<SparteE> = emptyList(),
    var istAktiv: Boolean = true,
    var istOeffentlich: Boolean = true,
    var maxTeilnehmer: Int? = null,

    @Serializable(with = KotlinLocalDateSerializer::class)
    var anmeldeschluss: LocalDate? = null,

    // Audit Fields
    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
) {
    /**
     * Checks if the event is currently accepting registrations.
     */
    fun isRegistrationOpen(): Boolean {
        // Simplified implementation - can be enhanced with proper date comparison
        return istAktiv && anmeldeschluss != null
    }

    /**
     * Returns the duration of the event in days.
     */
    fun getDurationInDays(): Int {
        return (endDatum.toEpochDays() - startDatum.toEpochDays()).toInt() + 1
    }

    /**
     * Checks if the event spans multiple days.
     */
    fun isMultiDay(): Boolean {
        return startDatum != endDatum
    }

    /**
     * Validates that the event data is consistent.
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (name.isBlank()) {
            errors.add("Event name is required")
        }

        if (ort.isBlank()) {
            errors.add("Event location is required")
        }

        if (endDatum < startDatum) {
            errors.add("End date cannot be before start date")
        }

        anmeldeschluss?.let { deadline ->
            if (deadline > startDatum) {
                errors.add("Registration deadline cannot be after event start date")
            }
        }

        maxTeilnehmer?.let { max ->
            if (max <= 0) {
                errors.add("Maximum participants must be positive")
            }
        }

        return errors
    }

    /**
     * Creates a copy of this event with updated timestamp.
     */
    fun withUpdatedTimestamp(): Veranstaltung {
        return this.copy(updatedAt = Clock.System.now())
    }
}
