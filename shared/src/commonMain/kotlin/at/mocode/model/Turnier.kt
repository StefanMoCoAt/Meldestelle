package at.mocode.model

import kotlinx.serialization.Serializable

@Serializable
data class Turnier(
    /** The name of the tournament, e.g. "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ" */
    val name: String,

    /** The date of the tournament as a formatted string, e.g. "7.JUNI 2025" */
    val datum: String,

    /** Unique identifier for the tournament */
    val number: Int,

    /** List of competitions (Bewerbe) associated with this tournament */
    var bewerbe: List<Bewerb> = emptyList()
)
