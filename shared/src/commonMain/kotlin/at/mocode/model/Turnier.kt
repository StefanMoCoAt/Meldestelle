package at.mocode.model

import kotlinx.serialization.Serializable
@Serializable
data class Turnier(
    val id: String, // Eine eindeutige ID für das Turnier (z.B. eine UUID als String)
    val name: String, // Der Name, z.B. "CDN-C Edelhof April 2025"
    val datum: String, // Das Datum oder der Zeitraum, erstmal als Text, z.B. "14.04.2025 - 15.04.2025"
    val logoUrl: String? = null, // Optional: Link zum Logo des Veranstalters
    val ausschreibungUrl: String? = null // Optional: Link zum Ausschreibung-PDF
    // Hier können später viele weitere Felder hinzukommen:
    // Ort, Veranstalter, Status (geplant, läuft, beendet), Disziplinen etc.
)
