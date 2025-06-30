package at.mocode.model.oeto_verwaltung

import at.mocode.enums.*
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Zentrale Entität zur Speicherung verschiedener, wiederverwendbarer sportfachlicher Definitionen
 * und Stammdaten, die nicht in spezifischere Entitäten wie `LizenzTypGlobal` oder
 * `QualifikationsTyp` passen. Die Art des Stammdatums wird durch das Feld `typ` definiert.
 *
 * Beispiele für Typen:
 * - DRESSURAUFGABE: Definition einer spezifischen Dressuraufgabe (z.B. "A1", "LF3").
 * - WERTUNGSVERFAHREN_SPRINGEN: Definition eines Richtverfahrens im Springen (z.B. "A2 nach ÖTO §204").
 * - WERTUNGSVERFAHREN_DRESSUR: Definition eines Richtverfahrens in der Dressur (z.B. "GM", "GT").
 * - BEWERBSKLASSE: Definition einer Leistungsklasse (z.B. "Klasse A", "115cm Höhe").
 * - BEWERBSKATEGORIE_OETO: Definition einer offiziellen ÖTO-Turnier-/Bewerbskategorie (z.B. "CDN-C Neu").
 * - HINDERNISTYP: Definition von Standard-Hindernistypen im Springen.
 * - RVK_PUNKTETABELLE: Punktetabellen für den Reitervierkampf.
 * - OETO_REGEL_TEXT: Freitextauszüge oder Erläuterungen zu spezifischen ÖTO-Regeln.
 *
 * @property stammdatumId Eindeutiger interner Identifikator für diesen Stammdatensatz (UUID).
 * @property typ Der Typ des sportfachlichen Stammdatums (siehe `SportfachStammdatenTypE`).
 * Bestimmt, wie die anderen Felder, insbesondere `detailsJson`, zu interpretieren sind.
 * @property code Ein eindeutiger Code oder Kürzel für diesen Stammdatensatz,
 * typischerweise eindeutig innerhalb des jeweiligen `typs` (z.B. "A1" für Typ DRESSURAUFGABE,
 * "CDN-CNEU" für Typ BEWERBSKATEGORIE_OETO).
 * @property bezeichnung Die offizielle oder allgemein verständliche Bezeichnung dieses Stammdatums.
 * @property detailsJson Optionale, strukturierte Zusatzinformationen im JSON-Format,
 * deren Inhalt vom `typ` abhängt.
 * Beispiele:
 * - Für DRESSURAUFGABE: Lektionen, Dauer, Max-Punkte.
 * - Für WERTUNGSVERFAHREN: Spezifische Fehlerwerte, Zeitregeln.
 * - Für BEWERBSKLASSE: Höhenangaben im Springen, Anforderungen in Dressur.
 * @property sparteZugehoerigkeit Optionale primäre Pferdesportsparte, für die dieser Stammdatensatz relevant ist.
 * Kann `null` sein, wenn spartenübergreifend oder nicht direkt zuzuordnen.
 * @property verbandGueltigkeit Für Aufgaben oder Regelwerke relevant, ob sie national (OEPS), FEI oder von anderer Nation sind.
 * @property viereckGroesse Standard-Viereckgröße, relevant für Typ DRESSURAUFGABE.
 * @property richtverfahrenModus Standard-Richtverfahrensmodus (GM/GT), relevant für Typ DRESSURAUFGABE.
 * @property oetoRegelReferenzId Optionale Verknüpfung zu einer spezifischen Regel in der `OETORegelReferenz`-Tabelle.
 * @property istAktiv Gibt an, ob dieser Stammdatensatz aktuell im System verwendet werden kann.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class Sportfachliche_Stammdaten(
    @Serializable(with = UuidSerializer::class)
    val stammdatumId: Uuid = uuid4(),

    var typ: SportfachStammdatenTypE,
    var code: String, // Eindeutig pro Typ, z.B. "A1", "CDN-CNEU", "115CM"
    var bezeichnung: String,
    var detailsJson: String? = null, // Strukturierte Details als JSON-String
    var sparteZugehoerigkeit: SparteE? = null,
    var verbandGueltigkeit: VerbandE? = null, // für Aufgaben/Regeln
    var viereckGroesse: PruefungsViereckE? = null, // für Dressuraufgaben
    var richtverfahrenModus: RichtverfahrenModusE? = null, // für Dressuraufgaben

    @Serializable(with = UuidSerializer::class)
    var oetoRegelReferenzId: Uuid? = null,

    var istAktiv: Boolean = true,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
