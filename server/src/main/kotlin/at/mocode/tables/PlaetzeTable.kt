package at.mocode.tables

import at.mocode.shared.enums.PlatzTypE
import org.jetbrains.exposed.sql.Table

object PlaetzeTable : Table("plaetze") {
    val id = uuid("id")
    val turnierId = uuid("turnier_id").references(TurniereTable.id)
    val name = varchar("name", 100)
    val dimension = varchar("dimension", 50).nullable()
    val boden = varchar("boden", 100).nullable()
    val typ = enumerationByName("typ", 20, PlatzTypE::class)

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, turnierId)
        index(false, name)
        index(false, typ)
    }
}
