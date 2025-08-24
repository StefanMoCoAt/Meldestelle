package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import mu.KotlinLogging
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
    private val logger = KotlinLogging.logger {}

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
     * @return Result with true if the token is valid, or failure with error details
     */
    fun validateToken(token: String): Result<Boolean> {
        return try {
            verifier.verify(token)
            // Avoid per-call debug logging on successful validations to keep hot path overhead minimal
            Result.success(true)
        } catch (e: JWTVerificationException) {
            logger.warn { "JWT token validation failed: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during JWT token validation" }
            Result.failure(e)
        }
    }

    /**
     * Validates a JWT token (legacy method for backward compatibility).
     *
     * @param token The JWT token to validate
     * @return True if the token is valid, false otherwise
     */
    @Deprecated("Use validateToken(token: String): Result<Boolean> instead", ReplaceWith("validateToken(token).isSuccess"))
    fun isValidToken(token: String): Boolean {
        return validateToken(token).isSuccess
    }

    /**
     * Gets the user ID from a JWT token.
     *
     * @param token The JWT token
     * @return Result with the user ID, or failure with error details
     */
    fun getUserIdFromToken(token: String): Result<String> {
        return try {
            val subject = verifier.verify(token).subject
            if (subject.isNullOrBlank()) {
                logger.warn { "JWT token has no subject (user ID)" }
                Result.failure(IllegalStateException("JWT token has no subject"))
            } else {
                logger.debug { "Successfully extracted user ID from JWT token" }
                Result.success(subject)
            }
        } catch (e: JWTVerificationException) {
            logger.warn { "Failed to extract user ID from JWT token: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error while extracting user ID from JWT token" }
            Result.failure(e)
        }
    }

    /**
     * Gets the user ID from a JWT token (legacy method for backward compatibility).
     *
     * @param token The JWT token
     * @return The user ID, or null if the token is invalid
     */
    @Deprecated("Use getUserIdFromToken(token: String): Result<String> instead", ReplaceWith("getUserIdFromToken(token).getOrNull()"))
    fun getUserId(token: String): String? {
        return getUserIdFromToken(token).getOrNull()
    }

    /**
     * Gets the permissions from a JWT token.
     *
     * @param token The JWT token
     * @return Result with the permissions, or failure with error details
     */
    fun getPermissionsFromToken(token: String): Result<List<BerechtigungE>> {
        return try {
            val decodedJWT = verifier.verify(token)
            val permissionStrings = decodedJWT.getClaim("permissions").asArray(String::class.java)
            val permissions = permissionStrings?.mapNotNull { permissionString ->
                try {
                    BerechtigungE.valueOf(permissionString)
                } catch (_: IllegalArgumentException) {
                    logger.warn { "Unknown permission in JWT token: $permissionString" }
                    null
                }
            } ?: emptyList()

            logger.debug { "Successfully extracted ${permissions.size} permissions from JWT token" }
            Result.success(permissions)
        } catch (e: JWTVerificationException) {
            logger.warn { "Failed to extract permissions from JWT token: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error while extracting permissions from JWT token" }
            Result.failure(e)
        }
    }

    /**
     * Gets the permissions from a JWT token (legacy method for backward compatibility).
     *
     * @param token The JWT token
     * @return The permissions, or an empty list if the token is invalid
     */
    @Deprecated("Use getPermissionsFromToken(token: String): Result<List<BerechtigungE>> instead", ReplaceWith("getPermissionsFromToken(token).getOrElse { emptyList() }"))
    fun getPermissions(token: String): List<BerechtigungE> {
        return getPermissionsFromToken(token).getOrElse { emptyList() }
    }
}
