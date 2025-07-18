package at.mocode.members.infrastructure.repository

import at.mocode.enums.BerechtigungE
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * Database table definition for permissions (Berechtigungen).
 */
object BerechtigungTable : UUIDTable("berechtigung") {
    val berechtigungTyp = enumerationByName("berechtigung_typ", 50, BerechtigungE::class)
    val name = varchar("name", 100)
    val beschreibung = text("beschreibung").nullable()
    val ressource = varchar("ressource", 50)
    val aktion = varchar("aktion", 50)
    val istAktiv = bool("ist_aktiv").default(true)
    val istSystemBerechtigung = bool("ist_system_berechtigung").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
