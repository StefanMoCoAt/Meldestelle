package at.mocode.clients.shared.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val roles: Set<String> = emptySet(),
    val isActive: Boolean = true
)

@Serializable
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)
