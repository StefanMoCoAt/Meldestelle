package at.mocode.model

import kotlinx.serialization.Serializable

// Jedes Turnier hat einen oder mehrere Bewerbe

@Serializable
data class Turnier(
    val name: String, // "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ",
    val datum: String, // "7.JUNI 2025" vielleicht DateTime und nur als String in diesem Format für das Frontend
    val number: Int, // 25319
    var bewerbe: List<Bewerb> // Liste an Bewerben mit nummer, titel, klasse und task (optional)
)

@Serializable
data class Bewerb(
    val nummer: Int, // 1
    val titel: String, // "Stilspringprüfung" oder "Dressurprüfung"
    val klasse: String ,// "60 cm" oder "Kl. A"
    val task: String? // "DRA 1"
)
