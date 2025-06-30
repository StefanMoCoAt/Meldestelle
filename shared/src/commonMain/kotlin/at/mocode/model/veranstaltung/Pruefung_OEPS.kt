import at.mocode.enums.BeginnzeitTypE
import at.mocode.enums.SparteE
import at.mocode.serializers.*
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * Repräsentiert die Basis-Informationen eines Bewerbs (einer Prüfung) innerhalb eines Turniers.
 * Diese Entität enthält spartenübergreifende Attribute. Spartenspezifische Details
 * werden in separaten, verknüpften Entitäten gespeichert (z.B. `DressurPruefungSpezifika`).
 *
 * @property pruefungDbId Eindeutiger interner Identifikator für diese Prüfung/diesen Bewerb (UUID).
 * @property turnierOepsId Fremdschlüssel zum übergeordneten `Turnier_OEPS`.
 * @property oepsBewerbNrAnzeige Die offizielle Nummer des Bewerbs gemäß Ausschreibung (z.B. 1, 12).
 * @property nameTextUebergeordnet Die Hauptbezeichnung des Bewerbs/der Prüfung
 * (z.B. "Dressurprüfung", "Standardspringprüfung").
 * @property sparte Die Pferdesportsparte dieses Bewerbs (DRESSUR, SPRINGEN, etc.).
 * Wird beim Anlegen gesetzt, ggf. vorgeschlagen aus der gewählten ÖTO-Kategorie.
 * @property oepsKategorieStammdatumId Fremdschlüssel zu `Sportfachliche_Stammdaten` (Typ BEWERBSKATEGORIE_OETO),
 * die die spezifische ÖTO-Kategorie dieses Bewerbs definiert (z.B. "CDN-C Neu").
 * @property istDotiert Gibt an, ob dieser Bewerb grundsätzlich mit Geldpreisen dotiert ist.
 * Details zur Dotierung können in der Abteilung oder spartspezifisch festgelegt werden.
 * @property startgeldStandard Ein Standard-Startgeld für diesen Bewerb. Kann von der Abteilung oder
 * spartspezifischen Regeln überschrieben werden.
 * @property oepsBewerbsartCodeZns Optionaler OEPS-Code für die Art des Bewerbs, relevant für den ZNS-Export.
 * @property notizenIntern Interne Notizen oder Kommentare zu diesem Bewerb.
 * @property istAbgesagt Gibt an, ob der Bewerb abgesagt wurde.
 * @property erfordertAbteilungsAuswahlFuerNennung Gibt an, ob für Nennungen zu diesem Bewerb
 * zwingend eine spezifische Abteilung ausgewählt werden muss (true), oder ob Nennungen direkt
 * zum Bewerb (implizit zur Default-Abteilung) erfolgen können (false).
 * @property standardPlatzId Optionaler Default-Austragungsort (FK zu `Platz`) für diesen Bewerb.
 * Kann von der Abteilung überschrieben werden.
 * @property standardDatum Optionales Default-Datum für diesen Bewerb. Kann von der Abteilung überschrieben werden.
 * @property standardBeginnzeitTyp Default-Typ für die geplante Beginnzeit.
 * @property standardBeginnzeitFix Default-fixe Beginnzeit, falls Typ FIX_UM.
 * @property standardBeginnNachPruefungId Default-Verweis auf eine andere Pruefung_OEPS (deren letzte Abteilung),
 * falls Typ ANSCHLIESSEND oder NACH_VORHERIGEM_BEWERB_ABTEILUNG.
 * @property standardBeginnzeitCa Default-ungefähre Beginnzeit, falls Typ CA_UM.
 * @property anzahlAbteilungen Die Anzahl der für diesen Bewerb definierten Abteilungen (informativ).
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class Pruefung_OEPS( // Unsere BewerbBasis
    @Serializable(with = UuidSerializer::class)
    val pruefungDbId: Uuid = uuid4(),

    @Serializable(with = UuidSerializer::class)
    val turnierOepsId: Uuid, // Umbenannt von turnier_db_id für Konsistenz

    var oepsBewerbNrAnzeige: Int, // Deine nummerInAusschreibung
    var nameTextUebergeordnet: String,
    var sparte: SparteE, // Explizit, kann aus oepsKategorieStammdatumId vorgeschlagen werden

    @Serializable(with = UuidSerializer::class)
    var oepsKategorieStammdatumId: Uuid, // FK zu Sportfachliche_Stammdaten (Typ BEWERBSKATEGORIE_OETO)

    var istDotiert: Boolean = false,
    @Serializable(with = BigDecimalSerializer::class)
    var startgeldStandard: BigDecimal? = null,

    var oepsBewerbsartCodeZns: String? = null,
    var notizenIntern: String? = null,
    var istAbgesagt: Boolean = false,
    var erfordertAbteilungsAuswahlFuerNennung: Boolean = true, // Default: Nennung nur für spezifische Abteilung

    // Standard-Zeitplanungswerte (können von Abteilung oder spartspez. Details überschrieben werden)
    @Serializable(with = UuidSerializer::class)
    var standardPlatzId: Uuid? = null,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var standardDatum: LocalDate? = null,
    var standardBeginnzeitTyp: BeginnzeitTypE = BeginnzeitTypE.ANSCHLIESSEND,
    @Serializable(with = KotlinLocalTimeSerializer::class)
    var standardBeginnzeitFix: LocalTime? = null,
    @Serializable(with = UuidSerializer::class)
    var standardBeginnNachPruefungId: Uuid? = null, // Verweis auf Pruefung_OEPS.pruefungDbId
    @Serializable(with = KotlinLocalTimeSerializer::class)
    var standardBeginnzeitCa: LocalTime? = null,

    var anzahlAbteilungen: Int = 0, // Wird berechnet oder bei Erstellung der Abteilungen gesetzt

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
