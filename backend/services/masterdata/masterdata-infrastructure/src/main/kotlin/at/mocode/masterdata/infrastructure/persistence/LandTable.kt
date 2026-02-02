package at.mocode.masterdata.infrastructure.persistence

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.kotlin.datetime.datetime
import org.jetbrains.exposed.v1.core.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.core.javaUUID

/**
 * Exposed-Tabellendefinition für die Land-Entität (Länderstammdaten).
 *
 * Diese Tabelle speichert alle Informationen zu Ländern/Nationen entsprechend
 * der LandDefinition Domain-Entität.
 */
object LandTable : Table("land") {
    val id = javaUUID("id").autoGenerate()
    val isoAlpha2Code = varchar("iso_alpha2_code", 2).uniqueIndex()
    val isoAlpha3Code = varchar("iso_alpha3_code", 3).uniqueIndex()
    val isoNumerischerCode = varchar("iso_numerischer_code", 3).nullable()
    val nameDeutsch = varchar("name_deutsch", 100)
    val nameEnglisch = varchar("name_englisch", 100).nullable()
    val wappenUrl = varchar("wappen_url", 500).nullable()
    val istEuMitglied = bool("ist_eu_mitglied").nullable()
    val istEwrMitglied = bool("ist_ewr_mitglied").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val sortierReihenfolge = integer("sortier_reihenfolge").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
