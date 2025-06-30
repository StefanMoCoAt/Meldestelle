package at.mocode.tables

import org.jetbrains.exposed.sql.Table

object DotierungsAbstufungTable : Table("dotierungs_abstufungen") {
    val id = uuid("id")
    val platz = integer("platz")
    val betrag = decimal("betrag", 10, 2)
    val beschreibung = varchar("beschreibung", 255).nullable()

    // Foreign key to link to Bewerb or Abteilung
    val bewerbId = uuid("bewerb_id").nullable() // FK to Bewerb when implemented
    val abteilungId = uuid("abteilung_id").nullable() // FK to Abteilung when implemented

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, platz)
        index(false, bewerbId)
        index(false, abteilungId)
    }
}
