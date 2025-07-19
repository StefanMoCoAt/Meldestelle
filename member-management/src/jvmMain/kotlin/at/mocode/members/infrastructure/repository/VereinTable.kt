package at.mocode.members.infrastructure.repository

import at.mocode.enums.DatenQuelleE
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Exposed table definition for Verein (Club/Association) entities.
 *
 * This table represents the database schema for storing club data
 * in the member management bounded context.
 */
object VereinTable : UUIDTable("vereine") {

    // Basic club information
    val oepsVereinsNr = varchar("oeps_vereins_nr", 4).nullable().uniqueIndex()
    val name = varchar("name", 200)
    val kuerzel = varchar("kuerzel", 20).nullable()

    // Address information
    val adresseStrasse = varchar("adresse_strasse", 200).nullable()
    val plz = varchar("plz", 10).nullable()
    val ort = varchar("ort", 100).nullable()

    // Geographic references
    val bundeslandId = uuid("bundesland_id").nullable()
    val landId = uuid("land_id")

    // Contact information
    val emailAllgemein = varchar("email_allgemein", 100).nullable()
    val telefonAllgemein = varchar("telefon_allgemein", 50).nullable()
    val webseiteUrl = varchar("webseite_url", 200).nullable()

    // Metadata
    val datenQuelle = enumerationByName("daten_quelle", 20, DatenQuelleE::class).default(DatenQuelleE.OEPS_ZNS)
    val istAktiv = bool("ist_aktiv").default(true)
    val notizenIntern = text("notizen_intern").nullable()

    // Audit fields
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
