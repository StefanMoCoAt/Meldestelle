import at.mocode.shared.enums.DatenQuelleE
import at.mocode.shared.enums.GeschlechtE
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
 * Repräsentiert eine Person (Reiter, Funktionär, Kontaktperson etc.)
 * im Domänenmodell der Anwendung.
 *
 * Die Daten können aus dem OEPS ZNS-Import (`Person_ZNS_Staging`) stammen
 * oder manuell im System angelegt werden.
 *
 * @property personId Eindeutiger interner Identifikator für diese Person (UUID).
 * @property oepsSatzNr Die offizielle 6-stellige OEPS-Satznummer der Person, falls vorhanden. Eindeutig.
 * @property nachname Familienname der Person.
 * @property vorname Vorname der Person.
 * @property titel Akademischer Titel oder Anrede (z.B. Dr., Ing.).
 * @property geburtsdatum Geburtsdatum der Person.
 * @property geschlecht Geschlecht der Person.
 * @property nationalitaetLandId Fremdschlüssel zur `LandDefinition` für die Nationalität.
 * @property feiId Optionale FEI-Identifikationsnummer der Person.
 * @property telefon Private oder geschäftliche Telefonnummer.
 * @property email Private oder geschäftliche E-Mail-Adresse.
 * @property strasse Straße und Hausnummer der Hauptadresse.
 * @property plz Postleitzahl der Hauptadresse.
 * @property ort Ortschaft der Hauptadresse.
 * @property adresszusatzZusatzinfo Weitere Adressinformationen.
 * @property stammVereinId Optionale Verknüpfung zum `DomVerein` (Stammverein der Person).
 * @property mitgliedsNummerBeiStammVerein Mitgliedsnummer der Person beim Stammverein.
 * @property istGesperrt Gibt an, ob die Person laut OEPS oder intern gesperrt ist.
 * @property sperrGrund Begründung für eine eventuelle Sperre.
 * @property altersklasseOepsCodeRaw Der Roh-Code für die Altersklasse aus dem ZNS-Import (z.B. "JG", "JR", "25").
 * Dient zur Ableitung oder als Information.
 * @property istJungerReiterOepsFlag Ob die Person im ZNS als "Junger Reiter" ("Y") gekennzeichnet ist.
 * @property kaderStatusOepsRaw Kaderkennzeichen aus dem ZNS-Import.
 * @property datenQuelle Gibt die Herkunft dieses Datensatzes an (z.B. OEPS_ZNS, MANUELL).
 * @property istAktiv Gibt an, ob dieser Personendatensatz aktuell aktiv ist.
 * @property notizenIntern Interne Anmerkungen oder Notizen zu dieser Person.
 * @property createdAt Zeitstempel der Erstellung dieses Datensatzes.
 * @property updatedAt Zeitstempel der letzten Aktualisierung dieses Datensatzes.
 */
@Serializable
data class DomPerson(
    @Serializable(with = UuidSerializer::class)
    val personId: Uuid = uuid4(),

    var oepsSatzNr: String?, // Wird aus Person_ZNS_Staging.oepsSatzNrPerson befüllt, UNIQUE
    var nachname: String,    // Wird aus Person_ZNS_Staging.familiennameRoh befüllt
    var vorname: String,     // Wird aus Person_ZNS_Staging.vornameRoh befüllt
    var titel: String? = null, // Manuelle Eingabe oder ggf. später aus ZNS falls vorhanden

    @Serializable(with = KotlinLocalDateSerializer::class)
    var geburtsdatum: LocalDate? = null, // Konvertiert aus Person_ZNS_Staging.geburtsdatumTextRoh

    var geschlecht: GeschlechtE? = null,   // Konvertiert aus Person_ZNS_Staging.geschlechtCodeRoh

    @Serializable(with = UuidSerializer::class)
    var nationalitaetLandId: Uuid? = null, // Aufgelöst aus Person_ZNS_Staging.nationalitaetCodeRoh via LandDefinition

    var feiId: String? = null,           // Wird aus Person_ZNS_Staging.feiIdPersonRoh befüllt

    var telefon: String? = null,         // Wird aus Person_ZNS_Staging.telefonRoh befüllt
    var email: String? = null,           // Manuelle Eingabe, nicht in LIZENZ01.dat

    // Adresse (manuelle Eingabe, nicht primär in LIZENZ01.dat für Person direkt)
    var strasse: String? = null,
    var plz: String? = null,
    var ort: String? = null,
    var adresszusatzZusatzinfo: String? = null,

    @Serializable(with = UuidSerializer::class)
    var stammVereinId: Uuid? = null,     // Aufgelöst aus Person_ZNS_Staging.vereinsnameOepsRoh & bundeslandCodeOepsRoh via DomVerein
    var mitgliedsNummerBeiStammVerein: String? = null, // Wird aus Person_ZNS_Staging.mitgliedNrVereinRoh befüllt

    var istGesperrt: Boolean = false,   // Konvertiert aus Person_ZNS_Staging.sperrlisteFlagOepsRoh ("S" -> true)
    var sperrGrund: String? = null,     // Manuelle Eingabe

    var altersklasseOepsCodeRaw: String? = null, // Speichert Roh-Code "JG", "JR", "25"
    var istJungerReiterOepsFlag: Boolean = false, // true wenn Roh-Code "Y"

    var kaderStatusOepsRaw: String? = null, // Speichert Roh-Code (aktuell meist BLANK)

    var datenQuelle: DatenQuelleE = DatenQuelleE.MANUELL,
    var istAktiv: Boolean = true,
    var notizenIntern: String? = null,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
)
