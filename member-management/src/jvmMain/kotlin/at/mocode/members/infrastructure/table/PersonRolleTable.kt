package at.mocode.members.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Exposed-Tabellendefinition für die Zuordnung von Rollen zu Personen.
 */
object PersonRolleTable : Table("person_rolle") {
    val id = uuid("id")
    val personId = uuid("person_id")
    val rolleId = uuid("rolle_id").references(RolleTable.id)
    val vereinId = uuid("verein_id").nullable()
    val gueltigVon = date("gueltig_von")
    val gueltigBis = date("gueltig_bis").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val zugewiesenVon = uuid("zugewiesen_von").nullable()
    val notizen = text("notizen").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}
