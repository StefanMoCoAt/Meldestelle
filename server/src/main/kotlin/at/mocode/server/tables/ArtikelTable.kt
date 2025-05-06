package at.mocode.server.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Optimized version of ArtikelTable
 * Changes:
 * - Changed unique index on bezeichnung to non-unique
 * - Added init block for defining indexes
 */
object ArtikelTable : Table(name = "artikel") {
    val id = uuid(name = "id")
    val bezeichnung = varchar(name = "bezeichnung", length = 255)
    val preis = varchar(name = "preis", length = 50)
    val einheit = varchar(name = "einheit", length = 50)
    val istVerbandsabgabe = bool(name = "ist_verbandsabgabe").default(defaultValue = false)
    val createdAt = timestamp(name = "created_at")
    val updatedAt = timestamp(name = "updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(isUnique = false, bezeichnung)
    }
}
