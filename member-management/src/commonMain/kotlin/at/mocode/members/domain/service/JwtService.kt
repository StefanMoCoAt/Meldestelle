package at.mocode.members.domain.service

import at.mocode.members.domain.model.DomUser
import at.mocode.enums.RolleE
import at.mocode.enums.BerechtigungE
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Service for JWT token generation and validation.
 *
 * This is a simplified implementation for multiplatform compatibility.
 * In a production environment, consider using platform-specific JWT libraries.
 */
class JwtService(
    private val userAuthorizationService: UserAuthorizationService,
    private val secret: String = "default-secret-key-change-in-production",
    private val issuer: String = "meldestelle-api",
    private val audience: String = "meldestelle-users",
    private val expirationTimeMillis: Long = 3600000L // 1 hour
) {

    /**
     * Data class representing JWT token information.
     */
    data class TokenInfo(
        val token: String,
        val expiresAt: Instant,
        val userId: Uuid
    )

    /**
     * Data class representing decoded JWT payload.
     */
    data class JwtPayload(
        val userId: Uuid,
        val username: String,
        val email: String,
        val roles: List<RolleE>,
        val permissions: List<BerechtigungE>,
        val issuedAt: Instant,
        val expiresAt: Instant,
        val issuer: String,
        val audience: String
    )

    /**
     * Generates a JWT token for the given user.
     *
     * @param user The user for whom to generate the token
     * @return TokenInfo containing the token and expiration information
     */
    suspend fun generateToken(user: DomUser): TokenInfo {
        val now = Clock.System.now()
        val expiresAt = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + expirationTimeMillis)

        // Get user roles and permissions
        val authInfo = userAuthorizationService.getUserAuthInfo(user.userId)
        val roles = authInfo?.roles ?: emptyList()
        val permissions = authInfo?.permissions ?: emptyList()

        // Create a simple token structure (in production, use proper JWT library)
        val payload = createPayload(user, roles, permissions, now, expiresAt)
        val token = encodeToken(payload)

        return TokenInfo(
            token = token,
            expiresAt = expiresAt,
            userId = user.userId
        )
    }

    /**
     * Validates a JWT token and returns the payload if valid.
     *
     * @param token The JWT token to validate
     * @return JwtPayload if token is valid, null otherwise
     */
    fun validateToken(token: String): JwtPayload? {
        return try {
            val payload = decodeToken(token)

            // Check if token is expired
            if (Clock.System.now() > payload.expiresAt) {
                return null
            }

            // Check issuer and audience
            if (payload.issuer != issuer || payload.audience != audience) {
                return null
            }

            payload
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Refreshes a JWT token if it's still valid but close to expiration.
     *
     * @param token The current JWT token
     * @return New TokenInfo if refresh is successful, null otherwise
     */
    fun refreshToken(token: String): TokenInfo? {
        val payload = validateToken(token) ?: return null

        // Check if token is within refresh window (e.g., last 15 minutes)
        val refreshWindowMillis = 15 * 60 * 1000L // 15 minutes
        val now = Clock.System.now()
        val timeUntilExpiry = payload.expiresAt.toEpochMilliseconds() - now.toEpochMilliseconds()

        if (timeUntilExpiry > refreshWindowMillis) {
            return null // Token is not yet in refresh window
        }

        // Create new token with same user info
        val newExpiresAt = Instant.fromEpochMilliseconds(now.toEpochMilliseconds() + expirationTimeMillis)
        val newPayload = payload.copy(
            issuedAt = now,
            expiresAt = newExpiresAt
        )
        val newToken = encodeToken(newPayload)

        return TokenInfo(
            token = newToken,
            expiresAt = newExpiresAt,
            userId = payload.userId
        )
    }

    /**
     * Extracts user ID from a JWT token without full validation.
     *
     * @param token The JWT token
     * @return User ID if extractable, null otherwise
     */
    fun extractUserId(token: String): Uuid? {
        return try {
            val payload = decodeToken(token)
            payload.userId
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a JWT payload for the given user.
     */
    private fun createPayload(user: DomUser, roles: List<RolleE>, permissions: List<BerechtigungE>, issuedAt: Instant, expiresAt: Instant): JwtPayload {
        return JwtPayload(
            userId = user.userId,
            username = user.username,
            email = user.email,
            roles = roles,
            permissions = permissions,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            issuer = issuer,
            audience = audience
        )
    }

    /**
     * Encodes a JWT payload into a token string.
     * This is a simplified implementation - in production use proper JWT library.
     */
    private fun encodeToken(payload: JwtPayload): String {
        // Simplified token encoding (in production, use proper JWT encoding)
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" // {"alg":"HS256","typ":"JWT"}
        val payloadJson = """
            {
                "userId": "${payload.userId}",
                "username": "${payload.username}",
                "email": "${payload.email}",
                "iat": ${payload.issuedAt.epochSeconds},
                "exp": ${payload.expiresAt.epochSeconds},
                "iss": "${payload.issuer}",
                "aud": "${payload.audience}"
            }
        """.trimIndent()

        // Base64 encode payload (simplified)
        val encodedPayload = payloadJson.encodeToByteArray().let { bytes ->
            // Simple base64-like encoding (in production use proper base64)
            bytes.joinToString("") { byte ->
                val hex = byte.toUByte().toString(16)
                if (hex.length == 1) "0$hex" else hex
            }
        }

        // Create signature (simplified)
        val signature = (header + encodedPayload + secret).hashCode().toString()

        return "$header.$encodedPayload.$signature"
    }

    /**
     * Decodes a JWT token into a payload.
     * This is a simplified implementation - in production use proper JWT library.
     */
    private fun decodeToken(token: String): JwtPayload {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid token format")
        }

        // Simplified decoding (in production, use proper JWT decoding)
        // This is just a placeholder implementation
        throw NotImplementedError("Token decoding not implemented in simplified version")
    }
}
