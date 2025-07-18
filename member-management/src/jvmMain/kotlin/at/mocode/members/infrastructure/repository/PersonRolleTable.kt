package at.mocode.members.infrastructure.repository

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Database table definition for person-role assignments (PersonRolle).
 * This is a many-to-many relationship table between persons and roles.
 */
object PersonRolleTable : UUIDTable("person_rolle") {
    val personId = uuid("person_id").references(PersonTable.id)
    val rolleId = uuid("rolle_id").references(RolleTable.id)
    val istAktiv = bool("ist_aktiv").default(true)
    val gueltigVon = datetime("gueltig_von").nullable()
    val gueltigBis = datetime("gueltig_bis").nullable()
    val zugewiesenVon = uuid("zugewiesen_von").nullable() // Person who assigned this role
    val notizen = text("notizen").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    // Unique constraint to prevent duplicate assignments
    init {
        uniqueIndex(personId, rolleId)
    }
}
