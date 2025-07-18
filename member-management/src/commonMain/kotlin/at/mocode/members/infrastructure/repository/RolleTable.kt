package at.mocode.members.infrastructure.repository

import at.mocode.enums.RolleE
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Exposed table definition for Rolle entities.
 *
 * This table represents the database schema for storing role data
 * in the member management bounded context.
 */
object RolleTable : UUIDTable("rollen") {

    // Role identification
    val rolleTyp = enumerationByName("rolle_typ", 20, RolleE::class)
    val name = varchar("name", 100)
    val beschreibung = text("beschreibung").nullable()

    // Status flags
    val istAktiv = bool("ist_aktiv").default(true)
    val istSystemRolle = bool("ist_system_rolle").default(false)

    // Audit fields
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    // Unique constraint on rolle_typ to ensure each role type exists only once
    init {
        uniqueIndex(rolleTyp)
    }
}
