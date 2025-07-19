package at.mocode.members.infrastructure.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime

/**
 * Exposed-Tabellendefinition für die Zuordnung von Berechtigungen zu Rollen.
 */
object RolleBerechtigungTable : Table("rolle_berechtigung") {
    val id = uuid("id").autoGenerate()
    val rolleId = uuid("rolle_id").references(RolleTable.id)
    val berechtigungId = uuid("berechtigung_id").references(BerechtigungTable.id)
    val istAktiv = bool("ist_aktiv").default(true)
    val zugewiesenVon = uuid("zugewiesen_von").nullable()
    val notizen = text("notizen").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)

    init {
        // Unique constraint on role-permission combination
        uniqueIndex(rolleId, berechtigungId)
    }
}
