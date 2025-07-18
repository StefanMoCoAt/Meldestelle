package at.mocode.members.infrastructure.repository

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Database table definition for users (authentication).
 *
 * This table stores user authentication data and is linked to the Person table.
 * It follows the Exposed framework conventions for UUID-based tables.
 */
object UserTable : UUIDTable("users") {

    // Foreign key to the Person table
    val personId = uuid("person_id").references(PersonTable.id)

    // Authentication fields
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val salt = varchar("salt", 255)

    // Status flags
    val istAktiv = bool("ist_aktiv").default(true)
    val istEmailVerifiziert = bool("ist_email_verifiziert").default(false)

    // Login tracking
    val letzteAnmeldung = datetime("letzte_anmeldung").nullable()
    val fehlgeschlageneAnmeldungen = integer("fehlgeschlagene_anmeldungen").default(0)
    val gesperrtBis = datetime("gesperrt_bis").nullable()
    val passwortAendernErforderlich = bool("passwort_aendern_erforderlich").default(false)

    // Audit fields
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
