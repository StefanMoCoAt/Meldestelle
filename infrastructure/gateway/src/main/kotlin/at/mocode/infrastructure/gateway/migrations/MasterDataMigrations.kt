package at.mocode.infrastructure.gateway.migrations

import at.mocode.core.utils.database.Migration
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert

/**
 * Migration zur Erstellung der Stammdaten-Tabellen.
 */
class MasterDataTablesCreation : Migration(1, "Create master data tables") {
    override fun up() {
        // Land-Tabelle
        SchemaUtils.create(LandTable)

        // Bundesland-Tabelle
        SchemaUtils.create(BundeslandTable)

        // Altersklasse-Tabelle
        SchemaUtils.create(AltersklasseTable)

        // Sportart-Tabelle
        SchemaUtils.create(SportartTable)

        // Anfangsdaten einfügen
        insertInitialData()
    }

    private fun insertInitialData() {
        // Länder einfügen
        LandTable.batchInsert(listOf(
            mapOf("code" to "AT", "name" to "Österreich", "active" to true),
            mapOf("code" to "DE", "name" to "Deutschland", "active" to true),
            mapOf("code" to "CH", "name" to "Schweiz", "active" to true)
        )) { data ->
            this[LandTable.code] = data["code"] as String
            this[LandTable.name] = data["name"] as String
            this[LandTable.active] = data["active"] as Boolean
        }

        // Bundesländer einfügen (Österreich)
        BundeslandTable.batchInsert(listOf(
            mapOf("landCode" to "AT", "code" to "W", "name" to "Wien"),
            mapOf("landCode" to "AT", "code" to "NÖ", "name" to "Niederösterreich"),
            mapOf("landCode" to "AT", "code" to "OÖ", "name" to "Oberösterreich"),
            mapOf("landCode" to "AT", "code" to "S", "name" to "Salzburg"),
            mapOf("landCode" to "AT", "code" to "T", "name" to "Tirol"),
            mapOf("landCode" to "AT", "code" to "V", "name" to "Vorarlberg"),
            mapOf("landCode" to "AT", "code" to "ST", "name" to "Steiermark"),
            mapOf("landCode" to "AT", "code" to "K", "name" to "Kärnten"),
            mapOf("landCode" to "AT", "code" to "B", "name" to "Burgenland")
        )) { data ->
            this[BundeslandTable.landCode] = data["landCode"] as String
            this[BundeslandTable.code] = data["code"] as String
            this[BundeslandTable.name] = data["name"] as String
        }

        // Altersklassen einfügen
        AltersklasseTable.batchInsert(listOf(
            mapOf("code" to "U12", "name" to "Unter 12", "minAlter" to 0, "maxAlter" to 12),
            mapOf("code" to "U16", "name" to "Unter 16", "minAlter" to 13, "maxAlter" to 16),
            mapOf("code" to "U21", "name" to "Unter 21", "minAlter" to 17, "maxAlter" to 21),
            mapOf("code" to "ALLG", "name" to "Allgemeine Klasse", "minAlter" to 22, "maxAlter" to 99)
        )) { data ->
            this[AltersklasseTable.code] = data["code"] as String
            this[AltersklasseTable.name] = data["name"] as String
            this[AltersklasseTable.minAlter] = data["minAlter"] as Int
            this[AltersklasseTable.maxAlter] = data["maxAlter"] as Int
        }

        // Sportarten einfügen
        SportartTable.batchInsert(listOf(
            mapOf("code" to "DR", "name" to "Dressur"),
            mapOf("code" to "SP", "name" to "Springen"),
            mapOf("code" to "VS", "name" to "Vielseitigkeit"),
            mapOf("code" to "WR", "name" to "Western Reiten"),
            mapOf("code" to "VT", "name" to "Voltigieren")
        )) { data ->
            this[SportartTable.code] = data["code"] as String
            this[SportartTable.name] = data["name"] as String
        }
    }
}

// Definition der Tabellen
object LandTable : Table("land") {
    val code = varchar("code", 2)
    val name = varchar("name", 50)
    val active = bool("active").default(true)

    override val primaryKey = PrimaryKey(code)
}

object BundeslandTable : Table("bundesland") {
    val landCode = varchar("land_code", 2).references(LandTable.code)
    val code = varchar("code", 5)
    val name = varchar("name", 50)

    override val primaryKey = PrimaryKey(landCode, code)
}

object AltersklasseTable : Table("altersklasse") {
    val code = varchar("code", 10)
    val name = varchar("name", 50)
    val minAlter = integer("min_alter")
    val maxAlter = integer("max_alter")

    override val primaryKey = PrimaryKey(code)
}

object SportartTable : Table("sportart") {
    val code = varchar("code", 5)
    val name = varchar("name", 50)

    override val primaryKey = PrimaryKey(code)
}
