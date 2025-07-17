package at.mocode.members.infrastructure.repository

import at.mocode.enums.DatenQuelleE
import at.mocode.enums.GeschlechtE
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.date

/**
 * Exposed table definition for Person entities.
 *
 * This table represents the database schema for storing person data
 * in the member management bounded context.
 */
object PersonTable : UUIDTable("persons") {

    // Basic person information
    val oepsSatzNr = varchar("oeps_satz_nr", 6).nullable().uniqueIndex()
    val nachname = varchar("nachname", 100)
    val vorname = varchar("vorname", 100)
    val titel = varchar("titel", 50).nullable()

    // Personal details
    val geburtsdatum = date("geburtsdatum").nullable()
    val geschlecht = enumerationByName("geschlecht", 10, GeschlechtE::class).nullable()
    val nationalitaetLandId = uuid("nationalitaet_land_id").nullable()
    val feiId = varchar("fei_id", 20).nullable()

    // Contact information
    val telefon = varchar("telefon", 50).nullable()
    val email = varchar("email", 100).nullable()

    // Address information
    val strasse = varchar("strasse", 200).nullable()
    val plz = varchar("plz", 10).nullable()
    val ort = varchar("ort", 100).nullable()
    val adresszusatzZusatzinfo = varchar("adresszusatz_zusatzinfo", 200).nullable()

    // Club membership
    val stammVereinId = uuid("stamm_verein_id").nullable()
    val mitgliedsNummerBeiStammVerein = varchar("mitglieds_nummer_bei_stamm_verein", 50).nullable()

    // Status and restrictions
    val istGesperrt = bool("ist_gesperrt").default(false)
    val sperrGrund = varchar("sperr_grund", 500).nullable()

    // OEPS specific data
    val altersklasseOepsCodeRaw = varchar("altersklasse_oeps_code_raw", 10).nullable()
    val istJungerReiterOepsFlag = bool("ist_junger_reiter_oeps_flag").default(false)
    val kaderStatusOepsRaw = varchar("kader_status_oeps_raw", 10).nullable()

    // Metadata
    val datenQuelle = enumerationByName("daten_quelle", 20, DatenQuelleE::class).default(DatenQuelleE.MANUELL)
    val istAktiv = bool("ist_aktiv").default(true)
    val notizenIntern = text("notizen_intern").nullable()

    // Audit fields
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
