package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
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

    init {
        require(secret.length >= 32) { "JWT secret must be at least 32 characters for HMAC512" }
        require(issuer.isNotBlank()) { "JWT issuer must not be blank" }
        require(audience.isNotBlank()) { "JWT audience must not be blank" }
    }

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
            // The library verifier already performs signature validation, so no need for redundant pre-check
            verifier.verify(token)
            Result.success(true)
        } catch (e: JWTVerificationException) {
            // Keep logging minimal to avoid timing variations under high frequency invalid inputs
            logger.debug { "JWT token validation failed" }
            Result.failure(e)
        } catch (e: Exception) {
            logger.debug { "Unexpected error during JWT token validation" }
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

    // ====== Internal helpers for strict signature validation ======
    private fun hasValidSignature(token: String): Boolean {
        return try {
            val parts = token.split('.')
            if (parts.size != 3) return false
            val header = parts[0]
            val payload = parts[1]
            val signature = parts[2]
            if (header.isBlank() || payload.isBlank() || signature.isBlank()) return false

            val mac = Mac.getInstance("HmacSHA512")
            mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA512"))
            val signingInput = "$header.$payload".toByteArray(StandardCharsets.UTF_8)
            val expected = mac.doFinal(signingInput)
            val expectedB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(expected)

            constantTimeEquals(expectedB64, signature)
        } catch (_: Exception) {
            false
        }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray(StandardCharsets.UTF_8)
        val bBytes = b.toByteArray(StandardCharsets.UTF_8)
        var diff = aBytes.size xor bBytes.size
        val minLen = if (aBytes.size < bBytes.size) aBytes.size else bBytes.size
        var i = 0
        while (i < minLen) {
            diff = diff or (aBytes[i].toInt() xor bBytes[i].toInt())
            i++
        }
        return diff == 0
    }
}
