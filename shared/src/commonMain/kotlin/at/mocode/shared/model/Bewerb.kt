package at.mocode.shared.model

import at.mocode.shared.enums.BeginnzeitTyp
import at.mocode.shared.enums.Sparte
import at.mocode.shared.serializers.*
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable


@Serializable
data class Bewerb(
    @Serializable(with = UuidSerializer::class)
    val id: Uuid = uuid4(),
    @Serializable(with = UuidSerializer::class)
    val turnierId: Uuid,

    // Allgemeine Infos
    var nummer: String,                     // Offizielle Nummer aus Ausschreibung, z.B. "12"
    var bezeichnungOffiziell: String,       // z.B. "Dressurprüfung Kl. L", "Standardspringprüfung 115cm"
    var internerName: String?,              // Für Listen, falls abweichend/kürzer
    var sparte: Sparte,
    var klasse: String?,                    // z.B. "L", "115cm", "Reiterpass"
    var kategorieOetoDesBewerbs: String?, // ÖTO Kategorie, z.B. "CDN-C Neu". Kann vom Turnier abweichen/spezifischer sein.
    // Wird für die Gültigkeit von Regeln/Lizenzen herangezogen.
    var teilnahmebedingungenText: String? = null, // Freitext für spezielle Teilnahmebedingungen

    // Detail-Informationen (aus den Tabs deines alten Programms)
    var maxPferdeProReiter: Int? = null,
    var pferdealterAnforderung: String? = null, // z.B. "4-jährig", "alle", "5-6j."
    var zusatzTextZeile1: String? = null,       // Für Cup-Namen, Sponsoren etc. auf Ergebnislisten
    var zusatzTextZeile2: String? = null,
    var zusatzTextZeile3: String? = null,
    var logoBewerbUrl: String? = null,
    var parcoursskizzeUrl: String? = null,

    // Bewertung & Aufgabe
    var pruefungsArtDetailName: String?, // Beschreibung der Prüfung aus SUDO "Prüfung" Dropdown
    @Serializable(with = UuidSerializer::class)
    var pruefungsaufgabeId: Uuid?,         // FK zu Pruefungsaufgabe.id (bes. für Dressur)
    @Serializable(with = UuidSerializer::class)
    var richtverfahrenId: Uuid?,           // FK zu Richtverfahren.id
    var anzahlRichterGeplant: Int? = 1,
    var paraGradeAnforderung: String? = null,
    var istManuellKalkuliert: Boolean = false, // Für Ergebnisberechnung

    // Geldpreis/Dotierung
    var istDotiert: Boolean = false,
    @Serializable(with = BigDecimalSerializer::class)
    var startgeldStandard: BigDecimal? = null, // Standard-Startgeld für diesen Bewerb
    @Serializable(with = BigDecimalSerializer::class)
    var startgeldKaderreiter: BigDecimal? = null,
    var auszahlungsModusGeldpreis: String? = null,
    var hatGeldpreisFuerKaderreiter: Boolean = false,
    @Serializable(with = UuidSerializer::class)
    var geldpreisVorlageId: Uuid?, // FK zu einer GeldpreisVorlagen-Tabelle (optional für später)
    var dotierungenManuell: List<DotierungsAbstufung> = emptyList(),

    // Ort/Zeit (Default-Werte, können pro Abteilung überschrieben werden)
    @Serializable(with = UuidSerializer::class)
    var standardPlatzId: Uuid?, // FK zum Default-Austragungsplatz
    @Serializable(with = KotlinLocalDateSerializer::class)
    var standardDatum: LocalDate?,
    var standardBeginnzeitTyp: BeginnzeitTyp = BeginnzeitTyp.ANSCHLIESSEND, // neuer Enum Wert
    @Serializable(with = KotlinLocalTimeSerializer::class)
    var standardBeginnzeitFix: LocalTime? = null,
    @Serializable(with = UuidSerializer::class)
    var standardBeginnNachBewerbId: Uuid?,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    var standardBeginnzeitCa: LocalTime? = null,
    var standardDauerProStartGeschaetztSek: Int? = 120,
    var standardUmbauzeitNachBewerbMin: Int? = 10,
    var standardBesichtigungszeitVorBewerbMin: Int? = 10,
    var standardStechzeitZusaetzlichMin: Int? = 0,

    // ÖTO/ZNS Spezifika
    var oepsBewerbsartCodeZns: String? = null,
    var oepsAltersklasseCodeZns: String? = null,
    var oepsPferderassenCodeZns: String? = null,

    // Steuerung
    var notizenIntern: String? = null,
    var istStartlisteFinal: Boolean = false,
    var istErgebnislisteFinal: Boolean = false,
    var erfordertAbteilungsAuswahlFuerNennung: Boolean = true, // Standardmäßig ja, außer es gibt nur eine Default-Abteilung

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)

// Enum BeginnzeitTypEnum um ANSCHLIESSEND erweitern

// ANSCHLIESSEND wäre der Standard, wenn keine explizite Zeit oder "nach Bewerb X" angegeben ist.
