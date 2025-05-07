package at.mocode.server.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Optimized version of ArtikelTable
 * Changes:
 * - Changed unique index on bezeichnung to non-unique
 * - Added init block for defining indexes
 */
object ArtikelTable : Table("artikel") {
    val id = uuid("id")
    val bezeichnung = varchar("bezeichnung", 255)
    val preis = varchar("preis", 50)
    val einheit = varchar("einheit", 50)
    val istVerbandsabgabe = bool("ist_verbandsabgabe").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, bezeichnung)
    }
}
