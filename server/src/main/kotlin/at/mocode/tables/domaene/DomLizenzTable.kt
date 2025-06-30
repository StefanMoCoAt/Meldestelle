package at.mocode.tables.domaene

import at.mocode.tables.stammdaten.PersonenTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object DomLizenzTable : Table("dom_lizenzen") {
    val lizenzId = uuid("lizenz_id")
    val personId = uuid("person_id").references(PersonenTable.id)
    val lizenzTypGlobalId = uuid("lizenz_typ_global_id") // FK to LizenzTypGlobal when implemented
    val gueltigBisJahr = integer("gueltig_bis_jahr").nullable()
    val ausgestelltAm = date("ausgestellt_am").nullable()
    val istAktivBezahltOeps = bool("ist_aktiv_bezahlt_oeps").default(false)
    val notiz = text("notiz").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(lizenzId)

    init {
        index(false, personId)
        index(false, lizenzTypGlobalId)
        index(false, gueltigBisJahr)
        index(false, istAktivBezahltOeps)
    }
}
