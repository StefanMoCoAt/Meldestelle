package at.mocode.shared.model.domaene.verein

import at.mocode.shared.enums.DatenQuelleE
import at.mocode.shared.serializers.KotlinInstantSerializer
import at.mocode.shared.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert einen Reitverein im Domänenmodell der Anwendung.
 *
 * Die Daten für einen Verein können aus dem OEPS ZNS-Import (`Verein_ZNS_Staging`)
 * stammen oder manuell im System angelegt werden (z.B. für ausländische Vereine).
 * Jeder Verein wird durch eine systeminterne UUID und die offizielle OEPS-Vereinsnummer
 * (falls vorhanden) eindeutig identifiziert.
 *
 * @property vereinId Eindeutiger interner Identifikator für diesen Verein (UUID).
 * @property oepsVereinsNr Die offizielle 4-stellige OEPS-Vereinsnummer. Sollte eindeutig sein, falls vorhanden.
 * @property name Der offizielle Name des Vereins.
 * @property kuerzel Ein optionales Kürzel oder eine Kurzbezeichnung für den Verein.
 * @property adresseStrasse Straße und Hausnummer des Vereinssitzes.
 * @property plz Postleitzahl des Vereinssitzes.
 * @property ort Ortschaft des Vereinssitzes.
 * @property bundeslandId Optionale Verknüpfung zur `BundeslandDefinition`. Für OEPS-Vereine
 * wird versucht, dies aus der ersten Ziffer der `oepsVereinsNr` abzuleiten.
 * @property landId Verknüpfung zur `LandDefinition`. Für OEPS-Vereine ist dies "Österreich".
 * @property emailAllgemein Allgemeine E-Mail-Adresse des Vereins.
 * @property telefonAllgemein Allgemeine Telefonnummer des Vereins.
 * @property webseiteUrl URL zur Webseite des Vereins.
 * @property datenQuelle Gibt die Herkunft dieses Datensatzes an (z.B. OEPS_ZNS, MANUELL).
 * @property istAktiv Gibt an, ob dieser Verein aktuell aktiv ist und im System verwendet werden kann.
 * @property notizenIntern Interne Anmerkungen oder Notizen zu diesem Verein.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class DomVerein(
    @Serializable(with = UuidSerializer::class)
    val vereinId: Uuid = uuid4(),

    var oepsVereinsNr: String?, // Kann null sein für nicht-OEPS Vereine. Wenn gesetzt, erste Ziffer = Bundesland-Code.
    var name: String,
    var kuerzel: String? = null,

    var adresseStrasse: String? = null,
    var plz: String? = null,
    var ort: String? = null,

    @Serializable(with = UuidSerializer::class)
    var bundeslandId: Uuid? = null, // FK zu BundeslandDefinition.bundeslandId

    @Serializable(with = UuidSerializer::class)
    var landId: Uuid,          // FK zu LandDefinition.landId (jeder Verein ist in einem Land)

    var emailAllgemein: String? = null,
    var telefonAllgemein: String? = null,
    var webseiteUrl: String? = null,

    var datenQuelle: DatenQuelleE = DatenQuelleE.OEPS_ZNS, // default OEPS_ZNS
    var istAktiv: Boolean = true,
    var notizenIntern: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
