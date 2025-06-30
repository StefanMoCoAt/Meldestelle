package at.mocode.model.oeto_verwaltung

import at.mocode.enums.SparteE
import at.mocode.enums.VerbandE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Definiert einen spezifischen Typ einer Funktionärsqualifikation gemäß den
 * Richtlinien der ÖTO oder anderer relevanter Verbände.
 *
 * Beispiele für Qualifikationstypen sind "Richter Dressur bis Grand Prix" (R-DGP),
 * "Parcoursbauer Springen bis Klasse S*" (PB-S*), "Technischer delegierter National" (TD-NAT),
 * oder auch spezifische Ausbilder-Qualifikationen.
 * Diese Entität dient als zentrale Referenz für die im System bekannten und
 * verwaltbaren Funktionärsqualifikationen.
 *
 * @property qualTypId Eindeutiger interner Identifikator für diesen Qualifikationstyp (UUID).
 * @property qualTypCode Eindeutiger Code oder Kürzel für den Qualifikationstyp,
 * wie er vom OEPS oder anderen Verbänden verwendet wird (z.B. "R-DPF-S", "PB-S*", "STEWARD-FEI-L2").
 * Dieser Code dient als Primärschlüssel für die fachliche Identifikation.
 * @property bezeichnung Die offizielle oder allgemein verständliche Bezeichnung des Qualifikationstyps
 * (z.B. "Richter Dressurpferdeprüfung bis Klasse S", "Parcoursbauer Springen Kategorie S*", "FEI Steward Level 2").
 * @property sparte Die primäre Pferdesportsparte, für die diese Qualifikation relevant ist.
 * Kann auch spartenübergreifend sein (z.B. für Stewards).
 * @property zustaendigerVerband Optional der Verband, der diese Qualifikation ausstellt oder definiert (z.B. "OEPS", "FEI").
 * @property oetoRegelReferenzId Optionale Verknüpfung zu einer spezifischen Regel in der `OETORegelReferenz`-Tabelle,
 * die diese Qualifikation definiert oder beschreibt.
 * @property istAktiv Gibt an, ob dieser Qualifikationstyp aktuell im System verwendet werden kann.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class QualifikationsTyp(
    @Serializable(with = UuidSerializer::class)
    val qualTypId: Uuid = uuid4(),

    var qualTypCode: String,
    var bezeichnung: String,
    var sparte: SparteE,
    var zustaendigerVerband: VerbandE = VerbandE.OEPS, // Default OEPS
    var beschreibungDetails: String? = null,

    @Serializable(with = UuidSerializer::class)
    var oetoRegelReferenzId: Uuid? = null, // FK zu OETORegelReferenz.oetoRegelReferenzId

    var istAktiv: Boolean = true,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
