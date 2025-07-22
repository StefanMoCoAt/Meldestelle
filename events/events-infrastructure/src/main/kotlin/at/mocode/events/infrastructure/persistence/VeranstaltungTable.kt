package at.mocode.events.infrastructure.persistence

import at.mocode.core.domain.model.SparteE
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Database table definition for events (Veranstaltung) in the event-management context.
 *
 * This table stores all event information including dates, location,
 * organization details, and administrative information.
 */
object VeranstaltungTable : UUIDTable("veranstaltungen") {

    // Basic Information
    val name = varchar("name", 255)
    val beschreibung = text("beschreibung").nullable()

    // Dates
    val startDatum = date("start_datum")
    val endDatum = date("end_datum")
    val anmeldeschluss = date("anmeldeschluss").nullable()

    // Location and Organization
    val ort = varchar("ort", 255)
    val veranstalterVereinId = uuid("veranstalter_verein_id")

    // Event Details
    val sparten = text("sparten") // JSON array of SparteE values
    val istAktiv = bool("ist_aktiv").default(true)
    val istOeffentlich = bool("ist_oeffentlich").default(true)
    val maxTeilnehmer = integer("max_teilnehmer").nullable()

    // Audit Fields
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        // Indexes for performance
        index(false, name)
        index(false, startDatum)
        index(false, endDatum)
        index(false, veranstalterVereinId)
        index(false, istAktiv)
        index(false, istOeffentlich)
    }
}
