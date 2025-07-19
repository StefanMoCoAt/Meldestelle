package at.mocode.members.infrastructure.table

import at.mocode.enums.RolleE
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime

/**
 * Exposed-Tabellendefinition für die Rolle-Entität.
 */
object RolleTable : Table("rolle") {
    val id = uuid("id").autoGenerate()
    val rolleTyp = enumeration<RolleE>("rolle_typ")
    val name = varchar("name", 50).uniqueIndex()
    val beschreibung = text("beschreibung").nullable()
    val istSystemRolle = bool("ist_system_rolle").default(false)
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
