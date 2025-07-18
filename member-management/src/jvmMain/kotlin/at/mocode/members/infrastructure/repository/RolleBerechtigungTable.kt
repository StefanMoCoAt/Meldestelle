package at.mocode.members.infrastructure.repository

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Database table definition for role-permission assignments (RolleBerechtigung).
 * This is a many-to-many relationship table between roles and permissions.
 */
object RolleBerechtigungTable : UUIDTable("rolle_berechtigung") {
    val rolleId = uuid("rolle_id").references(RolleTable.id)
    val berechtigungId = uuid("berechtigung_id").references(BerechtigungTable.id)
    val istAktiv = bool("ist_aktiv").default(true)
    val gueltigVon = datetime("gueltig_von").nullable()
    val gueltigBis = datetime("gueltig_bis").nullable()
    val zugewiesenVon = uuid("zugewiesen_von").nullable() // Person who assigned this permission
    val notizen = text("notizen").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    // Unique constraint to prevent duplicate assignments
    init {
        uniqueIndex(rolleId, berechtigungId)
    }
}
