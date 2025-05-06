package at.mocode.server.tables

import at.mocode.server.enums.GeschlechtPferd
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Optimized version of PferdeTable
 * Changes:
 * - Added proper imports for enums
 * - Added indexes for foreign key fields
 * - Added index for common search fields (name, rasse)
 */
object PferdeTable : Table(name = "pferde") {
    val id = uuid(name = "id")
    val oepsKopfNr = varchar(name = "oeps_kopf_nr", length = 10).uniqueIndex().nullable()
    val oepsSatzNr = varchar(name = "oeps_satz_nr", length = 15).uniqueIndex().nullable()
    val name = varchar(name = "name", length = 255)
    val lebensnummer = varchar(name = "lebensnummer", length = 20).nullable()
    val feiPassNr = varchar(name = "fei_pass_nr", length = 20).nullable()
    val geschlecht = enumerationByName(name = "geschlecht", length = 10, klass = GeschlechtPferd::class).nullable()
    val geburtsjahr = integer(name = "geburtsjahr").nullable()
    val rasse = varchar(name = "rasse", length = 100).nullable()
    val farbe = varchar(name = "farbe", length = 50).nullable()
    val vaterName = varchar(name = "vater_name", length = 255).nullable()
    val mutterName = varchar(name = "mutter_name", length = 255).nullable()
    val mutterVaterName = varchar(name = "mutter_vater_name", length = 255).nullable()
    val besitzerId = uuid(name = "besitzer_id").references(ref = PersonenTable.id).nullable()
    val verantwortlichePersonId = uuid(name = "verantwortliche_person_id").references(ref = PersonenTable.id).nullable()
    val heimatVereinId = uuid(name = "heimat_verein_id").references(ref = VereineTable.id).nullable()
    val letzteZahlungJahrOeps = integer(name = "letzte_zahlung_jahr_oeps").nullable()
    val stockmassCm = integer(name = "stockmass_cm").nullable()
    val istAktiv = bool(name = "ist_aktiv").default(defaultValue = true)
    val createdAt = timestamp(name = "created_at")
    val updatedAt = timestamp(name = "updated_at")

    override val primaryKey = PrimaryKey(firstColumn = id)

    init {
        index(isUnique = false, name)
        index(isUnique = false, rasse)
        index(isUnique = false, besitzerId)
        index(isUnique = false, verantwortlichePersonId)
        index(isUnique = false, heimatVereinId)
    }
}
