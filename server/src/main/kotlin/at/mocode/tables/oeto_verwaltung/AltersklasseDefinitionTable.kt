package at.mocode.tables.oeto_verwaltung

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AltersklasseDefinitionTable : Table("altersklasse_definitionen") {
    val altersklasseId = uuid("altersklasse_id")
    val altersklasseCode = varchar("altersklasse_code", 50).uniqueIndex()
    val bezeichnung = varchar("bezeichnung", 255)
    val minAlter = integer("min_alter").nullable()
    val maxAlter = integer("max_alter").nullable()
    val stichtagRegelText = varchar("stichtag_regel_text", 500).default("31.12. des laufenden Kalenderjahres")
    val sparteFilter = varchar("sparte_filter", 50).nullable()
    val geschlechtFilter = char("geschlecht_filter").nullable()
    val oetoRegelReferenzId = uuid("oeto_regel_referenz_id").nullable() // FK to OETORegelReferenz when implemented
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(altersklasseId)

    init {
        index(false, altersklasseCode)
        index(false, sparteFilter)
        index(false, geschlechtFilter)
        index(false, istAktiv)
        index(false, minAlter)
        index(false, maxAlter)
    }
}
