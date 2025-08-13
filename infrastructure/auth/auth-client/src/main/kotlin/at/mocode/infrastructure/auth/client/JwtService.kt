package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Service for JWT token generation and validation.
 */
class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val expiration: Duration = 60.minutes
) {
    private val algorithm = Algorithm.HMAC512(secret)
    private val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(
        userId: String,
        username: String,
        permissions: List<BerechtigungE>
    ): String {
        return JWT.create()
            .withSubject(userId)
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("username", username)
            .withArrayClaim("permissions", permissions.map { it.name }.toTypedArray())
            .withExpiresAt(Date(System.currentTimeMillis() + expiration.inWholeMilliseconds))
            .sign(algorithm)
    }

    /**
     * Validates a JWT token.
     *
     * @param token The JWT token to validate
     * @return True if the token is valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        return try {
            verifier.verify(token)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Gets the user ID from a JWT token.
     *
     * @param token The JWT token
     * @return The user ID, or null if the token is invalid
     */
    fun getUserIdFromToken(token: String): String? {
        return try {
            verifier.verify(token).subject
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Gets the permissions from a JWT token.
     *
     * @param token The JWT token
     * @return The permissions, or an empty list if the token is invalid
     */
    fun getPermissionsFromToken(token: String): List<BerechtigungE> {
        return try {
            val decodedJWT = verifier.verify(token)
            val permissionStrings = decodedJWT.getClaim("permissions").asArray(String::class.java)
            permissionStrings?.mapNotNull {
                try {
                    BerechtigungE.valueOf(it)
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
