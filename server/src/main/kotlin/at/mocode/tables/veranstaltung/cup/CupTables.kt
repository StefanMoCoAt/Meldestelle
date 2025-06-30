package at.mocode.tables.veranstaltung.cup

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// Cup/Series models tables
object MCSWertungspruefungTable : Table("mcs_wertungspruefungen") {
    val id = uuid("id")
    val meisterschaftCupSerieId = uuid("meisterschaft_cup_serie_id") // FK to Meisterschaft_Cup_Serie
    val bewerbId = uuid("bewerb_id") // FK to Bewerb when implemented
    val gewichtungsFaktor = double("gewichtungs_faktor").default(1.0)
    val mindestTeilnehmerAnzahl = integer("mindest_teilnehmer_anzahl").nullable()
    val istPflichtpruefung = bool("ist_pflichtpruefung").default(false)
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, meisterschaftCupSerieId)
        index(false, bewerbId)
        index(false, istPflichtpruefung)
        index(false, istAktiv)
    }
}

object MeisterschaftCupSerieTable : Table("meisterschaft_cup_serien") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val kuerzel = varchar("kuerzel", 50).nullable()
    val cupSerieTypE = varchar("cup_serie_typ", 50)
    val saison = varchar("saison", 20) // e.g., "2024", "2024/25"
    val sparteE = varchar("sparte", 50)
    val beschreibung = text("beschreibung").nullable()
    val reglementUrl = varchar("reglement_url", 500).nullable()
    val anmeldeschluss = date("anmeldeschluss").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, name)
        index(false, cupSerieTypE)
        index(false, saison)
        index(false, sparteE)
        index(false, istAktiv)
    }
}
