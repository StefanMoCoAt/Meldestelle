package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PruefungsaufgabeTable : Table("pruefungsaufgaben") {
    val id = uuid("id")
    val kuerzel = varchar("kuerzel", 100)
    val nameLang = varchar("name_lang", 500)
    val kategorieText = varchar("kategorie_text", 255).nullable()
    val sparteE = varchar("sparte", 50)
    val nation = varchar("nation", 50).default("NATIONAL")
    val richtverfahrenModusDefault = varchar("richtverfahren_modus_default", 50).nullable()
    val viereckGroesseDefault = varchar("viereck_groesse_default", 50).nullable()
    val schwierigkeitsgradText = varchar("schwierigkeitsgrad_text", 100).nullable()
    val aufgabenNummerInSammlung = varchar("aufgaben_nummer_in_sammlung", 100).nullable()
    val jahrgangVersion = varchar("jahrgang_version", 100).nullable()
    val pdfUrlExtern = varchar("pdf_url_extern", 500).nullable()
    val pdfDateinameIntern = varchar("pdf_dateiname_intern", 255).nullable()
    val anmerkungen = text("anmerkungen").nullable()
    val dauerGeschaetztMinuten = double("dauer_geschaetzt_minuten").nullable()
    val anzahlMaxPunkteProRichter = double("anzahl_max_punkte_pro_richter").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, kuerzel)
        index(false, sparteE)
        index(false, nation)
        index(false, istAktiv)
        index(false, schwierigkeitsgradText)
    }
}
