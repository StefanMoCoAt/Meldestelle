package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class JwtServiceTest {

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
            expiration = 60.seconds // Kurze Lebensdauer für Tests
        )
    }

    @Test
    fun `generateToken should create a valid JWT with correct claims`() {
        // Arrange
        val userId = "user-123"
        val username = "testuser"
        val permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.PFERD_CREATE)

        // Act
        val token = jwtService.generateToken(userId, username, permissions)

        // Assert
        assertNotNull(token)
        assertTrue(jwtService.validateToken(token).isSuccess)
        assertEquals(userId, jwtService.getUserIdFromToken(token).getOrNull())

        val extractedPermissions = jwtService.getPermissionsFromToken(token).getOrElse { emptyList() }
        assertEquals(2, extractedPermissions.size)
        assertTrue(extractedPermissions.contains(BerechtigungE.PERSON_READ))
        assertTrue(extractedPermissions.contains(BerechtigungE.PFERD_CREATE))
    }

    @Test
    fun `validateToken should return false for token with wrong secret`() {
        // Arrange
        val otherService = JwtService("a-different-wrong-secret-that-is-long-enough-1234567890", testIssuer, testAudience)
        val token = otherService.generateToken("user-123", "test", emptyList())

        // Act & Assert
        assertFalse(jwtService.validateToken(token).isSuccess)
    }

    @Test
    fun `validateToken should return false for expired token`() {
        // Arrange
        val expiredService =
            JwtService(testSecret, testIssuer, testAudience, expiration = (-10).seconds) // bereits abgelaufen
        val token = expiredService.generateToken("user-123", "test", emptyList())

        // Act & Assert
        assertFalse(jwtService.validateToken(token).isSuccess)
    }

    @Test
    fun `getPermissionsFromToken should return empty list for invalid token`() {
        // Arrange
        val invalidToken = "this.is.not.a.valid.token"

        // Act
        val permissions = jwtService.getPermissionsFromToken(invalidToken).getOrElse { emptyList() }

        // Assert
        assertTrue(permissions.isEmpty())
    }
}
