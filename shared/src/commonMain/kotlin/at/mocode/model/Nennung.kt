package at.mocode.model

import kotlinx.serialization.Serializable

/**
 * Represents a tournament registration (Nennung) submitted by a participant.
 * Contains all information needed for registering a rider and horse for specific events.
 */
@Serializable
data class Nennung(
    /** Name of the rider/participant */
    val riderName: String,

    /** Name of the horse */
    val horseName: String,

    /** Email address for contact */
    val email: String,

    /** Phone number for contact */
    val phone: String,

    /** List of selected event identifiers the participant wants to register for */
    val selectedEvents: List<String>,

    /** Additional comments or special requests */
    val comments: String,

    /** Reference to the tournament being registered for (optional) */
    val turnier: Turnier? = null
)
