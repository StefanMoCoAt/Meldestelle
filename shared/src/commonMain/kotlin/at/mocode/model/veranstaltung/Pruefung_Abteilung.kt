import at.mocode.enums.BeginnzeitTypE
import at.mocode.model.DotierungsAbstufung
import at.mocode.serializers.BigDecimalSerializer
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.KotlinLocalDateSerializer
import at.mocode.serializers.KotlinLocalTimeSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * Repräsentiert eine spezifische Abteilung innerhalb einer Prüfung (`Pruefung_OEPS`).
 * Eine Prüfung kann in eine oder mehrere Abteilungen unterteilt sein, basierend auf
 * Kriterien wie Lizenzstufen, Altersklassen, Pferdeeigenschaften etc.
 * Nennungen, Startlisten und Ergebnislisten beziehen sich primär auf diese Abteilungen.
 *
 * @property pruefungAbteilungDbId Eindeutiger interner Identifikator für diese Abteilung (UUID).
 * @property pruefungDbId Fremdschlüssel zur übergeordneten `Pruefung_OEPS`.
 * @property abteilungsKennzeichen Ein Kennzeichen für diese Abteilung innerhalb der Prüfung (z.B. "1", "A", "R1").
 * Wird oft zur Erstellung der vollständigen Bewerbsnummer verwendet (z.B. "12/1").
 * @property bezeichnungOeffentlich Die offizielle Bezeichnung dieser Abteilung, wie sie auf Listen erscheinen soll
 * (z.B. "R1 Reiter", "Lizenzfrei", "Junioren"). Falls leer, kann sie aus der
 * Bezeichnung der Hauptprüfung und dem Kennzeichen abgeleitet werden.
 * @property bezeichnungIntern Interne Bezeichnung oder Notiz zur Abteilung.
 * @property teilKritMinLizenzStammdatumId Optionaler FK zu `Sportfachliche_Stammdaten` (Typ LIZENZTYP_OEPS_STUFE),
 * der die Mindestlizenz für diese Abteilung definiert.
 * @property teilKritMaxLizenzStammdatumId Optionaler FK für die Maximallizenz.
 * @property teilKritErlaubteLizenzenStammdatenIds Liste von FKs zu `Sportfachliche_Stammdaten` (Typ LIZENZTYP_OEPS),
 * die die exakt erlaubten Lizenzen definieren.
 * @property teilKritMinPferdealter Mindestalter der Pferde für diese Abteilung.
 * @property teilKritMaxPferdealter Maximalalter der Pferde für diese Abteilung.
 * @property teilKritAltersklasseReiterStammdatumId FK zu `Sportfachliche_Stammdaten` (Typ ALTERSCLASSEN_DEFINITION),
 * der die Altersklasse der Reiter definiert.
 * @property teilKritPferderasseStammdatumId FK zu `Sportfachliche_Stammdaten` (Typ PFERDERASSE),
 * der die erlaubte Pferderasse definiert.
 * @property teilKritAnzahlStarterMin Optionale Mindestanzahl an Startern für diese Abteilung.
 * @property teilKritAnzahlStarterMax Optionale Maximalanzahl an Startern für diese Abteilung.
 * @property teilKritFreiTextBeschreibung Freitextliche Beschreibung weiterer oder spezieller Teilungskriterien.
 * @property startgeld Überschreibt ggf. das Standard-Startgeld der `Pruefung_OEPS` für diese Abteilung.
 * @property dotierungen Spezifische Dotierungsabstufungen für diese Abteilung.
 * @property platzId Optionaler Fremdschlüssel zum `Platz`, falls diese Abteilung auf einem anderen Platz
 * als dem Standardplatz der `Pruefung_OEPS` stattfindet.
 * @property datum Optionales spezifisches Datum für diese Abteilung.
 * @property beginnzeitTyp Typ der geplanten Beginnzeit für diese Abteilung.
 * @property beginnzeitFix Fixe Beginnzeit, falls Typ FIX_UM.
 * @property beginnNachAbteilungOderPruefungId Fremdschlüssel zu einer anderen `Pruefung_Abteilung` oder `Pruefung_OEPS`,
 * nach der diese Abteilung starten soll.
 * @property beginnzeitCa Ungefähre Beginnzeit, falls Typ CA_UM.
 * @property dauerProStartGeschaetztSek Geschätzte Dauer pro Starter in Sekunden für diese Abteilung.
 * Überschreibt ggf. Werte aus `Pruefung_OEPS` oder den spartspezifischen Details.
 * @property umbauzeitNachAbteilungMin Umbauzeit in Minuten nach dieser Abteilung.
 * @property besichtigungszeitVorAbteilungMin Besichtigungszeit in Minuten vor dieser Abteilung.
 * @property stechzeitZusaetzlichMin Zusatzzeit für ein eventuelles Stechen in Minuten für diese Abteilung.
 * @property istAktivFuerNennung Gibt an, ob für diese Abteilung Nennungen entgegengenommen werden können.
 * @property istStartlisteFinal Gibt an, ob die Startliste für diese Abteilung finalisiert wurde.
 * @property istErgebnislisteFinal Gibt an, ob die Ergebnisliste für diese Abteilung finalisiert wurde.
 * @property anzahlNennungen Informativ: Aktuelle Anzahl der Nennungen für diese Abteilung.
 * @property anzahlStarterEffektiv Informativ: Tatsächliche Anzahl der Starter.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class Pruefung_Abteilung(
    @Serializable(with = UuidSerializer::class)
    val pruefungAbteilungDbId: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    val pruefungDbId: Uuid, // FK zu Pruefung_OEPS.pruefungDbId

    var abteilungsKennzeichen: String, // z.B. "1", "A", "R1"
    var bezeichnungOeffentlich: String? = null,
    var bezeichnungIntern: String? = null,

    // Strukturierte Teilungskriterien (Verweise auf Sportfachliche_Stammdaten)
    @Serializable(with = UuidSerializer::class)
    var teilKritMinLizenzStammdatumId: Uuid? = null, // Typ LIZENZTYP_OEPS_STUFE
    @Serializable(with = UuidSerializer::class)
    var teilKritMaxLizenzStammdatumId: Uuid? = null, // Typ LIZENZTYP_OEPS_STUFE
    var teilKritErlaubteLizenzenStammdatenIds: List<@Serializable(with = UuidSerializer::class) Uuid>? = null, // Typ LIZENZTYP_OEPS
    var teilKritMinPferdealter: Int? = null,
    var teilKritMaxPferdealter: Int? = null,
    @Serializable(with = UuidSerializer::class)
    var teilKritAltersklasseReiterStammdatumId: Uuid? = null, // Typ ALTERSCLASSEN_DEFINITION
    @Serializable(with = UuidSerializer::class)
    var teilKritPferderasseStammdatumId: Uuid? = null, // Typ PFERDERASSE
    var teilKritAnzahlStarterMin: Int? = null,
    var teilKritAnzahlStarterMax: Int? = null,
    var teilKritFreiTextBeschreibung: String? = null,

    // Abteilungsspezifische Überschreibungen
    @Serializable(with = BigDecimalSerializer::class)
    var startgeld: BigDecimal? = null, // Überschreibt Pruefung_OEPS.startgeldStandard
    var dotierungen: List<DotierungsAbstufung> = emptyList(), // Eigene Dotierung, erbt sonst von Pruefung_OEPS

    // Zeitplanung (überschreibt ggf. Werte aus Pruefung_OEPS oder deren spartspez. Details)
    @Serializable(with = UuidSerializer::class)
    var platzId: Uuid?, // FK zu Platz.platzId
    @Serializable(with = KotlinLocalDateSerializer::class)
    var datum: LocalDate?,
    var beginnzeitTyp: BeginnzeitTypE = BeginnzeitTypE.ANSCHLIESSEND,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    var beginnzeitFix: LocalTime? = null,
    @Serializable(with = UuidSerializer::class)
    var beginnNachAbteilungOderPruefungId: Uuid?, // Kann ID einer anderen Pruefung_Abteilung oder Pruefung_OEPS sein
    @Serializable(with = KotlinLocalTimeSerializer::class)
    var beginnzeitCa: LocalTime? = null,
    var dauerProStartGeschaetztSek: Int? = null,
    var umbauzeitNachAbteilungMin: Int? = null,
    var besichtigungszeitVorAbteilungMin: Int? = null,
    var stechzeitZusaetzlichMin: Int? = null,

    var istAktivFuerNennung: Boolean = true,
    var istStartlisteFinal: Boolean = false,
    var istErgebnislisteFinal: Boolean = false,

    var anzahlNennungen: Int = 0, // Wird dynamisch befüllt oder periodisch aktualisiert
    var anzahlStarterEffektiv: Int = 0, // Wird dynamisch befüllt

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
