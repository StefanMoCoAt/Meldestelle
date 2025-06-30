package at.mocode.model.oeto_verwaltung

import at.mocode.enums.LizenzKategorieE
import at.mocode.enums.SparteE
import at.mocode.enums.VerbandE // Wiederverwendung von VerbandE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Definiert einen globalen Typ einer Lizenz oder Startkarte gemäß der Systematik
 * des OEPS oder anderer relevanter Verbände (z.B. FEI).
 *
 * Diese Entität dient als zentrale Referenz für alle im System bekannten Lizenzarten,
 * ihre grundlegenden Eigenschaften und wie sie ggf. aus kombinierten Kürzeln
 * aufgeschlüsselt werden.
 *
 * @property lizenzTypGlobalId Eindeutiger interner Identifikator für diesen globalen Lizenztyp (UUID).
 * @property lizenzTypGlobalCode Das offizielle und eindeutige Kürzel des Lizenztyps
 * (z.B. "R1", "RD2", "S" für Startkarte allgemein, "F1", "R1S2" für eine Kombi-Lizenz).
 * Dient als fachlicher Primärschlüssel. Die Länge sollte auch längere Kombi-Kürzel erlauben.
 * @property bezeichnung Die offizielle oder allgemein verständliche Bezeichnung des Lizenztyps
 * (z.B. "Reiterlizenz R1", "Reiterlizenz Dressur D2", "Startkarte Allgemein", "Fahrerlizenz F1").
 * @property spartePrimaer Die primäre Pferdesportsparte, für die dieser Lizenztyp hauptsächlich relevant ist.
 * Kann `null` sein, wenn spartenübergreifend oder nicht eindeutig zuzuordnen (z.B. bei manchen Startkarten).
 * @property kategorieLizenzText Eine übergeordnete Kategorie zur Gruppierung von Lizenzen
 * (z.B. "Reiterlizenz", "Fahrerlizenz", "Startkarte", "Sonderlizenz").
 * @property stufe Die Stufe oder das Level der Lizenz, falls anwendbar (z.B. "1", "2", "S", "M", "GP").
 * @property beschreibungBerechtigung Eine kurze Beschreibung der Berechtigungen oder des Geltungsbereichs
 * dieser Lizenz (z.B. "Startberechtigung Dressur & Springen Klasse A", "Nationale Turniere").
 * @property aufschluesselungKombilizenzCodes Eine optionale Liste von Basis-`lizenzTypGlobalCode`s,
 * falls dieser Lizenztyp eine Kombination darstellt (z.B. für "R1S2" könnte hier ["R1", "RS2"] stehen,
 * wobei "R1" und "RS2" dann eigene `LizenzTypGlobal`-Einträge wären). Dies hilft bei der
 * detaillierten Berechtigungsprüfung.
 * @property zustaendigerVerband Der Verband, der diesen Lizenztyp primär definiert oder ausstellt.
 * @property oetoRegelReferenzId Optionale Verknüpfung zu einer spezifischen Regel in der `OETORegelReferenz`-Tabelle,
 * die diesen Lizenztyp definiert.
 * @property istAktiv Gibt an, ob dieser Lizenztyp aktuell im System verwendet und bei der Nennung
 * oder Funktionärszuordnung ausgewählt werden kann.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class LizenzTypGlobal(
    @Serializable(with = UuidSerializer::class)
    val lizenzTypGlobalId: Uuid = uuid4(), // Interner Primärschlüssel

    var lizenzTypGlobalCode: String, // Fachlicher PK, z.B. "R1", "RD2", "R1S2"
    var bezeichnung: String,
    var spartePrimaer: SparteE? = null,
    var kategorieLizenzText: LizenzKategorieE, // z.B. "Reiterlizenz", "Startkarte"
    var stufe: String? = null,
    var beschreibungBerechtigung: String? = null,
    var aufschluesselungKombilizenzCodes: List<String>? = null, // Liste von lizenzTypGlobalCode(s)
    var zustaendigerVerband: VerbandE = VerbandE.OEPS,

    @Serializable(with = UuidSerializer::class)
    var oetoRegelReferenzId: Uuid? = null, // FK zu OETORegelReferenz.oetoRegelReferenzId

    var istAktiv: Boolean = true,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
