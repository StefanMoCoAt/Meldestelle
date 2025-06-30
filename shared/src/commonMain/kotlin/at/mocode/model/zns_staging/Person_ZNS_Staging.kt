package at.mocode.model.zns_staging

import at.mocode.serializers.KotlinInstantSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert einen Rohdatensatz für eine Person (Reiter, Funktionär etc.),
 * wie er aus den OEPS ZNS-Dateien (`LIZENZ01.dat`, `RICHT01.dat`) importiert wird.
 *
 * Diese Klasse dient als temporärer Container (Staging-Tabelle) für die unverarbeiteten
 * Personendaten, bevor sie validiert, transformiert und in die Domänen-Entitäten
 * `DomPerson` und `DomLizenz` überführt werden. Alle Felder aus der Datei werden
 * als String oder der direkteste Basistyp gespeichert, um Parsing-Fehler auf dieser
 * Ebene zu minimieren. Die Konvertierung und Interpretation erfolgt im nächsten Schritt.
 *
 * @property oepsSatzNrPerson Die eindeutige OEPS-Satznummer der Person (aus `LIZENZ01.dat` / `RICHT01.dat`, Stelle 1-6). Dient als Primärschlüssel.
 * @property familiennameRoh Familienname der Person (Rohwert).
 * @property vornameRoh Vorname der Person (Rohwert).
 * @property geburtsdatumTextRoh Geburtsdatum als Text im Format JJJJMMTT (Rohwert).
 * @property geschlechtCodeRoh Geschlechtscode (W, M, " ") (Rohwert).
 * @property nationalitaetCodeRoh Nationalitätskürzel (3-stellig) (Rohwert).
 * @property bundeslandCodeOepsRoh OEPS-Code für das Bundesland (Rohwert).
 * @property vereinsnameOepsRoh Name des Stammvereins (Rohwert).
 * @property mitgliedNrVereinRoh Mitgliedsnummer im Verein (Rohwert).
 * @property feiIdPersonRoh FEI-ID der Person (Rohwert).
 * @property sperrlisteFlagOepsRoh Kennzeichen für Sperrliste ("S" oder BLANK) (Rohwert).
 * @property kaderFlagOepsRoh Kaderkennzeichen (Rohwert).
 * @property telefonRoh Telefonnummer (Rohwert).
 * @property reiterlizenzRoh Kürzel der Haupt-Reiterlizenz (Rohwert).
 * @property startkarteRoh Kürzel der Startkarte (Rohwert).
 * @property fahrlizenzRoh Kürzel der Fahrlizenz (Rohwert).
 * @property altersklasseJugendCodeOepsRoh Code für Altersklasse Jugend/Junior/U25 (Rohwert).
 * @property altersklasseJungerreiterCodeOepsRoh Code für Altersklasse Junge Reiter ("Y") (Rohwert).
 * @property jahrLetzteZahlungLizenzOepsRoh Jahr der letzten Lizenzzahlung als Text (Rohwert).
 * @property lizenzinfoRawOepsRoh Kommagetrennte Liste der bezahlten Lizenzen/Startkarten (Rohwert).
 * @property qualifikationenRawOepsRoh Kommagetrennte Liste der Funktionärsqualifikationen aus `RICHT01.dat` (Rohwert).
 * @property importTimestamp Zeitstempel, wann dieser Datensatz in die Staging-Tabelle importiert wurde.
 */
@Serializable
data class Person_ZNS_Staging(
    val oepsSatzNrPerson: String, // PK aus LIZENZ01.dat / RICHT01.dat (VARCHAR(6))
    var familiennameRoh: String?,     // VARCHAR(50)
    var vornameRoh: String?,           // VARCHAR(25)
    var geburtsdatumTextRoh: String?,  // VARCHAR(8) (JJJJMMTT)
    var geschlechtCodeRoh: String?,    // CHAR(1)
    var nationalitaetCodeRoh: String?, // VARCHAR(3)
    var bundeslandCodeOepsRoh: String?,// VARCHAR(2)
    var vereinsnameOepsRoh: String?,    // VARCHAR(50)
    var mitgliedNrVereinRoh: String?,   // VARCHAR(8)
    var feiIdPersonRoh: String?,        // VARCHAR(10)
    var sperrlisteFlagOepsRoh: String?,// CHAR(1)
    var kaderFlagOepsRoh: String?,      // CHAR(1)
    var telefonRoh: String?,           // VARCHAR(21)
    var reiterlizenzRoh: String?,      // VARCHAR(4)
    var startkarteRoh: String?,        // CHAR(1)
    var fahrlizenzRoh: String?,        // VARCHAR(2)
    var altersklasseJugendCodeOepsRoh: String?, // VARCHAR(2)
    var altersklasseJungerreiterCodeOepsRoh: String?, // CHAR(1)
    var jahrLetzteZahlungLizenzOepsRoh: String?, // VARCHAR(4) -> wird zu Int?
    var lizenzinfoRawOepsRoh: String?,          // VARCHAR(10)
    var qualifikationenRawOepsRoh: String?,    // VARCHAR(30) aus RICHT01.dat
    @Serializable(with = KotlinInstantSerializer::class)
    var importTimestamp: Instant = Clock.System.now()
)

