import at.mocode.shared.enums.PruefungsViereckE
import at.mocode.shared.enums.RichterPositionE
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Speichert die spezifischen Details und Anforderungen für eine Dressurprüfung,
 * die auf einer Basis-Prüfung (`Pruefung_OEPS`) aufbaut.
 *
 * @property pruefungDbId Eindeutiger Identifikator, der auch der Fremdschlüssel zur
 * zugehörigen `Pruefung_OEPS` ist (1:1-Beziehung).
 * @property aufgabeStammdatumId Fremdschlüssel zu `Sportfachliche_Stammdaten` (Typ DRESSURAUFGABE),
 * die die zu reitende Dressuraufgabe definiert.
 * @property klasseStammdatumId Optionaler Fremdschlüssel zu `Sportfachliche_Stammdaten` (Typ BEWERBSKLASSE),
 * der die Leistungsklasse dieser Dressurprüfung definiert (z.B. "A", "L", "M").
 * @property richtverfahrenStammdatumId Fremdschlüssel zu `Sportfachliche_Stammdaten`
 * (Typ WERTUNGSVERFAHREN_DRESSUR), der das anzuwendende Richtverfahren (z.B. "GM", "GT") definiert.
 * @property viereckGroesse Die vorgeschriebene Größe des Dressurvierecks für diese Prüfung.
 * @property erlaubeAuswendigReiten Gibt an, ob das Reiten der Aufgabe auswendig erlaubt/vorgeschrieben ist.
 * @property erlaubeVorlesen Gibt an, ob das Vorlesen der Aufgabe erlaubt ist.
 * @property anzahlRichterGeplant Die geplante Anzahl der Richter für diese Prüfung.
 * @property maxPunkteProRichterMöglich Maximale Punktzahl, die pro Richter erreicht werden kann (falls relevant und nicht in der Aufgabe definiert).
 * @property geplanteRichterpositionen Liste der geplanten Richterpositionen für diese Prüfung (z.B. [C, M, H]).
 * Die tatsächliche Zuweisung von Personen erfolgt über `FunktionaerEinsatzPlanung` oder `BewerbFunktionaerZuordnung`.
 * @property standardDauerProStartSek Standarddauer pro Starter in Sekunden für diese Art von Dressurprüfung.
 * Kann von der Abteilung überschrieben werden.
 * @property pauseNachAnzahlReiter Optional: Nach wie vielen Reitern eine kurze Pause eingeplant werden soll.
 * @property dauerPauseMin Optional: Dauer der Pause in Minuten.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class DressurPruefungSpezifika(
    @Serializable(with = UuidSerializer::class)
    val pruefungDbId: Uuid, // PK (identisch mit Pruefung_OEPS.pruefungDbId) und FK

    @Serializable(with = UuidSerializer::class)
    var aufgabeStammdatumId: Uuid, // FK zu Sportfachliche_Stammdaten (Typ DRESSURAUFGABE)

    @Serializable(with = UuidSerializer::class)
    var klasseStammdatumId: Uuid?, // FK zu Sportfachliche_Stammdaten (Typ BEWERBSKLASSE, Sparte Dressur)

    @Serializable(with = UuidSerializer::class)
    var richtverfahrenStammdatumId: Uuid, // FK zu Sportfachliche_Stammdaten (Typ WERTUNGSVERFAHREN_DRESSUR)

    var viereckGroesse: PruefungsViereckE,
    var erlaubeAuswendigReiten: Boolean = true,
    var erlaubeVorlesen: Boolean = false,
    var anzahlRichterGeplant: Int = 1,
    var maxPunkteProRichterMöglich: Double? = null,

    // Definiert, welche Richterpositionen für diese Prüfung besetzt werden sollen
    var geplanteRichterPositionen: List<RichterPositionE> = listOf(RichterPositionE.C),

    // Zeitplanung spezifisch (Default für Abteilungen dieses Dressurbewerbs)
    var standardDauerProStartSek: Int = 240, // z.B. 4 Minuten
    var pauseNachAnzahlReiter: Int? = null,
    var dauerPauseMin: Int? = null,


    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
