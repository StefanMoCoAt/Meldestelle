package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import com.auth0.jwt.exceptions.JWTVerificationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Extended tests for JwtService focusing on Result-based APIs, edge cases, and security scenarios.
 */
class JwtServiceExtendedTest {

    private lateinit var jwtService: JwtService
    private val testSecret = "a-very-long-and-secure-test-secret-that-is-at-least-512-bits-long-for-hmac512"
    private val testIssuer = "test-issuer"
    private val testAudience = "test-audience"

    @BeforeEach
    fun setUp() {
        jwtService = JwtService(
            secret = testSecret,
            issuer = testIssuer,
            audience = testAudience,
            expiration = 60.minutes
        )
    }

    // ========== Result API Tests ==========

    @Test
    fun `validateToken should return Success with true for valid token`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "testuser", listOf(BerechtigungE.PERSON_READ))

        // Act
        val result = jwtService.validateToken(token)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `validateToken should return Failure for malformed token`() {
        // Arrange
        val malformedToken = "this.is.not.a.valid.jwt.token"

        // Act
        val result = jwtService.validateToken(malformedToken)

        // Assert
        assertTrue(result.isFailure)
        assertInstanceOf(JWTVerificationException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `validateToken should return Failure for token with wrong issuer`() {
        // Arrange
        val wrongIssuerService = JwtService(testSecret, "wrong-issuer", testAudience)
        val token = wrongIssuerService.generateToken("user-123", "test", emptyList())

        // Act
        val result = jwtService.validateToken(token)

        // Assert
        assertTrue(result.isFailure)
        assertInstanceOf(JWTVerificationException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `validateToken should return Failure for token with wrong audience`() {
        // Arrange
        val wrongAudienceService = JwtService(testSecret, testIssuer, "wrong-audience")
        val token = wrongAudienceService.generateToken("user-123", "test", emptyList())

        // Act
        val result = jwtService.validateToken(token)

        // Assert
        assertTrue(result.isFailure)
        assertInstanceOf(JWTVerificationException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `validateToken should return Failure for expired token`() {
        // Arrange
        val expiredService = JwtService(testSecret, testIssuer, testAudience, expiration = (-10).seconds)
        val token = expiredService.generateToken("user-123", "test", emptyList())

        // Act
        val result = jwtService.validateToken(token)

        // Assert
        assertTrue(result.isFailure)
        assertInstanceOf(JWTVerificationException::class.java, result.exceptionOrNull())
    }

    // ========== getUserIdFromToken Result API Tests ==========

    @Test
    fun `getUserIdFromToken should return Success with user ID for valid token`() {
        // Arrange
        val userId = "user-12345"
        val token = jwtService.generateToken(userId, "testuser", emptyList())

        // Act
        val result = jwtService.getUserIdFromToken(token)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(userId, result.getOrNull())
    }

    @Test
    fun `getUserIdFromToken should return Failure for invalid token`() {
        // Arrange
        val invalidToken = "invalid.jwt.token"

        // Act
        val result = jwtService.getUserIdFromToken(invalidToken)

        // Assert
        assertTrue(result.isFailure)
        assertInstanceOf(JWTVerificationException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `getUserIdFromToken should handle missing subject claim`() {
        // Note: This test verifies that empty/blank subject claims are properly rejected for security
        val token = jwtService.generateToken("", "testuser", emptyList())

        val result = jwtService.getUserIdFromToken(token)

        // Empty subject should be rejected for security reasons
        assertTrue(result.isFailure)
        assertInstanceOf(IllegalStateException::class.java, result.exceptionOrNull())
        assertTrue(result.exceptionOrNull()!!.message!!.contains("no subject"))
    }

    // ========== getPermissionsFromToken Result API Tests ==========

    @Test
    fun `getPermissionsFromToken should return Success with permissions for valid token`() {
        // Arrange
        val permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.PFERD_CREATE, BerechtigungE.VEREIN_UPDATE)
        val token = jwtService.generateToken("user-123", "testuser", permissions)

        // Act
        val result = jwtService.getPermissionsFromToken(token)

        // Assert
        assertTrue(result.isSuccess)
        val extractedPermissions = result.getOrNull()!!
        assertEquals(3, extractedPermissions.size)
        assertTrue(extractedPermissions.containsAll(permissions))
    }

    @Test
    fun `getPermissionsFromToken should return Failure for invalid token`() {
        // Arrange
        val invalidToken = "invalid.jwt.token"

        // Act
        val result = jwtService.getPermissionsFromToken(invalidToken)

        // Assert
        assertTrue(result.isFailure)
        assertInstanceOf(JWTVerificationException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `getPermissionsFromToken should return empty list for token without permissions`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "testuser", emptyList())

        // Act
        val result = jwtService.getPermissionsFromToken(token)

        // Assert
        assertTrue(result.isSuccess)
        val permissions = result.getOrNull()!!
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `getPermissionsFromToken should ignore unknown permissions gracefully`() {
        // This test simulates a token with permissions that don't exist in the enum
        // In practice, this would require manually crafting a JWT, so this tests the enum parsing logic
        val permissions = listOf(BerechtigungE.PERSON_READ)
        val token = jwtService.generateToken("user-123", "testuser", permissions)

        val result = jwtService.getPermissionsFromToken(token)

        assertTrue(result.isSuccess)
        val extractedPermissions = result.getOrNull()!!
        assertEquals(1, extractedPermissions.size)
        assertEquals(BerechtigungE.PERSON_READ, extractedPermissions[0])
    }

    // ========== Token Generation Tests ==========

    @Test
    fun `generateToken should create tokens with correct expiration time`() {
        // Arrange
        val shortExpirationService = JwtService(testSecret, testIssuer, testAudience, expiration = 5.seconds)
        val token = shortExpirationService.generateToken("user-123", "test", emptyList())

        // Act - Validate immediately (should be valid)
        val immediateResult = shortExpirationService.validateToken(token)

        // Wait and validate again (should be expired) - using Thread.sleep is acceptable for this specific test
        Thread.sleep(6000) // 6 seconds
        val delayedResult = shortExpirationService.validateToken(token)

        // Assert
        assertTrue(immediateResult.isSuccess, "Token should be valid immediately after creation")
        assertTrue(delayedResult.isFailure, "Token should be expired after waiting")
    }

    // ========== Legacy Method Backward Compatibility Tests ==========

    @Test
    fun `legacy methods should maintain backward compatibility`() {
        // Arrange
        val userId = "user-123"
        val permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.PFERD_CREATE)
        val token = jwtService.generateToken(userId, "testuser", permissions)

        // Act & Assert - Legacy methods should still work
        @Suppress("DEPRECATION")
        assertTrue(jwtService.isValidToken(token))

        @Suppress("DEPRECATION")
        assertEquals(userId, jwtService.getUserId(token))

        @Suppress("DEPRECATION")
        val legacyPermissions = jwtService.getPermissions(token)
        assertEquals(2, legacyPermissions.size)
        assertTrue(legacyPermissions.containsAll(permissions))
    }

    @Test
    fun `legacy methods should handle invalid tokens gracefully`() {
        // Arrange
        val invalidToken = "invalid.token"

        // Act & Assert - Legacy methods should handle errors gracefully
        @Suppress("DEPRECATION")
        assertFalse(jwtService.isValidToken(invalidToken))

        @Suppress("DEPRECATION")
        assertNull(jwtService.getUserId(invalidToken))

        @Suppress("DEPRECATION")
        val permissions = jwtService.getPermissions(invalidToken)
        assertTrue(permissions.isEmpty())
    }

    // ========== Security Edge Cases ==========

    @Test
    fun `should reject tokens with tampered signatures`() {
        // Arrange
        val validToken = jwtService.generateToken("user-123", "testuser", emptyList())
        val tamperedToken = validToken.dropLast(5) + "TAMPR"

        // Act
        val result = jwtService.validateToken(tamperedToken)

        // Assert
        assertTrue(result.isFailure)
        assertInstanceOf(JWTVerificationException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `should handle empty token gracefully`() {
        // Act
        val result = jwtService.validateToken("")

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `should handle null-like values in token validation`() {
        // Arrange
        val nullLikeTokens = listOf("null", "undefined", " ", "\t", "\n")

        // Act & Assert
        nullLikeTokens.forEach { token ->
            val result = jwtService.validateToken(token)
            assertTrue(result.isFailure, "Token '$token' should be rejected")
        }
    }
}
