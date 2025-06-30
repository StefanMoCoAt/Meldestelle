package at.mocode.tables.domaene

import at.mocode.enums.DatenQuelleE
import at.mocode.enums.PferdeGeschlechtE
import at.mocode.tables.PersonenTable
import at.mocode.tables.VereineTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object DomPferdTable : Table("dom_pferde") {
    val pferdId = uuid("pferd_id")
    val oepsSatzNrPferd = varchar("oeps_satz_nr_pferd", 15).uniqueIndex().nullable()
    val oepsKopfNr = varchar("oeps_kopf_nr", 10).nullable()
    val name = varchar("name", 255)
    val lebensnummer = varchar("lebensnummer", 20).nullable()
    val feiPassNr = varchar("fei_pass_nr", 20).nullable()
    val geburtsjahr = integer("geburtsjahr").nullable()
    val geschlecht = enumerationByName("geschlecht", 20, PferdeGeschlechtE::class).nullable()
    val farbe = varchar("farbe", 50).nullable()
    val rasse = varchar("rasse", 100).nullable()
    val abstammungVaterName = varchar("abstammung_vater_name", 255).nullable()
    val abstammungMutterName = varchar("abstammung_mutter_name", 255).nullable()
    val abstammungMutterVaterName = varchar("abstammung_mutter_vater_name", 255).nullable()
    val abstammungZusatzInfo = text("abstammung_zusatz_info").nullable()
    val besitzerPersonId = uuid("besitzer_person_id").references(PersonenTable.id).nullable()
    val verantwortlichePersonId = uuid("verantwortliche_person_id").references(PersonenTable.id).nullable()
    val heimatVereinId = uuid("heimat_verein_id").references(VereineTable.id).nullable()
    val letzteZahlungPferdegebuehrJahrOeps = integer("letzte_zahlung_pferdegebuehr_jahr_oeps").nullable()
    val stockmassCm = integer("stockmass_cm").nullable()
    val datenQuelle = enumerationByName("daten_quelle", 20, DatenQuelleE::class).default(DatenQuelleE.MANUELL)
    val istAktiv = bool("ist_aktiv").default(true)
    val notizenIntern = text("notizen_intern").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(pferdId)

    init {
        index(false, name)
        index(false, oepsSatzNrPferd)
        index(false, lebensnummer)
        index(false, besitzerPersonId)
        index(false, verantwortlichePersonId)
        index(false, heimatVereinId)
        index(false, rasse)
        index(false, geburtsjahr)
        index(false, istAktiv)
        index(false, datenQuelle)
    }
}
