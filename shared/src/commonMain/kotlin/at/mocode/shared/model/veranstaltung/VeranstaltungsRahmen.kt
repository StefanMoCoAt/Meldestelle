package at.mocode.shared.model.veranstaltung

import at.mocode.shared.enums.EventStatusE
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.KotlinLocalDateSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Definiert den übergeordneten Rahmen einer Veranstaltung (z.B. ein Turnierwochenende),
 * der ein oder mehrere spezifische OEPS-Turniere umfassen kann.
 *
 * @property veranstRahmenId Eindeutiger interner Identifikator für diesen Veranstaltungsrahmen (UUID).
 * @property name Die offizielle oder werbewirksame Bezeichnung des Veranstaltungsrahmens
 * (z.B. "Neumarkter Pferdesporttage 2025", "Pfingstturnier Sudenhof").
 * @property eventTypIntern Optionale interne Klassifizierung des Events
 * (z.B. "StandardWochenende", "Meisterschaftsevent", "Cupveranstaltung").
 * @property ortName Name der Anlage oder des Hauptortes der Veranstaltung (z.B. "Reitanlage Stroblmair").
 * @property ortStrasse Straße und Hausnummer des Veranstaltungsortes.
 * @property ortPlz Postleitzahl des Veranstaltungsortes.
 * @property ortOrt Ortschaft des Veranstaltungsortes.
 * @property datumVonGesamt Startdatum des gesamten Veranstaltungsrahmens.
 * @property datumBisGesamt Enddatum des gesamten Veranstaltungsrahmens.
 * @property logoUrl Optionaler URL-Pfad zu einem Logo für den Veranstaltungsrahmen.
 * @property webseiteUrl Optionale URL zur Webseite des Veranstaltungsrahmens oder des Veranstalters.
 * @property hauptveranstalterDomVereinId Optionale Verknüpfung zum `DomVerein`,
 * der als Hauptveranstalter dieses Rahmenevents auftritt.
 * @property hauptKontaktpersonDomPersonId Optionale Verknüpfung zur `DomPerson`,
 * die als Hauptansprechpartner für den gesamten Veranstaltungsrahmen dient.
 * @property status Aktueller Status des Veranstaltungsrahmens (z.B. in Planung, genehmigt, aktiv).
 * @property anmerkungenAllgemein Allgemeine Notizen oder Beschreibungen zum Veranstaltungsrahmen.
 * @property berichtAnmerkungSanitaer Anmerkungen zu sanitären Anlagen für den Turnierbericht.
 * @property berichtAnmerkungParkenEntladen Anmerkungen zu Park- und Entlademöglichkeiten.
 * @property berichtAnmerkungSponsorenBetreuung Anmerkungen zur Sponsorenbetreuung.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class VeranstaltungsRahmen(
    @Serializable(with = UuidSerializer::class)
    val veranstRahmenId: Uuid = uuid4(),

    var name: String,
    var eventTypIntern: String? = null,

    var ortName: String, // Kombiniert aus deinem ort_text oder spezifischer
    var ortStrasse: String? = null,
    var ortPlz: String? = null,
    var ortOrt: String? = null, // Genauer Ort

    @Serializable(with = KotlinLocalDateSerializer::class)
    var datumVonGesamt: LocalDate,
    @Serializable(with = KotlinLocalDateSerializer::class)
    var datumBisGesamt: LocalDate,

    var logoUrl: String? = null,
    var webseiteUrl: String? = null,

    @Serializable(with = UuidSerializer::class)
    var hauptveranstalterDomVereinId: Uuid? = null, // FK zu DomVerein.vereinId

    @Serializable(with = UuidSerializer::class)
    var hauptKontaktpersonDomPersonId: Uuid? = null, // FK zu DomPerson.personId

    var status: EventStatusE = EventStatusE.IN_PLANUNG,
    var anmerkungenAllgemein: String? = null,

    // Felder für übergreifende Turnierbericht-Aspekte
    var berichtAnmerkungSanitaer: String? = null,
    var berichtAnmerkungParkenEntladen: String? = null,
    var berichtAnmerkungSponsorenBetreuung: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
