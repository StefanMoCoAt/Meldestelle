package at.mocode.tables.stammdaten

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BundeslandDefinitionTable : Table("bundesland_definitionen") {
    val bundeslandId = uuid("bundesland_id")
    val bundeslandCode = varchar("bundesland_code", 10).uniqueIndex()
    val name = varchar("name", 255)
    val kuerzel = varchar("kuerzel", 10).nullable()
    val landId = uuid("land_id") // FK to LandDefinition when implemented
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(bundeslandId)

    init {
        index(false, bundeslandCode)
        index(false, name)
        index(false, landId)
        index(false, istAktiv)
    }
}

object LandDefinitionTable : Table("land_definitionen") {
    val landId = uuid("land_id")
    val landCode = varchar("land_code", 10).uniqueIndex()
    val name = varchar("name", 255)
    val nameEnglisch = varchar("name_englisch", 255).nullable()
    val iso2Code = varchar("iso2_code", 2).nullable()
    val iso3Code = varchar("iso3_code", 3).nullable()
    val istEuMitglied = bool("ist_eu_mitglied").default(false)
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(landId)

    init {
        index(false, landCode)
        index(false, name)
        index(false, iso2Code)
        index(false, iso3Code)
        index(false, istEuMitglied)
        index(false, istAktiv)
    }
}
