package at.mocode.shared.model.zns_staging

import at.mocode.shared.serializers.KotlinInstantSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Repräsentiert einen Rohdatensatz für einen Verein, wie er aus der OEPS ZNS-Datei
 * `VEREIN01.dat` importiert wird.
 *
 * Diese Klasse dient als temporärer Container (Staging-Tabelle) für die unverarbeiteten
 * Vereinsdaten, bevor sie validiert, transformiert und in die Domänen-Entität
 * `DomVerein` überführt werden.
 *
 * @property oepsVereinsNr Die offizielle OEPS-Vereinsnummer (aus `VEREIN01.dat`, Stelle 1-4). Dient als Primärschlüssel für diesen Staging-Datensatz.
 * @property nameRoh Der Name des Vereins, wie er in der `VEREIN01.dat` (Stelle 5-54) steht.
 * @property importTimestamp Zeitstempel, wann dieser Datensatz in die Staging-Tabelle importiert wurde.
 */
@Serializable
data class Verein_ZNS_Staging(
    val oepsVereinsNr: String, // PK aus VEREIN01.dat (VARCHAR(4))
    var nameRoh: String?,      // VARCHAR(50)
    @Serializable(with = KotlinInstantSerializer::class)
    var importTimestamp: Instant = Clock.System.now()
)

