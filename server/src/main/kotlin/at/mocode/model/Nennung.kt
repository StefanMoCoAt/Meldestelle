package at.mocode.model

import kotlinx.serialization.Serializable

@Serializable
data class Nennung(
    /** Name of the rider */
    val riderName: String,

    /** Name of the horse */
    val horseName: String,

    /** Email address for contact */
    val email: String,

    /** Phone number for contact */
    val phone: String,

    /** List of selected event numbers */
    val selectedEvents: List<String>,

    /** Additional comments or wishes */
    val comments: String,

    /** The tournament this registration is for */
    val turnier: Turnier
)
