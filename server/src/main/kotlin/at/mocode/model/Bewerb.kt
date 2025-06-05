package at.mocode.model

import kotlinx.serialization.Serializable

/**
 * Represents a competition (Bewerb) within a tournament.
 * A competition has specific details like number, title, class, and optional task.
 */
@Serializable
data class Bewerb(
    /** Competition number, e.g., 1, 2, etc. */
    val nummer: Int,

    /** Title of the competition, e.g. "Stilspringprüfung" or "Dressurprüfung" */
    val titel: String,

    /** Class/level of the competition, e.g. "60 cm" or "Kl. A" */
    val klasse: String = "",

    /** Optional task identifier, e.g. "DRA 1" */
    val task: String? = null
)
