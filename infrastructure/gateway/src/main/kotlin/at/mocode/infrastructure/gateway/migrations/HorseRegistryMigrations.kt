package at.mocode.infrastructure.gateway.migrations

import at.mocode.core.utils.database.Migration
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp

/**
 * Migration zur Erstellung der Pferderegister-Tabellen.
 */
class HorseRegistryTablesCreation : Migration(3, "Create horse registry tables") {
    override fun up() {
        // Pferd-Tabelle
        SchemaUtils.create(PferdTable)

        // Pferdebesitzer-Tabelle
        SchemaUtils.create(PferdebesitzerTable)
    }
}

// Definition der Tabellen
object PferdTable : Table("pferd") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 100)
    val lebensnummer = varchar("lebensnummer", 30).uniqueIndex()
    val rasse = varchar("rasse", 50)
    val farbe = varchar("farbe", 50)
    val geburtsjahr = integer("geburtsjahr").nullable()
    val geschlecht = varchar("geschlecht", 1) // 'S' = Stute, 'W' = Wallach, 'H' = Hengst
    val aktiv = bool("aktiv").default(true)
    val erstelltAm = timestamp("erstellt_am").defaultExpression(CurrentTimestamp)
    val geaendertAm = timestamp("geaendert_am").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        // Geschlecht muss S, W oder H sein
        check("geschlecht_check") { geschlecht.inList(listOf("S", "W", "H")) }
    }
}

object PferdebesitzerTable : Table("pferdebesitzer") {
    val pferdId = uuid("pferd_id").references(PferdTable.id)
    val personId = uuid("person_id").references(PersonTable.id)
    val hauptbesitzer = bool("hauptbesitzer").default(false)
    val aktiv = bool("aktiv").default(true)
    val erstelltAm = timestamp("erstellt_am").defaultExpression(CurrentTimestamp)
    val geaendertAm = timestamp("geaendert_am").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(pferdId, personId)
}
