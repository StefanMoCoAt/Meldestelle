package at.mocode.client.common.repository

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Simplified Event data class for client-side use.
 * This is a client-side representation of the Veranstaltung entity from the domain model.
 */
@Serializable
data class Event(
    val id: String = "",
    val name: String,
    val beschreibung: String? = null,
    val startDatum: LocalDate,
    val endDatum: LocalDate,
    val ort: String,
    val veranstalterVereinId: String? = null,
    val sparten: List<String> = emptyList(),
    val istAktiv: Boolean = true,
    val istOeffentlich: Boolean = true,
    val maxTeilnehmer: Int? = null,
    val anmeldeschluss: LocalDate? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
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
}
