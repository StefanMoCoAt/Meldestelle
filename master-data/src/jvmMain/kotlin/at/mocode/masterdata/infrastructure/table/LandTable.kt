package at.mocode.masterdata.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime

/**
 * Exposed-Tabellendefinition für die Land-Entität (Länderstammdaten).
 */
object LandTable : Table("land") {
    val id = uuid("id").autoGenerate()
    val isoAlpha2Code = varchar("iso_alpha2_code", 2).uniqueIndex()
    val isoAlpha3Code = varchar("iso_alpha3_code", 3).uniqueIndex()
    val nameDe = varchar("name_de", 100)
    val nameEn = varchar("name_en", 100)
    val istEuMitglied = bool("ist_eu_mitglied").default(false)
    val istEwrMitglied = bool("ist_ewr_mitglied").default(false)
    val sortierReihenfolge = integer("sortier_reihenfolge").default(999)
    val istAktiv = bool("ist_aktiv").default(true)
    val erstelltAm = datetime("erstellt_am").defaultExpression(CurrentDateTime)
    val geaendertAm = datetime("geaendert_am").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
