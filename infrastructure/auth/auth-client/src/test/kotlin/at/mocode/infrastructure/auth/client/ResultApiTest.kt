package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import com.auth0.jwt.exceptions.JWTVerificationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Comprehensive tests for the Result-based APIs in the auth module.
 * Tests focus on Result type behavior, error handling, and API consistency.
 */
class ResultApiTest {

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

    // ========== Result Success Cases Tests ==========

    @Test
    fun `Result success cases should provide correct values`() {
        // Arrange
        val userId = "user-12345"
        val permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.PFERD_CREATE)
        val token = jwtService.generateToken(userId, "testuser", permissions)

        // Act - Test all Result-based APIs
        val validateResult = jwtService.validateToken(token)
        val userIdResult = jwtService.getUserIdFromToken(token)
        val permissionsResult = jwtService.getPermissionsFromToken(token)

        // Assert - All should be successful
        assertTrue(validateResult.isSuccess)
        assertTrue(validateResult.isFailure.not())
        assertEquals(true, validateResult.getOrNull())
        assertNull(validateResult.exceptionOrNull())

        assertTrue(userIdResult.isSuccess)
        assertTrue(userIdResult.isFailure.not())
        assertEquals(userId, userIdResult.getOrNull())
        assertNull(userIdResult.exceptionOrNull())

