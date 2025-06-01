package at.mocode.model

import kotlinx.serialization.Serializable

@Serializable
data class Nennung(
    val riderName: String,
    val horseName: String,
    val email: String,
    val phone: String,
    val selectedEvents: List<String>,
    val comments: String
)
