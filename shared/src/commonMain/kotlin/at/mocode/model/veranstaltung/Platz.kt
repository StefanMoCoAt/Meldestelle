import at.mocode.enums.PlatzTypE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert einen physischen Platz auf einer Reitanlage, der für verschiedene Zwecke
 * im Rahmen einer Pferdesportveranstaltung genutzt werden kann.
 *
 * @property platzId Eindeutiger interner Identifikator für diesen Platz (UUID).
 * @property name Der Name oder die Bezeichnung des Platzes.
 * @property typ Die Art des Platzes (z.B. AUSTRAGUNG, VORBEREITUNG).
 * @property laengeMeter Optionale Angabe der Länge des Platzes in Metern.
 * @property breiteMeter Optionale Angabe der Breite des Platzes in Metern.
 * @property bodenbelag Beschreibung des Bodenbelags.
 * @property ueberdacht Gibt an, ob der Platz überdacht ist.
 * @property beleuchtungVorhanden Gibt an, ob eine Beleuchtung für den Platz vorhanden ist.
 * @property istAktiv Gibt an, ob dieser Platz aktuell verfügbar und nutzbar ist.
 * @property notizen Interne Notizen oder zusätzliche Informationen zum Platz.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class Platz(
    @Serializable(with = UuidSerializer::class)
    val platzId: Uuid = uuid4(),

    var name: String,
    var typ: PlatzTypE,

    var laengeMeter: Double? = null,
    var breiteMeter: Double? = null,
    var bodenbelag: String? = null,
    var ueberdacht: Boolean? = null,
    var beleuchtungVorhanden: Boolean? = null,

    var istAktiv: Boolean = true,
    var notizen: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
