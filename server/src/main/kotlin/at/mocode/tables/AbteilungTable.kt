package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AbteilungTable : Table("abteilungen") {
    val id = uuid("id")
    val bewerbId = uuid("bewerb_id") // FK to Bewerb when implemented
    val abteilungsKennzeichen = varchar("abteilungs_kennzeichen", 50)
    val bezeichnungIntern = varchar("bezeichnung_intern", 255).nullable()
    val bezeichnungAufStartliste = varchar("bezeichnung_auf_startliste", 255).nullable()

    // Teilungskriterien
    val teilungsKriteriumLizenz = varchar("teilungs_kriterium_lizenz", 255).nullable()
    val teilungsKriteriumPferdealter = varchar("teilungs_kriterium_pferdealter", 255).nullable()
    val teilungsKriteriumAltersklasseReiter = varchar("teilungs_kriterium_altersklasse_reiter", 255).nullable()
    val teilungsKriteriumAnzahlMin = integer("teilungs_kriterium_anzahl_min").nullable()
    val teilungsKriteriumAnzahlMax = integer("teilungs_kriterium_anzahl_max").nullable()
    val teilungsKriteriumFreiText = text("teilungs_kriterium_frei_text").nullable()

    // Überschreibungen vom Hauptbewerb
    val startgeld = decimal("startgeld", 10, 2).nullable()
    val platzId = uuid("platz_id").nullable() // FK to Platz when implemented
    val datum = date("datum").nullable()
    val beginnzeitTypE = varchar("beginnzeit_typ", 50).default("ANSCHLIESSEND")
    val beginnzeitFix = time("beginnzeit_fix").nullable()
    val beginnNachAbteilungId = uuid("beginn_nach_abteilung_id").nullable()
    val beginnzeitCa = time("beginnzeit_ca").nullable()
    val dauerProStartGeschaetztSek = integer("dauer_pro_start_geschaetzt_sek").nullable()
    val umbauzeitNachAbteilungMin = integer("umbauzeit_nach_abteilung_min").nullable()
    val besichtigungszeitVorAbteilungMin = integer("besichtigungszeit_vor_abteilung_min").nullable()
    val stechzeitZusaetzlichMin = integer("stechzeit_zusaetzlich_min").nullable()

    val anzahlStarter = integer("anzahl_starter").default(0)
    val istAktiv = bool("ist_aktiv").default(true)

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, bewerbId)
        index(false, platzId)
        index(false, istAktiv)
    }
}
