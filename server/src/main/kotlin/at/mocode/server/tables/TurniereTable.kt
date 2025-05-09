package at.mocode.server.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date // Für kotlinx-datetime LocalDate
import org.jetbrains.exposed.sql.kotlin.datetime.datetime // Für kotlinx-datetime LocalDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp // Für kotlinx-datetime Instant

object TurniereTable : Table("turniere") { // Name der Tabelle in PostgreSQL
    val id = uuid("id")
    val veranstaltungId = uuid("veranstaltung_id").references(VeranstaltungenTable.id)
    val oepsTurnierNr = varchar("oeps_turnier_nr", 15).uniqueIndex()
    val titel = varchar("titel", 255)
    val untertitel = varchar("untertitel", 500).nullable()
    val datumVon = date("datum_von")
    val datumBis = date("datum_bis")
    val nennungsschluss = datetime("nennungsschluss").nullable()
    val nennungsArtCsv = text("nennungs_art_csv").nullable()
    val nennungsHinweis = text("nennungs_hinweis").nullable()
    val eigenesNennsystemUrl = varchar("eigenes_nennsystem_url", 500).nullable()
    val nenngeld = varchar("nenngeld", 50).nullable()
    val startgeldStandard = varchar("startgeld_standard", 50).nullable()
    val turnierleiterId = uuid("turnierleiter_id").references(PersonenTable.id).nullable()
    val turnierbeauftragterId = uuid("turnierbeauftragter_id").references(PersonenTable.id).nullable()
    val richterIdsCsv = text("richter_ids_csv").nullable()
    val parcoursbauerIdsCsv = text("parcoursbauer_ids_csv").nullable()
    val parcoursAssistentIdsCsv = text("parcours_assistent_ids_csv").nullable()
    val tierarztInfos = text("tierarzt_infos").nullable()
    val hufschmiedInfo = text("hufschmied_info").nullable()
    val meldestelleVerantwortlicherId = uuid("meldestelle_verantwortlicher_id").references(PersonenTable.id).nullable()
    val meldestelleTelefon = varchar("meldestelle_telefon", 50).nullable()
    val meldestelleOeffnungszeiten = varchar("meldestelle_oeffnungszeiten", 255).nullable()
    val ergebnislistenUrl = varchar("ergebnislisten_url", 500).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, veranstaltungId)
        index(false, datumVon, datumBis)
        index(false, titel)
        index(false, turnierleiterId)
        index(false, turnierbeauftragterId)
        index(false, meldestelleVerantwortlicherId)
    }
}
