package at.mocode.infrastructure.gateway.migrations

import at.mocode.core.utils.database.Migration
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp

/**
 * Migration zur Erstellung der Veranstaltungsmanagement-Tabellen.
 */
class EventManagementTablesCreation : Migration(4, "Create event management tables") {
    override fun up() {
        // Veranstaltung-Tabelle
        SchemaUtils.create(VeranstaltungTable)

        // Veranstaltung_Sportart-Tabelle
        SchemaUtils.create(VeranstaltungSportartTable)
    }
}

// Definition der Tabellen
object VeranstaltungTable : Table("veranstaltung") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 100)
    val beschreibung = text("beschreibung").nullable()
    val startDatum = date("start_datum")
    val endDatum = date("end_datum")
    val anmeldeschluss = date("anmeldeschluss").nullable()
    val ort = varchar("ort", 100)
    val landCode = varchar("land_code", 2).references(LandTable.code)
    val bundeslandCode = varchar("bundesland_code", 5).nullable()
    val maxTeilnehmer = integer("max_teilnehmer").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val istOeffentlich = bool("ist_oeffentlich").default(true)
    val erstelltAm = timestamp("erstellt_am").defaultExpression(CurrentTimestamp)
    val geaendertAm = timestamp("geaendert_am").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(
            bundeslandCode to LandTable.code,
            landCode to BundeslandTable.landCode
        )
        // Ende muss nach Start sein
        check("datum_check") { endDatum greaterEq startDatum }
    }
}

object VeranstaltungSportartTable : Table("veranstaltung_sportart") {
    val veranstaltungId = uuid("veranstaltung_id").references(VeranstaltungTable.id)
    val sportartCode = varchar("sportart_code", 5).references(SportartTable.code)

    override val primaryKey = PrimaryKey(veranstaltungId, sportartCode)
}
