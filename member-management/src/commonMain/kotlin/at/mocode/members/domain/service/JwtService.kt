package at.mocode.members.domain.service

import at.mocode.members.domain.model.DomUser
import at.mocode.enums.BerechtigungE
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant

/**
 * Contains the information extracted from a JWT token.
 */
data class TokenInfo(
    val userId: Uuid,
    val personId: Uuid,
    val username: String,
    val permissions: List<BerechtigungE>,
    val issuedAt: Instant,
    val expiresAt: Instant
)

/**
 * Service for JWT token generation and validation.
 * Platform-specific implementation required.
 */
expect class JwtService {
    suspend fun createToken(user: DomUser): String
    fun validateToken(token: String): TokenInfo?
}
