import at.mocode.enums.CupSerieTypE
import at.mocode.enums.SparteE
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Definiert einen übergreifenden Wettbewerb wie eine Meisterschaft, einen Cup oder eine Turnierserie,
 * der sich über mehrere Turniere oder spezifische Prüfungen/Abteilungen erstrecken kann und
 * eigene Reglements sowie eine Gesamtwertung haben kann.
 *
 * @property mcsId Eindeutiger interner Identifikator für diese Meisterschaft/Cup/Serie (UUID).
 * @property name Der offizielle Name der Meisterschaft, des Cups oder der Serie
 * (z.B. "EQUIVERON Cup 2025", "NÖ Landesmeisterschaft Dressur allgemeine Klasse").
 * @property typ Die Art des übergreifenden Wettbewerbs (siehe `CupSerieTypE`).
 * @property jahr Das Jahr, in dem diese Meisterschaft/Cup/Serie stattfindet oder gewertet wird.
 * @property sparte Die Pferdesportsparte, für die dieser Wettbewerb primär ausgeschrieben ist.
 * @property beschreibung Optionale ausführlichere Beschreibung des Wettbewerbs.
 * @property reglementText Das spezifische Reglement als Text oder Markdown.
 * @property reglementPdfUrl Optionaler Link zum offiziellen PDF-Reglement.
 * @property hauptsponsor Optionaler Name des Hauptsponsors.
 * @property gesamtdotationText Optionale Beschreibung der Gesamtdotation (z.B. "ca. EUR 8.000.- in Sachpreisen").
 * @property teilnahmebedingungenText Spezifische Teilnahmebedingungen für diesen Wettbewerb.
 * @property austragungsbedingungenText Spezifische Austragungsbedingungen (z.B. welche Prüfungen zählen).
 * @property wertungsModusBeschreibung Beschreibung, wie die Gesamtwertung ermittelt wird.
 * @property oetoRegelReferenzId Optionale Verknüpfung zu einer spezifischen Regel in der
 * `OETORegelReferenz`-Tabelle, die diesen Wettbewerb oder seine Grundlagen definiert.
 * @property istAktiv Gibt an, ob dieser Wettbewerb aktuell aktiv ist.
 * @property ansprechpartnerDomPersonId Optionale Verknüpfung zur `DomPerson`, die für diesen Cup verantwortlich ist.
 * @property logoUrl Optionaler URL zu einem Logo für den Cup/die Meisterschaft.
 * @property webseiteUrl Optionale Webseite mit weiteren Informationen.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class Meisterschaft_Cup_Serie(
    @Serializable(with = UuidSerializer::class)
    val mcsId: Uuid = uuid4(),

    var name: String,
    var typ: CupSerieTypE,
    var jahr: Int,
    var sparte: SparteE,

    var beschreibung: String? = null,
    var reglementText: String? = null,
    var reglementPdfUrl: String? = null,
    var hauptsponsor: String? = null,
    var gesamtdotationText: String? = null,

    var teilnahmebedingungenText: String? = null,
    var austragungsbedingungenText: String? = null,
    var wertungsModusBeschreibung: String? = null,

    @Serializable(with = UuidSerializer::class)
    var oetoRegelReferenzId: Uuid? = null, // Dein oeto_regel_ref_id_mcs

    var istAktiv: Boolean = true,

    @Serializable(with = UuidSerializer::class)
    var ansprechpartnerDomPersonId: Uuid? = null,
    var logoUrl: String? = null,
    var webseiteUrl: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
