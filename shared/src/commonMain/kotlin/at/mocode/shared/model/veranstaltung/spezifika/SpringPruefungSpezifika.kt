import at.mocode.shared.enums.ArtDesStechensE
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Speichert die spezifischen Details und Anforderungen für eine Springprüfung,
 * die auf einer Basis-Prüfung (`Pruefung_OEPS`) aufbaut.
 *
 * @property pruefungDbId Eindeutiger Identifikator, der auch der Fremdschlüssel zur
 * zugehörigen `Pruefung_OEPS` ist (1:1-Beziehung).
 * @property klasseStammdatumId Optionaler Fremdschlüssel zu `Sportfachliche_Stammdaten` (Typ BEWERBSKLASSE),
 * der die Leistungsklasse/Höhe dieser Springprüfung definiert (z.B. "105cm", "Klasse A").
 * @property richtverfahrenStammdatumId Fremdschlüssel zu `Sportfachliche_Stammdaten`
 * (Typ WERTUNGSVERFAHREN_SPRINGEN), der das anzuwendende Richtverfahren definiert (z.B. "A2", "C").
 * @property parcoursskizzeUrl Optionaler URL oder Pfad zur Parcoursskizze.
 * @property gpParcoursLaengeMeter Länge des Grundparcours in Metern.
 * @property gpErlaubteZeitSekunden Erlaubte Zeit für den Grundparcours in Sekunden.
 * @property gpHoechstZeitSekunden Höchstzeit für den Grundparcours in Sekunden.
 * @property gpAnzahlHindernisse Anzahl der Hindernisse im Grundparcours.
 * @property gpAnzahlSpruenge Anzahl der Sprünge im Grundparcours.
 * @property hatIdealzeit Gibt an, ob es eine Idealzeit gibt (relevant für Stilspringprüfungen oder spezielle Verfahren).
 * @property artDesStechens Die Art des Stechens, falls eines stattfindet (siehe `ArtDesStechensE`).
 * @property stParcoursLaengeMeter Länge des Stechparcours in Metern.
 * @property stErlaubteZeitSekunden Erlaubte Zeit für das Stechen in Sekunden.
 * @property stHoechstZeitSekunden Höchstzeit für das Stechen in Sekunden.
 * @property stAnzahlHindernisse Anzahl der Hindernisse im Stechen.
 * @property stAnzahlSpruenge Anzahl der Sprünge im Stechen.
 * @property standardDauerProStartSek Standarddauer pro Starter in Sekunden für diese Art von Springprüfung.
 * @property standardUmbauzeitMin Standard-Umbauzeit nach diesem Bewerb in Minuten (Default für Abteilungen).
 * @property standardBesichtigungszeitMin Standard-Besichtigungszeit vor diesem Bewerb in Minuten (Default für Abteilungen).
 * @property standardStechzeitZusaetzlichMin Standard-Zusatzzeit für ein eventuelles Stechen in Minuten (Default für Abteilungen).
 * @property zeitMessSystemIntegrationInformation Information zur geplanten oder verwendeten Zeitmessanlage (z.B. "Microgate REI2", "Alge Timing").
 * @propertycreatedAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class SpringPruefungSpezifika(
    @Serializable(with = UuidSerializer::class)
    val pruefungDbId: Uuid, // PK (identisch mit Pruefung_OEPS.pruefungDbId) und FK

    @Serializable(with = UuidSerializer::class)
    var klasseStammdatumId: Uuid?, // FK zu Sportfachliche_Stammdaten (Typ BEWERBSKLASSE, Sparte Springen)

    @Serializable(with = UuidSerializer::class)
    var richtverfahrenStammdatumId: Uuid, // FK zu Sportfachliche_Stammdaten (Typ WERTUNGSVERFAHREN_SPRINGEN)

    var parcoursskizzeUrl: String? = null,

    // Grundparcours Details
    var gpParcoursLaengeMeter: Int? = null,
    var gpErlaubteZeitSekunden: Int? = null,
    var gpHoechstZeitSekunden: Int? = null,
    var gpAnzahlHindernisse: Int? = null,
    var gpAnzahlSpruenge: Int? = null,
    var hatIdealzeit: Boolean = false,

    // Stechen Details (optional, je nach Richtverfahren und artDesStechens)
    var artDesStechens: ArtDesStechensE? = null,
    var stParcoursLaengeMeter: Int? = null,
    var stErlaubteZeitSekunden: Int? = null,
    var stHoechstZeitSekunden: Int? = null,
    var stAnzahlHindernisse: Int? = null,
    var stAnzahlSpruenge: Int? = null,

    // Zeitplanung spezifisch (Default für Abteilungen dieses Springbewerbs)
    var standardDauerProStartSek: Int = 90, // z.B. 1.5 Minuten
    var standardUmbauzeitMin: Int = 10,
    var standardBesichtigungszeitMin: Int = 10,
    var standardStechzeitZusaetzlichMin: Int? = null, // Nur relevant, wenn Stechen stattfindet

    var zeitMessSystemIntegrationInformation: String? = null, // Freitext für Info zur Zeitmessung

    // var zugewieseneFunktionaere: List<BewerbFunktionaerZuordnung> = emptyList(), // Kommt noch

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
