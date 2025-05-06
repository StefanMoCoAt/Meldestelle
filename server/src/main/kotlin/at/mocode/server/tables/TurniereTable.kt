package at.mocode.server.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date // Für kotlinx-datetime LocalDate
import org.jetbrains.exposed.sql.kotlin.datetime.datetime // Für kotlinx-datetime LocalDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp // Für kotlinx-datetime Instant

/**
 * Optimized version of TurniereTable
 * Changes:
 * - Added proper imports for enums
 * - Added indexes for foreign key fields and common search fields
 * - Added init block for defining indexes
 */
object TurniereTable : Table(name = "turniere") { // Name der Tabelle in PostgreSQL
    val id = uuid(name = "id")
    val veranstaltungId = uuid(name = "veranstaltung_id").references(ref = VeranstaltungenTable.id)
    val oepsTurnierNr = varchar(name = "oeps_turnier_nr", length = 15).uniqueIndex()
    val titel = varchar(name = "titel", length = 255)
    val untertitel = varchar(name = "untertitel", length = 500).nullable()
    val datumVon = date(name = "datum_von")
    val datumBis = date(name = "datum_bis")
    val nennungsschluss = datetime(name = "nennungsschluss").nullable()
    val nennungsArtCsv = text(name = "nennungs_art_csv").nullable()
    val nennungsHinweis = text(name = "nennungs_hinweis").nullable()
    val eigenesNennsystemUrl = varchar(name = "eigenes_nennsystem_url", length = 500).nullable()
    val nenngeld = varchar(name = "nenngeld", length = 50).nullable()
    val startgeldStandard = varchar(name = "startgeld_standard", length = 50).nullable()
    val turnierleiterId = uuid(name = "turnierleiter_id").references(ref = PersonenTable.id).nullable()
    val turnierbeauftragterId = uuid(name = "turnierbeauftragter_id").references(ref = PersonenTable.id).nullable()
    val richterIdsCsv = text(name = "richter_ids_csv").nullable()
    val parcoursbauerIdsCsv = text(name = "parcoursbauer_ids_csv").nullable()
    val parcoursAssistentIdsCsv = text(name = "parcours_assistent_ids_csv").nullable()
    val tierarztInfos = text(name = "tierarzt_infos").nullable()
    val hufschmiedInfo = text(name = "hufschmied_info").nullable()
    val meldestelleVerantwortlicherId = uuid(name = "meldestelle_verantwortlicher_id").references(ref = PersonenTable.id).nullable()
    val meldestelleTelefon = varchar(name = "meldestelle_telefon", length = 50).nullable()
    val meldestelleOeffnungszeiten = varchar(name = "meldestelle_oeffnungszeiten", length = 255).nullable()
    val ergebnislistenUrl = varchar(name = "ergebnislisten_url", length = 500).nullable()
    val createdAt = timestamp(name = "created_at")
    val updatedAt = timestamp(name = "updated_at")

    override val primaryKey = PrimaryKey(firstColumn = id)

    init {
        index(isUnique = false, veranstaltungId)
        index(isUnique = false, datumVon, datumBis)
        index(isUnique = false, titel)
        index(isUnique = false, turnierleiterId)
        index(isUnique = false, turnierbeauftragterId)
        index(isUnique = false, meldestelleVerantwortlicherId)
    }
}
