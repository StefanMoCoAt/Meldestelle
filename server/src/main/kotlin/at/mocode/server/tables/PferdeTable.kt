package at.mocode.server.tables

import at.mocode.shared.model.enums.GeschlechtPferd
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PferdeTable : Table("pferde") {
    val id = uuid("id")
    val oepsKopfNr = varchar("oeps_kopf_nr", 10).uniqueIndex().nullable()
    val oepsSatzNr = varchar("oeps_satz_nr", 15).uniqueIndex().nullable()
    val name = varchar("name", 255)
    val lebensnummer = varchar("lebensnummer", 20).nullable()
    val feiPassNr = varchar("fei_pass_nr", 20).nullable()
    val geschlecht = enumerationByName("geschlecht", 10, GeschlechtPferd::class).nullable()
    val geburtsjahr = integer("geburtsjahr").nullable()
    val rasse = varchar("rasse", 100).nullable()
    val farbe = varchar("farbe", 50).nullable()
    val vaterName = varchar("vater_name", 255).nullable()
    val mutterName = varchar("mutter_name", 255).nullable()
    val mutterVaterName = varchar("mutter_vater_name", 255).nullable()
    val besitzerId = uuid("besitzer_id").references(PersonenTable.id).nullable()
    val verantwortlichePersonId = uuid("verantwortliche_person_id").references(PersonenTable.id).nullable()
    val heimatVereinId = uuid("heimat_verein_id").references(VereineTable.id).nullable()
    val letzteZahlungJahrOeps = integer("letzte_zahlung_jahr_oeps").nullable()
    val stockmassCm = integer("stockmass_cm").nullable()
    val istAktiv = bool("ist_aktiv").default(defaultValue = true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, rasse)
        index(false, besitzerId)
        index(false, verantwortlichePersonId)
        index(false, heimatVereinId)
        index(false, name)
    }
}
