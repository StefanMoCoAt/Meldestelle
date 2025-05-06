package at.mocode.server.tables

import at.mocode.server.enums.VeranstalterTyp
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Optimized version of VeranstaltungenTable
 * Changes:
 * - Added proper imports for enums
 * - Added indexes for common search fields
 * - Added init block for defining indexes
 */
object VeranstaltungenTable : Table(name = "veranstaltungen") {
    val id = uuid(name = "id")
    val name = varchar(name = "name", length = 255)
    val datumVon = date(name = "datum_von")
    val datumBis = date(name = "datum_bis")
    val veranstalterName = varchar(name = "veranstalter_name", length = 255)
    val veranstalterOepsNummer = varchar(name = "veranstalter_oeps_nr", length = 10).nullable()
    val veranstalterTyp =
        enumerationByName(name = "veranstalter_typ", length = 20, klass = VeranstalterTyp::class).default(
            VeranstalterTyp.UNBEKANNT
        )
    val veranstaltungsortName = varchar(name = "veranstaltungsort_name", length = 255)
    val veranstaltungsortAdresse = varchar(name = "veranstaltungsort_adresse", length = 500)
    val kontaktpersonName = varchar(name = "kontaktperson_name", length = 200).nullable()
    val kontaktTelefon = varchar(name = "kontakt_telefon", length = 50).nullable()
    val kontaktEmail = varchar(name = "kontakt_email", length = 255).nullable()
    val webseite = varchar(name = "webseite", length = 500).nullable()
    val logoUrl = varchar(name = "logo_url", length = 500).nullable()
    val anfahrtsplanInfo = text(name = "anfahrtsplan_info").nullable()
    val sponsorInfosCsv = text(name = "sponsor_infos_csv").nullable()
    val dsgvoText = text(name = "dsgvo_text").nullable()
    val haftungsText = text(name = "haftungs_text").nullable()
    val sonstigeBesondereBestimmungen = text(name = "sonstige_bestimmungen").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(isUnique = false, name)
        index(isUnique = false, datumVon, datumBis)
        index(isUnique = false, veranstalterName)
        index(isUnique = false, veranstaltungsortName)
    }
}
