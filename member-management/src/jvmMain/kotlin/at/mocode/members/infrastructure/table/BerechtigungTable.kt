package at.mocode.members.infrastructure.table

import at.mocode.enums.BerechtigungE
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime

/**
 * Exposed-Tabellendefinition für die Berechtigung-Entität.
 */
object BerechtigungTable : Table("berechtigung") {
    val id = uuid("id").autoGenerate()
    val berechtigungTyp = enumerationByName<BerechtigungE>("berechtigung_typ", 50)
    val name = varchar("name", 100)
    val beschreibung = text("beschreibung").nullable()
    val ressource = varchar("ressource", 50)
    val aktion = varchar("aktion", 50)
    val istAktiv = bool("ist_aktiv").default(true)
    val istSystemBerechtigung = bool("ist_system_berechtigung").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(berechtigungTyp)
    }
}
