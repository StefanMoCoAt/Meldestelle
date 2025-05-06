package at.mocode.server.tables

import at.mocode.server.enums.PlatzTyp
import org.jetbrains.exposed.sql.Table

/**
 * Optimized version of PlaetzeTable
 * Changes:
 * - Added proper imports for enums
 * - Added index for name field
 */
object PlaetzeTable : Table(name = "plaetze") {
    val id = uuid(name = "id")
    val turnierId = uuid(name = "turnier_id").references(ref = TurniereTable.id)
    val name = varchar(name = "name", length = 100)
    val dimension = varchar(name = "dimension", length = 50).nullable()
    val boden = varchar(name = "boden", length = 100).nullable()
    val typ = enumerationByName(name = "typ", length = 20, klass = PlatzTyp::class)

    override val primaryKey = PrimaryKey(firstColumn = id)

    init {
        index(isUnique = false, turnierId)
        index(isUnique = false, name)
        index(isUnique = false, typ)
    }
}
