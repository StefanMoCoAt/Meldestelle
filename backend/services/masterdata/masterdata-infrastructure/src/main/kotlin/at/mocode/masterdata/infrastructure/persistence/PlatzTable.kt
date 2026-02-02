package at.mocode.masterdata.infrastructure.persistence

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.kotlin.datetime.datetime
import org.jetbrains.exposed.v1.core.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.core.javaUUID

/**
 * Exposed-Tabellendefinition für die Platz-Entität (Turnierplätze/Wettkampfstätten).
 *
 * Diese Tabelle speichert alle Informationen zu Plätzen und Arenen
 * entsprechend der Platz Domain-Entität.
 */
object PlatzTable : Table("platz") {
    val id = javaUUID("id").autoGenerate()
    val turnierId = javaUUID("turnier_id") // Foreign key to tournament (not enforced here as tournament might be in different module)
    val name = varchar("name", 200)
    val dimension = varchar("dimension", 50).nullable()
    val boden = varchar("boden", 100).nullable()
    val typ = varchar("typ", 50) // Enum as string
    val istAktiv = bool("ist_aktiv").default(true)
    val sortierReihenfolge = integer("sortier_reihenfolge").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)

    init {
        // Index for performance on common queries
        index(customIndexName = "idx_platz_turnier", columns = arrayOf(turnierId))
        index(customIndexName = "idx_platz_aktiv", columns = arrayOf(istAktiv))
        index(customIndexName = "idx_platz_typ", columns = arrayOf(typ))
        index(customIndexName = "idx_platz_turnier_aktiv", columns = arrayOf(turnierId, istAktiv))

        // Unique constraint for name per tournament
        uniqueIndex("uk_platz_name_turnier", name, turnierId)
    }
}
