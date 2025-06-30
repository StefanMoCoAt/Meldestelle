package at.mocode.tables.oeto_verwaltung

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object LizenzTypGlobalTable : Table("lizenz_typ_global") {
    val lizenzTypGlobalId = uuid("lizenz_typ_global_id")
    val lizenzTypCode = varchar("lizenz_typ_code", 50).uniqueIndex()
    val bezeichnung = varchar("bezeichnung", 255)
    val lizenzKategorieE = varchar("lizenz_kategorie", 50)
    val lizenzTypE = varchar("lizenz_typ", 50)
    val sparteE = varchar("sparte", 50).nullable()
    val beschreibung = text("beschreibung").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(lizenzTypGlobalId)

    init {
        index(false, lizenzTypCode)
        index(false, lizenzKategorieE)
        index(false, lizenzTypE)
        index(false, sparteE)
        index(false, istAktiv)
    }
}
