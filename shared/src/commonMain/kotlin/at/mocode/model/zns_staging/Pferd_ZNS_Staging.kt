package at.mocode.model.zns_staging

import at.mocode.serializers.KotlinInstantSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert einen Rohdatensatz für ein Pferd, wie er aus der OEPS ZNS-Datei
 * `PFERDE01.dat` importiert wird.
 *
 * Diese Klasse dient als temporärer Container (Staging-Tabelle) für die unverarbeiteten
 * Pferdedaten, bevor sie validiert, transformiert und in die Domänen-Entität
 * `DomPferd` überführt werden. Alle Felder aus der Datei werden als String oder
 * der direkteste Basistyp gespeichert.
 *
 * @property oepsSatzNrPferd Die eindeutige 10-stellige OEPS-Satznummer des Pferdes (aus `PFERDE01.dat`). Dient als Primärschlüssel.
 * @property oepsKopfNrRoh Die 4-stellige OEPS-Kopfnummer des Pferdes (Rohwert).
 * @property nameRoh Name des Pferdes (Rohwert).
 * @property lebensnummerRoh Lebensnummer des Pferdes (Rohwert).
 * @property geburtsjahrRoh Geburtsjahr des Pferdes als Text (Rohwert).
 * @property geschlechtCodeRoh Geschlechtscode des Pferdes (Rohwert).
 * @property farbeRoh Farbe des Pferdes (Rohwert).
 * @property abstammungVaterNameRoh Name des Vaters (Rohwert).
 * @property abstammungInfoRoh Allgemeines Abstammungsfeld (Muttervater etc.) (Rohwert).
 * @property oepsVereinNrPferdRoh OEPS-Vereinsnummer des Heimatvereins des Pferdes (Rohwert).
 * @property verantwortlichePersonNameRoh Name der verantwortlichen Person für das Pferd (Rohwert).
 * @property feiPassNrRoh FEI-Passnummer des Pferdes (Rohwert).
 * @property letzteZahlungPferdegebuehrJahrRoh Jahr der letzten Zahlung der Pferdegebühr als Text (Rohwert).
 * @property importTimestamp Zeitstempel, wann dieser Datensatz in die Staging-Tabelle importiert wurde.
 */
@Serializable
data class Pferd_ZNS_Staging(
    val oepsSatzNrPferd: String, // PK aus PFERDE01.dat (VARCHAR(10))
    var oepsKopfNrRoh: String?,      // VARCHAR(4)
    var nameRoh: String?,           // VARCHAR(30)
    var lebensnummerRoh: String?,   // VARCHAR(9)
    var geburtsjahrRoh: String?,      // VARCHAR(4) -> wird zu Int?
    var geschlechtCodeRoh: String?,   // CHAR(1)
    var farbeRoh: String?,           // VARCHAR(15)
    var abstammungVaterNameRoh: String?,// VARCHAR(30)
    var abstammungInfoRoh: String?,    // VARCHAR(15) (allgemeines Abstammungsfeld)
    var oepsVereinNrPferdRoh: String?, // VARCHAR(4)
    var verantwortlichePersonNameRoh: String?, // VARCHAR(75)
    var feiPassNrRoh: String?,         // VARCHAR(10)
    var letzteZahlungPferdegebuehrJahrRoh: String?, // VARCHAR(4) -> wird zu Int?
    @Serializable(with = KotlinInstantSerializer::class)
    var importTimestamp: Instant = Clock.System.now()
)

