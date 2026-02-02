package at.mocode.masterdata.infrastructure.persistence

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.kotlin.datetime.datetime
import org.jetbrains.exposed.v1.core.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.core.javaUUID

/**
 * Exposed-Tabellendefinition für die Altersklasse-Entität (Altersklassendefinitionen).
 *
 * Diese Tabelle speichert alle Informationen zu Altersklassen für Teilnehmer
 * entsprechend der AltersklasseDefinition Domain-Entität.
 */
object AltersklasseTable : Table("altersklasse") {
    val id = javaUUID("id").autoGenerate()
    val altersklasseCode = varchar("altersklasse_code", 50).uniqueIndex()
    val bezeichnung = varchar("bezeichnung", 200)
    val minAlter = integer("min_alter").nullable()
    val maxAlter = integer("max_alter").nullable()
    val stichtagRegelText = varchar("stichtag_regel_text", 500).nullable()
    val sparteFilter = varchar("sparte_filter", 50).nullable() // Enum as string
    val geschlechtFilter = char("geschlecht_filter").nullable()
    val oetoRegelReferenzId = javaUUID("oeto_regel_referenz_id").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)

    init {
        // Index for performance on common queries
        index(customIndexName = "idx_altersklasse_aktiv", columns = arrayOf(istAktiv))
        index(customIndexName = "idx_altersklasse_sparte", columns = arrayOf(sparteFilter))
        index(customIndexName = "idx_altersklasse_geschlecht", columns = arrayOf(geschlechtFilter))
        index(customIndexName = "idx_altersklasse_alter", columns = arrayOf(minAlter, maxAlter))
    }
}
