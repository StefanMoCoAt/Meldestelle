package at.mocode.infrastructure.auth.client

import at.mocode.core.domain.model.BerechtigungE
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

/**
 * Service for JWT token generation and validation.
 */
class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val expirationInMinutes: Long = 60
) {
    /**
     * Generates a JWT token for the given user.
     *
     * @param userId The user ID
     * @param username The username
     * @param permissions The user's permissions
     * @return The generated JWT token
     */
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
            .withExpiresAt(Date(System.currentTimeMillis() + expirationInMinutes * 60 * 1000))
            .sign(Algorithm.HMAC512(secret))
    }

    /**
     * Validates a JWT token.
     *
     * @param token The JWT token to validate
     * @return True if the token is valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        return try {
            JWT.require(Algorithm.HMAC512(secret))
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)
            true
        } catch (e: Exception) {
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
            JWT.require(Algorithm.HMAC512(secret))
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)
                .subject
        } catch (e: Exception) {
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
            val decodedJWT = JWT.require(Algorithm.HMAC512(secret))
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token)

            val permissionStrings = decodedJWT.getClaim("permissions").asArray(String::class.java)
            permissionStrings.mapNotNull {
                try {
                    BerechtigungE.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
