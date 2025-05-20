package at.mocode.shared.model.stammdaten

import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Definiert ein Land/eine Nation mit seinen offiziellen Codes und Bezeichnungen.
 *
 * Diese Entität dient als zentrale Referenz für Länder, die im System für
 * Nationalitäten von Personen, Vereinen oder für internationale Turniere relevant sind.
 *
 * @property landId Eindeutiger interner Identifikator für dieses Land (UUID).
 * @property isoAlpha2Code Der 2-stellige ISO 3166-1 Alpha-2 Code des Landes (z.B. "AT", "DE"). Sollte eindeutig sein.
 * @property isoAlpha3Code Der 3-stellige ISO 3166-1 Alpha-3 Code des Landes (z.B. "AUT", "DEU"). Sollte eindeutig sein.
 * @property isoNumerischerCode Optionaler 3-stelliger numerischer ISO 3166-1 Code des Landes (z.B. "040" für Österreich).
 * @property nameDeutsch Der offizielle deutsche Name des Landes.
 * @property nameEnglisch Der offizielle englische Name des Landes.
 * @property wappenUrl Optionaler URL-Pfad zu einer Bilddatei des Länderwappens oder der Flagge.
 * @property istEuMitglied Gibt an, ob das Land Mitglied der Europäischen Union ist.
 * @property istEwrMitglied Gibt an, ob das Land Mitglied des Europäischen Wirtschaftsraums ist.
 * @property istAktiv Gibt an, ob dieses Land aktuell im System ausgewählt/verwendet werden kann.
 * @property sortierReihenfolge Optionale Zahl zur Steuerung der Sortierreihenfolge in Auswahllisten.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class LandDefinition(
    @Serializable(with = UuidSerializer::class)
    val landId: Uuid = uuid4(),

    var isoAlpha2Code: String, // z.B. "AT" -> Fachlicher PK oder Unique Constraint
    var isoAlpha3Code: String, // z.B. "AUT" -> Unique Constraint
    var isoNumerischerCode: String? = null, // z.B. "040"
    var nameDeutsch: String,    // z.B. "Österreich"
    var nameEnglisch: String? = null, // z.B. "Austria"
    var wappenUrl: String? = null,
    var istEuMitglied: Boolean? = null,
    var istEwrMitglied: Boolean? = null, // Europäischer Wirtschaftsraum
    var istAktiv: Boolean = true,
    var sortierReihenfolge: Int? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
