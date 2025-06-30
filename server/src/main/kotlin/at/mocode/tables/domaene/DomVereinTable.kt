package at.mocode.tables.domaene

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object DomVereinTable : Table("dom_vereine") {
    val vereinId = uuid("verein_id")
    val oepsVereinsNr = varchar("oeps_vereins_nr", 10).nullable().uniqueIndex()
    val name = varchar("name", 255)
    val kuerzel = varchar("kuerzel", 50).nullable()

    val adresseStrasse = varchar("adresse_strasse", 255).nullable()
    val plz = varchar("plz", 20).nullable()
    val ort = varchar("ort", 255).nullable()

    val bundeslandId = uuid("bundesland_id").nullable() // FK to BundeslandDefinition when implemented
    val landId = uuid("land_id") // FK to LandDefinition when implemented

    val emailAllgemein = varchar("email_allgemein", 255).nullable()
    val telefonAllgemein = varchar("telefon_allgemein", 50).nullable()
    val webseiteUrl = varchar("webseite_url", 500).nullable()

    val datenQuelle = varchar("daten_quelle", 50).default("OEPS_ZNS")
    val istAktiv = bool("ist_aktiv").default(true)
    val notizenIntern = text("notizen_intern").nullable()

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(vereinId)

    init {
        index(false, name)
        index(false, oepsVereinsNr)
        index(false, bundeslandId)
        index(false, landId)
        index(false, istAktiv)
        index(false, datenQuelle)
    }
}
