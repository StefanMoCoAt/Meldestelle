package at.mocode.tables.veranstaltung.spezifika

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// Spezifika models tables
object DressurPruefungSpezifikaTable : Table("dressur_pruefung_spezifika") {
    val id = uuid("id")
    val bewerbId = uuid("bewerb_id") // FK to Bewerb when implemented
    val pruefungsaufgabeId = uuid("pruefungsaufgabe_id") // FK to Pruefungsaufgabe when implemented
    val viereckGroesse = varchar("viereck_groesse", 50).nullable()
    val richtverfahrenModus = varchar("richtverfahren_modus", 50).nullable()
    val anzahlRichter = integer("anzahl_richter").default(1)
    val maxPunkteProRichter = double("max_punkte_pro_richter").nullable()
    val istKuerPruefung = bool("ist_kuer_pruefung").default(false)
    val musikErlaubt = bool("musik_erlaubt").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, bewerbId)
        index(false, pruefungsaufgabeId)
        index(false, istKuerPruefung)
    }
}

object SpringPruefungSpezifikaTable : Table("spring_pruefung_spezifika") {
    val id = uuid("id")
    val bewerbId = uuid("bewerb_id") // FK to Bewerb when implemented
    val hoehe = integer("hoehe").nullable() // in cm
    val anzahlHindernisse = integer("anzahl_hindernisse").nullable()
    val parcourslange = integer("parcourslange").nullable() // in Metern
    val erlaubteZeit = integer("erlaubte_zeit").nullable() // in Sekunden
    val hatStechen = bool("hat_stechen").default(false)
    val stechhoehe = integer("stechhoehe").nullable() // in cm
    val artDesStechens = varchar("art_des_stechens", 100).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, bewerbId)
        index(false, hoehe)
        index(false, hatStechen)
    }
}