        assertTrue(permissionsResult.isSuccess)
        assertTrue(permissionsResult.isFailure.not())
        val extractedPermissions = permissionsResult.getOrNull()!!
        assertEquals(2, extractedPermissions.size)
        assertTrue(extractedPermissions.containsAll(permissions))
        assertNull(permissionsResult.exceptionOrNull())
    }

    @Test
    fun `Result getOrElse should work correctly for success cases`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "test", listOf(BerechtigungE.VEREIN_READ))

        // Act & Assert
        val isValid = jwtService.validateToken(token).getOrElse { false }
        assertTrue(isValid)

        val userId = jwtService.getUserIdFromToken(token).getOrElse { "default" }
        assertEquals("user-123", userId)

        val permissions = jwtService.getPermissionsFromToken(token).getOrElse { emptyList() }
        assertEquals(1, permissions.size)
        assertEquals(BerechtigungE.VEREIN_READ, permissions[0])
    }

    // ========== Result Failure Cases Tests ==========

    @Test
    fun `Result failure cases should contain meaningful error messages`() {
        // Arrange
        val invalidToken = "invalid.jwt.token"

        // Act
        val validateResult = jwtService.validateToken(invalidToken)
        val userIdResult = jwtService.getUserIdFromToken(invalidToken)
        val permissionsResult = jwtService.getPermissionsFromToken(invalidToken)

        // Assert - All should be failures with proper exception types
        assertTrue(validateResult.isFailure)
        assertTrue(validateResult.isSuccess.not())
        assertNull(validateResult.getOrNull())
        assertInstanceOf(JWTVerificationException::class.java, validateResult.exceptionOrNull())

        assertTrue(userIdResult.isFailure)
        assertTrue(userIdResult.isSuccess.not())
        assertNull(userIdResult.getOrNull())
        assertInstanceOf(JWTVerificationException::class.java, userIdResult.exceptionOrNull())

        assertTrue(permissionsResult.isFailure)
        assertTrue(permissionsResult.isSuccess.not())
        assertNull(permissionsResult.getOrNull())
        assertInstanceOf(JWTVerificationException::class.java, permissionsResult.exceptionOrNull())
    }

    @Test
    fun `Result getOrElse should work correctly for failure cases`() {
        // Arrange
        val invalidToken = "invalid.token"

        // Act & Assert
        val isValid = jwtService.validateToken(invalidToken).getOrElse { false }
        assertFalse(isValid)

        val userId = jwtService.getUserIdFromToken(invalidToken).getOrElse { "default-user" }
        assertEquals("default-user", userId)

        val permissions = jwtService.getPermissionsFromToken(invalidToken).getOrElse { emptyList() }
        assertTrue(permissions.isEmpty())
    }

    @Test
    fun `Result getOrDefault should handle different default types`() {
        // Arrange
        val invalidToken = "malformed.jwt"

        // Act & Assert - Test various default value types
        val defaultBoolean = jwtService.validateToken(invalidToken).getOrElse { true }
        assertTrue(defaultBoolean)

        val defaultString = jwtService.getUserIdFromToken(invalidToken).getOrElse { "anonymous" }
        assertEquals("anonymous", defaultString)

        val defaultList = jwtService.getPermissionsFromToken(invalidToken).getOrElse { listOf(BerechtigungE.PERSON_READ) }
        assertEquals(1, defaultList.size)
        assertEquals(BerechtigungE.PERSON_READ, defaultList[0])
    }

    // ========== Result Chaining Tests ==========

    @Test
    fun `Result chaining should work correctly`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "test", listOf(BerechtigungE.PERSON_READ))

        // Act - Chain Result operations
        val chainedResult = jwtService.validateToken(token)
            .map { isValid -> if (isValid) "VALID" else "INVALID" }

        val userChainedResult = jwtService.getUserIdFromToken(token)
            .map { userId -> "User: $userId" }

        val permissionChainedResult = jwtService.getPermissionsFromToken(token)
            .map { permissions -> permissions.map { it.name } }

        // Assert
        assertTrue(chainedResult.isSuccess)
        assertEquals("VALID", chainedResult.getOrNull())

        assertTrue(userChainedResult.isSuccess)
        assertEquals("User: user-123", userChainedResult.getOrNull())

        assertTrue(permissionChainedResult.isSuccess)
        val permissionNames = permissionChainedResult.getOrNull()!!
        assertEquals(1, permissionNames.size)
        assertEquals("PERSON_READ", permissionNames[0])
    }

    @Test
    fun `Result chaining should handle failures correctly`() {
        // Arrange
        val invalidToken = "bad.token"

        // Act - Chain operations that will fail
        val chainedResult = jwtService.validateToken(invalidToken)
            .map { isValid -> "This should not be called" }

        val userChainedResult = jwtService.getUserIdFromToken(invalidToken)
            .map { userId -> "User: $userId" }

        // Assert - Chained operations should not execute on failure
        assertTrue(chainedResult.isFailure)
        assertNull(chainedResult.getOrNull())

        assertTrue(userChainedResult.isFailure)
        assertNull(userChainedResult.getOrNull())
    }

    // ========== Exception Handling Consistency Tests ==========

    @Test
    fun `Exception handling should be consistent across all Result methods`() {
        // Test various types of invalid tokens
        val testCases = listOf(
            "malformed.token",
            "",
            "too.short",
            "way.too.many.parts.in.this.token.structure",
            "null.claims.signature"
        )

        testCases.forEach { invalidToken ->
            // All methods should handle the same invalid input consistently
            val validateResult = jwtService.validateToken(invalidToken)
            val userIdResult = jwtService.getUserIdFromToken(invalidToken)
            val permissionsResult = jwtService.getPermissionsFromToken(invalidToken)

            // All should fail
            assertTrue(validateResult.isFailure, "validateToken should fail for: $invalidToken")
            assertTrue(userIdResult.isFailure, "getUserIdFromToken should fail for: $invalidToken")
            assertTrue(permissionsResult.isFailure, "getPermissionsFromToken should fail for: $invalidToken")

            // All should have non-null exceptions
            assertNotNull(validateResult.exceptionOrNull(), "validateToken should have exception for: $invalidToken")
            assertNotNull(userIdResult.exceptionOrNull(), "getUserIdFromToken should have exception for: $invalidToken")
            assertNotNull(permissionsResult.exceptionOrNull(), "getPermissionsFromToken should have exception for: $invalidToken")
        }
    }

    // ========== Special Edge Cases for Result API ==========

    @Test
    fun `Result API should handle expired tokens consistently`() {
        // Arrange
        val expiredService = JwtService(testSecret, testIssuer, testAudience, expiration = (-10).seconds)
        val expiredToken = expiredService.generateToken("user-123", "test", listOf(BerechtigungE.PERSON_READ))

        // Act
        val validateResult = jwtService.validateToken(expiredToken)
        val userIdResult = jwtService.getUserIdFromToken(expiredToken)
        val permissionsResult = jwtService.getPermissionsFromToken(expiredToken)

        // Assert - All should consistently fail for expired tokens
        assertTrue(validateResult.isFailure)
        assertTrue(userIdResult.isFailure)
        assertTrue(permissionsResult.isFailure)

        // All should have JWT verification exceptions
        assertInstanceOf(JWTVerificationException::class.java, validateResult.exceptionOrNull())
        assertInstanceOf(JWTVerificationException::class.java, userIdResult.exceptionOrNull())
        assertInstanceOf(JWTVerificationException::class.java, permissionsResult.exceptionOrNull())
    }

    @Test
    fun `Result API should handle wrong issuer and audience consistently`() {
        // Arrange
        val wrongConfigService = JwtService(testSecret, "wrong-issuer", "wrong-audience")
        val wrongToken = wrongConfigService.generateToken("user-123", "test", emptyList())

        // Act
        val validateResult = jwtService.validateToken(wrongToken)
        val userIdResult = jwtService.getUserIdFromToken(wrongToken)
        val permissionsResult = jwtService.getPermissionsFromToken(wrongToken)

        // Assert - All should consistently fail
        assertTrue(validateResult.isFailure)
        assertTrue(userIdResult.isFailure)
        assertTrue(permissionsResult.isFailure)

        // All exceptions should be JWT verification exceptions
        assertInstanceOf(JWTVerificationException::class.java, validateResult.exceptionOrNull())
        assertInstanceOf(JWTVerificationException::class.java, userIdResult.exceptionOrNull())
        assertInstanceOf(JWTVerificationException::class.java, permissionsResult.exceptionOrNull())
    }

    // ========== Result API Interoperability Tests ==========

    @Test
    fun `Result API should work well with Kotlin standard library`() {
        // Arrange
        val validToken = jwtService.generateToken("user-123", "test", listOf(BerechtigungE.PERSON_READ, BerechtigungE.PFERD_CREATE))
        val invalidToken = "invalid.token"

        // Act & Assert - Test integration with Kotlin stdlib

        // Use with let
        val letResult = jwtService.validateToken(validToken).getOrNull()?.let { "Token is valid: $it" }
        assertEquals("Token is valid: true", letResult)

        // Use with also
        var sideEffectCalled = false
        jwtService.getUserIdFromToken(validToken).also { result ->
            if (result.isSuccess) sideEffectCalled = true
        }
        assertTrue(sideEffectCalled)

        // Use with takeIf
        val conditionalResult = jwtService.getPermissionsFromToken(validToken)
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
        assertNotNull(conditionalResult)
        assertEquals(2, conditionalResult!!.size)

        // Use with run
        val runResult = jwtService.validateToken(invalidToken).run {
            if (isFailure) "Failed as expected" else "Unexpected success"
        }
        assertEquals("Failed as expected", runResult)
    }

    @Test
    fun `Result API should support functional programming patterns`() {
        // Arrange
        val token = jwtService.generateToken("user-123", "test", listOf(BerechtigungE.PERSON_READ))

        // Act & Assert - Functional patterns

        // Map transformations
        val transformedValidation = jwtService.validateToken(token)
            .map { if (it) 1 else 0 }
            .getOrElse { -1 }
        assertEquals(1, transformedValidation)

        // Filter-like behavior
        val hasReadPermission = jwtService.getPermissionsFromToken(token)
            .map { permissions -> permissions.contains(BerechtigungE.PERSON_READ) }
            .getOrElse { false }
        assertTrue(hasReadPermission)

        // Combine multiple Results
        val combinedCheck = jwtService.validateToken(token).isSuccess &&
                jwtService.getUserIdFromToken(token).isSuccess &&
                jwtService.getPermissionsFromToken(token).isSuccess
        assertTrue(combinedCheck)
    }
}
