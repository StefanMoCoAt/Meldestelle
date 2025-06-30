package at.mocode.tables.domaene

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object DomQualifikationTable : Table("dom_qualifikationen") {
    val qualifikationId = uuid("qualifikation_id")
    val personId = uuid("person_id") // FK to DomPerson when implemented
    val qualTypId = uuid("qual_typ_id") // FK to QualifikationsTyp when implemented
    val bemerkung = text("bemerkung").nullable()
    val gueltigVon = date("gueltig_von").nullable()
    val gueltigBis = date("gueltig_bis").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(qualifikationId)

    init {
        index(false, personId)
        index(false, qualTypId)
        index(false, istAktiv)
        index(false, gueltigVon)
        index(false, gueltigBis)
    }
}
