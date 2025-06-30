package at.mocode.tables.veranstaltung

import at.mocode.tables.AbteilungTable
import at.mocode.tables.PlaetzeTable
import at.mocode.tables.TurniereTable
import at.mocode.tables.VeranstaltungenTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// Event models tables
object PruefungAbteilungTable : Table("pruefung_abteilungen") {
    val id = uuid("id")
    val pruefungId = uuid("pruefung_id") // FK to Pruefung when implemented
    val abteilungId = uuid("abteilung_id").references(AbteilungTable.id)
    val reihenfolge = integer("reihenfolge").default(1)
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, pruefungId)
        index(false, abteilungId)
        index(false, reihenfolge)
    }
}

object PruefungOEPSTable : Table("pruefung_oeps") {
    val id = uuid("id")
    val pruefungId = uuid("pruefung_id") // FK to Pruefung when implemented
    val oepsCode = varchar("oeps_code", 50)
    val oepsBezeichnung = varchar("oeps_bezeichnung", 255)
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, pruefungId)
        index(false, oepsCode)
    }
}

object TurnierHatPlatzTable : Table("turnier_hat_platz") {
    val id = uuid("id")
    val turnierId = uuid("turnier_id").references(TurniereTable.id)
    val platzId = uuid("platz_id").references(PlaetzeTable.id)
    val istHauptplatz = bool("ist_hauptplatz").default(false)
    val verfuegbarVon = date("verfuegbar_von").nullable()
    val verfuegbarBis = date("verfuegbar_bis").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, turnierId)
        index(false, platzId)
        index(false, istHauptplatz)
    }
}

object TurnierOEPSTable : Table("turnier_oeps") {
    val id = uuid("id")
    val turnierId = uuid("turnier_id").references(TurniereTable.id)
    val oepsTurnierNr = varchar("oeps_turnier_nr", 50)
    val oepsKategorie = varchar("oeps_kategorie", 100)
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, turnierId)
        index(false, oepsTurnierNr)
        index(false, oepsKategorie)
    }
}

object VeranstaltungsRahmenTable : Table("veranstaltungs_rahmen") {
    val id = uuid("id")
    val veranstaltungId = uuid("veranstaltung_id").references(VeranstaltungenTable.id)
    val rahmenTyp = varchar("rahmen_typ", 100)
    val bezeichnung = varchar("bezeichnung", 255)
    val beschreibung = text("beschreibung").nullable()
    val reihenfolge = integer("reihenfolge").default(1)
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, veranstaltungId)
        index(false, rahmenTyp)
        index(false, reihenfolge)
    }
}
