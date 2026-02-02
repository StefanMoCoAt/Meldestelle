package at.mocode.masterdata.infrastructure.persistence

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.kotlin.datetime.datetime
import org.jetbrains.exposed.v1.core.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.core.javaUUID

/**
 * Exposed-Tabellendefinition für die Bundesland-Entität (Bundesländer/Regionen).
 *
 * Diese Tabelle speichert alle Informationen zu Bundesländern und subnationalen
 * Verwaltungseinheiten entsprechend der BundeslandDefinition Domain-Entität.
 */
object BundeslandTable : Table("bundesland") {
    val id = javaUUID("id").autoGenerate()
    val landId = javaUUID("land_id").references(LandTable.id)
    val oepsCode = varchar("oeps_code", 10).nullable()
    val iso3166_2_Code = varchar("iso_3166_2_code", 10).nullable()
    val name = varchar("name", 100)
    val kuerzel = varchar("kuerzel", 10).nullable()
    val wappenUrl = varchar("wappen_url", 500).nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val sortierReihenfolge = integer("sortier_reihenfolge").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)

    init {
        // Unique constraint for OEPS code per country
        uniqueIndex("uk_bundesland_oeps_land", oepsCode, landId)
        // Unique constraint for ISO 3166-2 code globally
        uniqueIndex("uk_bundesland_iso3166_2", iso3166_2_Code)
    }
}
