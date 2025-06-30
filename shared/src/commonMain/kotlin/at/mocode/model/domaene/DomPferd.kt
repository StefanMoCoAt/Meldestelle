import at.mocode.enums.DatenQuelleE
import at.mocode.enums.PferdeGeschlechtE // NEUES ENUM
import at.mocode.serializers.KotlinInstantSerializer
import at.mocode.serializers.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert ein Pferd im Domänenmodell der Anwendung.
 *
 * Die Daten für ein Pferd können aus dem OEPS ZNS-Import (`Pferd_ZNS_Staging`)
 * stammen oder manuell im System angelegt werden (z.B. für ausländische Pferde oder
 * Pferde ohne OEPS-Registrierung, die z.B. in lizenzfreien Bewerben starten).
 *
 * @property pferdId Eindeutiger interner Identifikator für dieses Pferd (UUID).
 * @property oepsSatzNrPferd Die offizielle 10-stellige OEPS-Satznummer des Pferdes, falls vorhanden. Eindeutig.
 * @property oepsKopfNr Die offizielle 4-stellige OEPS-Kopfnummer des Pferdes, falls vorhanden.
 * @property name Der Name des Pferdes.
 * @property lebensnummer Die Lebensnummer des Pferdes (UELN), falls bekannt.
 * @property feiPassNr Die FEI-Passnummer des Pferdes, falls vorhanden.
 * @property geburtsjahr Geburtsjahr des Pferdes.
 * @property geschlecht Geschlecht des Pferdes.
 * @property farbe Farbe des Pferdes.
 * @property rasse Rasse des Pferdes.
 * @property abstammungVaterName Name des Vaters.
 * @property abstammungMutterName Name der Mutter.
 * @property abstammungMutterVaterName Name des Muttervaters.
 * @property abstammungZusatzInfo Allgemeine Abstammungsinformationen aus dem ZNS (Feld "ABSTAMMUNG").
 * @property besitzerPersonId Optionale Verknüpfung zur `DomPerson` (Besitzer).
 * @property verantwortlichePersonId Optionale Verknüpfung zur `DomPerson` (Verantwortliche Person lt. ZNS).
 * @property heimatVereinId Optionale Verknüpfung zum `DomVerein` (Heimatverein des Pferdes lt. ZNS).
 * @property letzteZahlungPferdegebuehrJahrOeps Jahr der letzten Zahlung der OEPS-Pferdegebühr.
 * @property stockmassCm Stockmaß des Pferdes in cm.
 * @property datenQuelle Gibt die Herkunft dieses Datensatzes an.
 * @property istAktiv Gibt an, ob dieser Pferdedatensatz aktuell aktiv ist.
 * @property notizenIntern Interne Anmerkungen oder Notizen zu diesem Pferd.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class DomPferd(
    @Serializable(with = UuidSerializer::class)
    val pferdId: Uuid = uuid4(),

    var oepsSatzNrPferd: String?, // Aus Pferd_ZNS_Staging.oepsSatzNrPferd, UNIQUE
    var oepsKopfNr: String?,      // Aus Pferd_ZNS_Staging.oepsKopfNrRoh
    var name: String,             // Aus Pferd_ZNS_Staging.nameRoh

    var lebensnummer: String? = null,   // Aus Pferd_ZNS_Staging.lebensnummerRoh
    var feiPassNr: String? = null,      // Aus Pferd_ZNS_Staging.feiPassNrRoh

    var geburtsjahr: Int? = null,       // Konvertiert aus Pferd_ZNS_Staging.geburtsjahrRoh
    var geschlecht: PferdeGeschlechtE? = null, // Konvertiert aus Pferd_ZNS_Staging.geschlechtCodeRoh
    var farbe: String? = null,          // Aus Pferd_ZNS_Staging.farbeRoh
    var rasse: String? = null,          // Nicht direkt in PFERDE01.dat, aber oft bekannt/wichtig

    var abstammungVaterName: String? = null, // Aus Pferd_ZNS_Staging.abstammungVaterNameRoh
    var abstammungMutterName: String? = null, // Manuell oder andere Quelle
    var abstammungMutterVaterName: String? = null, // Manuell oder andere Quelle
    var abstammungZusatzInfo: String? = null, // Aus Pferd_ZNS_Staging.abstammungInfoRoh

    @Serializable(with = UuidSerializer::class)
    var besitzerPersonId: Uuid? = null, // Muss aufgelöst werden (Name in Pferd_ZNS_Staging.verantwortlichePersonNameRoh, oder separate Besitzerinfo?)
    @Serializable(with = UuidSerializer::class)
    var verantwortlichePersonId: Uuid? = null, // Muss aufgelöst werden aus Pferd_ZNS_Staging.verantwortlichePersonNameRoh

    @Serializable(with = UuidSerializer::class)
    var heimatVereinId: Uuid? = null,    // Muss aufgelöst werden aus Pferd_ZNS_Staging.oepsVereinNrPferdRoh via DomVerein

    var letzteZahlungPferdegebuehrJahrOeps: Int? = null, // Konvertiert aus Pferd_ZNS_Staging.letzteZahlungPferdegebuehrJahrRoh
    var stockmassCm: Int? = null,       // Nicht in PFERDE01.dat

    var datenQuelle: DatenQuelleE = DatenQuelleE.MANUELL,
    var istAktiv: Boolean = true,
    var notizenIntern: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
