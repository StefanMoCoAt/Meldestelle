package at.mocode.tables

import at.mocode.model.enums.VeranstalterTyp
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// --- Tabelle für Veranstaltungen ---
object VeranstaltungenTable : Table("veranstaltungen") {
    val id = uuid("id") // KMP Uuid -> DB UUID
    val name = varchar("name", 255)
    val datumVon = date("datum_von") // kotlinx.datetime.LocalDate
    val datumBis = date("datum_bis") // kotlinx.datetime.LocalDate

    // Veranstalter Infos
    val veranstalterName = varchar("veranstalter_name", 255)
    val veranstalterOepsNummer = varchar("veranstalter_oeps_nr", 10).nullable()
    val veranstalterTyp =
        enumerationByName("veranstalter_typ", 20, VeranstalterTyp::class).default(VeranstalterTyp.UNBEKANNT)

    // Ort Infos
    val veranstaltungsortName = varchar("veranstaltungsort_name", 255)
    val veranstaltungsortAdresse = varchar("veranstaltungsort_adresse", 500)

    // Kontakt Infos
    val kontaktpersonName = varchar("kontaktperson_name", 200).nullable()
    val kontaktTelefon = varchar("kontakt_telefon", 50).nullable()
    val kontaktEmail = varchar("kontakt_email", 255).nullable()

    // Weitere Infos
    val webseite = varchar("webseite", 500).nullable()
    val logoUrl = varchar("logo_url", 500).nullable()
    val anfahrtsplanInfo = text("anfahrtsplan_info").nullable()

    // Sponsoren als einfacher Text (CSV oder ähnlich)
    val sponsorInfosCsv = text("sponsor_infos_csv").nullable()

    // Rechtliche Texte
    val dsgvoText = text("dsgvo_text").nullable()
    val haftungsText = text("haftungs_text").nullable()
    val sonstigeBesondereBestimmungen = text("sonstige_bestimmungen").nullable()

    // Timestamps
    val createdAt = timestamp("created_at") // kotlinx.datetime.Instant
    val updatedAt = timestamp("updated_at") // kotlinx.datetime.Instant

    override val primaryKey = PrimaryKey(id)
}
