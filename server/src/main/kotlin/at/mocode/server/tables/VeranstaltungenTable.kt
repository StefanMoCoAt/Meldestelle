package at.mocode.server.tables

import at.mocode.shared.model.enums.VeranstalterTyp
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object VeranstaltungenTable : Table("veranstaltungen") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val datumVon = date("datum_von")
    val datumBis = date("datum_bis")
    val veranstalterName = varchar("veranstalter_name", 255)
    val veranstalterOepsNummer = varchar("veranstalter_oeps_nr", 10).nullable()
    val veranstalterTyp =
        enumerationByName("veranstalter_typ", 20, VeranstalterTyp::class).default(
            VeranstalterTyp.UNBEKANNT
        )
    val veranstaltungsortName = varchar("veranstaltungsort_name", 255)
    val veranstaltungsortAdresse = varchar("veranstaltungsort_adresse", 500)
    val kontaktpersonName = varchar("kontaktperson_name", 200).nullable()
    val kontaktTelefon = varchar("kontakt_telefon", 50).nullable()
    val kontaktEmail = varchar("kontakt_email", 255).nullable()
    val webseite = varchar("webseite", 500).nullable()
    val logoUrl = varchar("logo_url", 500).nullable()
    val anfahrtsplanInfo = text("anfahrtsplan_info").nullable()
    val sponsorInfosCsv = text("sponsor_infos_csv").nullable()
    val dsgvoText = text("dsgvo_text").nullable()
    val haftungsText = text("haftungs_text").nullable()
    val sonstigeBesondereBestimmungen = text("sonstige_bestimmungen").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, name)
        index(false, datumVon, datumBis)
        index(false, veranstalterName)
        index(false, veranstaltungsortName)
    }
}
