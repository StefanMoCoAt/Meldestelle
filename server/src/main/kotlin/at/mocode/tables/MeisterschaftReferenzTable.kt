package at.mocode.tables

import org.jetbrains.exposed.sql.Table

object MeisterschaftReferenzTable : Table("meisterschaft_referenzen") {
    val id = uuid("id")
    val meisterschaftId = uuid("meisterschaft_id") // FK to Meisterschaft when implemented
    val name = varchar("name", 255)
    val betrifftBewerbNummern = text("betrifft_bewerb_nummern") // JSON array as text
    val berechnungsstrategie = varchar("berechnungsstrategie", 255).nullable()
    val reglementUrl = varchar("reglement_url", 500).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, meisterschaftId)
        index(false, name)
    }
}

object CupReferenzTable : Table("cup_referenzen") {
    val id = uuid("id")
    val cupId = uuid("cup_id") // FK to Cup when implemented
    val name = varchar("name", 255)
    val betrifftBewerbNummern = text("betrifft_bewerb_nummern") // JSON array as text
    val berechnungsstrategie = varchar("berechnungsstrategie", 255).nullable()
    val reglementUrl = varchar("reglement_url", 500).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, cupId)
        index(false, name)
    }
}

object SonderpruefungReferenzTable : Table("sonderpruefung_referenzen") {
    val id = uuid("id")
    val cupId = uuid("cup_id") // FK to Cup when implemented
    val name = varchar("name", 255)
    val betrifftBewerbNummern = text("betrifft_bewerb_nummern") // JSON array as text
    val berechnungsstrategie = varchar("berechnungsstrategie", 255).nullable()
    val reglementUrl = varchar("reglement_url", 500).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, cupId)
        index(false, name)
    }
}
