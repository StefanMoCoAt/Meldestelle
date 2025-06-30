package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BewerbTable : Table("bewerbe") {
    val id = uuid("id")
    val turnierId = uuid("turnier_id").references(TurniereTable.id)

    // Allgemeine Infos
    val nummer = varchar("nummer", 50)
    val bezeichnungOffiziell = varchar("bezeichnung_offiziell", 500)
    val internerName = varchar("interner_name", 255).nullable()
    val sparteE = varchar("sparte", 50)
    val klasse = varchar("klasse", 100).nullable()
    val kategorieOetoDesBewerbs = varchar("kategorie_oeto_des_bewerbs", 255).nullable()
    val teilnahmebedingungenText = text("teilnahmebedingungen_text").nullable()

    // Detail-Informationen
    val maxPferdeProReiter = integer("max_pferde_pro_reiter").nullable()
    val pferdealterAnforderung = varchar("pferdealter_anforderung", 255).nullable()
    val zusatzTextZeile1 = varchar("zusatz_text_zeile1", 255).nullable()
    val zusatzTextZeile2 = varchar("zusatz_text_zeile2", 255).nullable()
    val zusatzTextZeile3 = varchar("zusatz_text_zeile3", 255).nullable()
    val logoBewerbUrl = varchar("logo_bewerb_url", 500).nullable()
    val parcoursskizzeUrl = varchar("parcoursskizze_url", 500).nullable()

    // Bewertung & Aufgabe
    val pruefungsArtDetailName = varchar("pruefungs_art_detail_name", 255).nullable()
    val pruefungsaufgabeId = uuid("pruefungsaufgabe_id").nullable() // FK to Pruefungsaufgabe when implemented
    val richtverfahrenId = uuid("richtverfahren_id").nullable() // FK to Richtverfahren when implemented
    val anzahlRichterGeplant = integer("anzahl_richter_geplant").default(1)
    val paraGradeAnforderung = varchar("para_grade_anforderung", 255).nullable()
    val istManuellKalkuliert = bool("ist_manuell_kalkuliert").default(false)

    // Geldpreis/Dotierung
    val istDotiert = bool("ist_dotiert").default(false)
    val startgeldStandard = decimal("startgeld_standard", 10, 2).nullable()
    val startgeldKaderreiter = decimal("startgeld_kaderreiter", 10, 2).nullable()
    val auszahlungsModusGeldpreis = varchar("auszahlungs_modus_geldpreis", 255).nullable()
    val hatGeldpreisFuerKaderreiter = bool("hat_geldpreis_fuer_kaderreiter").default(false)
    val geldpreisVorlageId = uuid("geldpreis_vorlage_id").nullable()

    // Ort/Zeit (Default-Werte)
    val standardPlatzId = uuid("standard_platz_id").nullable().references(PlaetzeTable.id)
    val standardDatum = date("standard_datum").nullable()
    val standardBeginnzeitTypE = varchar("standard_beginnzeit_typ", 50).default("ANSCHLIESSEND")
    val standardBeginnzeitFix = time("standard_beginnzeit_fix").nullable()
    val standardBeginnNachBewerbId = uuid("standard_beginn_nach_bewerb_id").nullable()
    val standardBeginnzeitCa = time("standard_beginnzeit_ca").nullable()
    val standardDauerProStartGeschaetztSek = integer("standard_dauer_pro_start_geschaetzt_sek").default(120)
    val standardUmbauzeitNachBewerbMin = integer("standard_umbauzeit_nach_bewerb_min").default(10)
    val standardBesichtigungszeitVorBewerbMin = integer("standard_besichtigungszeit_vor_bewerb_min").default(10)
    val standardStechzeitZusaetzlichMin = integer("standard_stechzeit_zusaetzlich_min").default(0)

    // ÖTO/ZNS Spezifika
    val oepsBewerbsartCodeZns = varchar("oeps_bewerbsart_code_zns", 100).nullable()
    val oepsAltersklasseCodeZns = varchar("oeps_altersklasse_code_zns", 100).nullable()
    val oepsPferderassenCodeZns = varchar("oeps_pferderassen_code_zns", 100).nullable()

    // Steuerung
    val notizenIntern = text("notizen_intern").nullable()
    val istStartlisteFinal = bool("ist_startliste_final").default(false)
    val istErgebnislisteFinal = bool("ist_ergebnisliste_final").default(false)
    val erfordertAbteilungsAuswahlFuerNennung = bool("erfordert_abteilungs_auswahl_fuer_nennung").default(true)

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, turnierId)
        index(false, nummer)
        index(false, sparteE)
        index(false, standardPlatzId)
        index(false, istStartlisteFinal)
        index(false, istErgebnislisteFinal)
    }
}
