package at.mocode.model

import kotlinx.serialization.Serializable

@Serializable
data class Nennung(
    // Wir brauchen die Turnier-ID, um die Nennung zuzuordnen
    val turnierId: String,
    // Einfache Felder für den Start
    val riderName: String = "", // Standardwerte für leeres Formular
    val horseName: String = "",
    val email: String = "",
    val comments: String? = null
    // Hier kommen später Felder hinzu: Verein, Lizenznr., Tel,
    // und vor allem: die Auswahl der Prüfungen!
)