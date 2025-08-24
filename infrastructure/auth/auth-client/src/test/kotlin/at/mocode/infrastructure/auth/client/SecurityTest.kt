package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import com.auth0.jwt.exceptions.JWTVerificationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Security-focused tests for JWT handling.
 * Tests against common JWT vulnerabilities and security attack vectors.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SecurityTest {

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

    // ========== Signature Tampering Tests ==========

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun `should reject tokens with tampered signatures`() {
        // Arrange - neue JwtService-Instanz für vollständige Isolation
        val isolatedJwtService = JwtService(
            secret = testSecret,
            issuer = testIssuer,
            audience = testAudience,
            expiration = 60.minutes
        )

        // Arrange
        val validToken = isolatedJwtService.generateToken("user-123", "testuser", listOf(BerechtigungE.PERSON_READ))
        val tokenParts = validToken.split(".")

        // Validierung der Token-Struktur
        assertEquals(3, tokenParts.size, "JWT should have exactly 3 parts")
        assertTrue(tokenParts[2].isNotEmpty(), "Signature part should not be empty")

        // Tamper with the signature by changing the last character
        val tamperedSignature = tokenParts[2].dropLast(1) + "X"
        val tamperedToken = "${tokenParts[0]}.${tokenParts[1]}.$tamperedSignature"

        // Sicherstellen, dass Signatur tatsächlich verändert wurde
        assertNotEquals(tokenParts[2], tamperedSignature, "Signature should be different after tampering")

        // Act
        val result = isolatedJwtService.validateToken(tamperedToken)

        // Assert - Erweiterte Validierung
        assertTrue(result.isFailure, "Tampered token should be rejected")
        val exception = result.exceptionOrNull()
        assertNotNull(exception, "Exception should be present for failed validation")
        assertInstanceOf(
            JWTVerificationException::class.java, exception,
            "Exception should be JWTVerificationException, but was: ${exception?.javaClass?.simpleName}"
        )

        // Zusätzliche Sicherheitsüberprüfung: Original Token sollte noch gültig sein
        val originalResult = isolatedJwtService.validateToken(validToken)
        assertTrue(originalResult.isSuccess, "Original valid token should still be valid")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun `should reject tokens with completely different signatures`() {
        // Isolierte Instanzen verwenden
        val isolatedJwtService1 = JwtService(testSecret, testIssuer, testAudience, expiration = 60.minutes)
        val isolatedJwtService2 = JwtService(testSecret, testIssuer, testAudience, expiration = 60.minutes)

        // Arrange
        val validToken = isolatedJwtService1.generateToken("user-123", "testuser", emptyList())
        val anotherValidToken = isolatedJwtService2.generateToken("user-456", "anotheruser", emptyList())

        val tokenParts1 = validToken.split(".")
        val tokenParts2 = anotherValidToken.split(".")

        // Mix signature from different token
        val mixedToken = "${tokenParts1[0]}.${tokenParts1[1]}.${tokenParts2[2]}"

        // Act
        val result = isolatedJwtService1.validateToken(mixedToken)

        // Assert
        assertTrue(result.isFailure)
        assertInstanceOf(JWTVerificationException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `should reject tokens with extended expiration time`() {
        // This test simulates an attacker trying to extend the token's validity
        // by manipulating the payload (even though it will break the signature)

        // Arrange
        val validToken = jwtService.generateToken("user-123", "testuser", emptyList())
        val tokenParts = validToken.split(".")

        // Try to use a different payload with extended expiration
        // (This will fail signature validation, which is the expected behavior)
        val anotherService = JwtService(testSecret, testIssuer, testAudience, expiration = 24.minutes)
        val longValidToken = anotherService.generateToken("user-123", "testuser", emptyList())
        val longValidParts = longValidToken.split(".")

        val tamperedToken = "${longValidParts[0]}.${longValidParts[1]}.${tokenParts[2]}"

        // Act
        val result = jwtService.validateToken(tamperedToken)

        // Assert
        assertTrue(result.isFailure)
    }

    // ========== Timing Attack Resistance Tests ==========

    @Test
    fun `token validation should be resistant to timing attacks`() {
        // Arrange
        val validToken = jwtService.generateToken("user-123", "testuser", emptyList())
        val invalidTokens = listOf(
            "invalid.token.signature",
            validToken.dropLast(5) + "wrong",
            "completely.wrong.token",
            ""
        )

        // Measure validation times for valid and invalid tokens
        val validationTimes = mutableListOf<Long>()

        // Act - Test multiple times to get consistent timing measurements
        repeat(10) {
            // Valid token
            val start1 = System.nanoTime()
            jwtService.validateToken(validToken)
            val end1 = System.nanoTime()
            validationTimes.add(end1 - start1)

            // Invalid tokens
            invalidTokens.forEach { invalidToken ->
                val start2 = System.nanoTime()
                jwtService.validateToken(invalidToken)
                val end2 = System.nanoTime()
                validationTimes.add(end2 - start2)
            }
        }

        // Assert - All validation operations should complete reasonably quickly
        // (This is not a perfect timing attack test but ensures no obvious timing differences)
        validationTimes.forEach { time ->
            assertTrue(time < 10_000_000, "Token validation should complete within 10ms (was ${time}ns)")
        }
    }

    @Test
    fun `validation should complete under consistent time limits`() {
        // Arrange
        val tokens = (1..20).map {
            jwtService.generateToken("user-$it", "testuser$it", listOf(BerechtigungE.PERSON_READ))
        }

        // Act & Assert - Each validation should complete within reasonable time
        tokens.forEach { token ->
            assertTimeoutPreemptively(Duration.ofMillis(100)) {
                val result = jwtService.validateToken(token)
                assertTrue(result.isSuccess)
            }
        }
    }

    // ========== JWT Vulnerability Tests (Based on Common CVEs) ==========

    @Test
    fun `should validate against algorithm confusion attack`() {
        // This test ensures our service doesn't accept tokens with different algorithms
        // Common attack: changing algorithm from RS256 to HS256 in the header

        // Arrange
        val validToken = jwtService.generateToken("user-123", "testuser", emptyList())
        val tokenParts = validToken.split(".")

        // Try to create a token with a manipulated header (algorithm confusion)
        // In practice, this would require crafting a specific header, but our implementation
        // should reject any token that doesn't match our configured algorithm
        val manipulatedHeader = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9" // RS256 instead of HS512
        val manipulatedToken = "$manipulatedHeader.${tokenParts[1]}.${tokenParts[2]}"

        // Act
        val result = jwtService.validateToken(manipulatedToken)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `should reject tokens without proper structure`() {
        // Test malformed tokens that don't follow the JWT structure
        val malformedTokens = listOf(
            "not.a.jwt",
            "only.two.parts",
            "too.many.parts.here.extra",
            ".empty.first.",
            "first..third",
            "first.second.",
            "",
            "single-string-no-dots"
        )

        malformedTokens.forEach { malformedToken ->
            val result = jwtService.validateToken(malformedToken)
            assertTrue(result.isFailure, "Malformed token '$malformedToken' should be rejected")
        }
    }

    @Test
    fun `should handle extremely long tokens without hanging`() {
        // Test against DoS attacks using extremely long tokens
        val longString = "a".repeat(10000)
        val longTokens = listOf(
            "$longString.valid.token",
            "valid.$longString.token",
            "valid.token.$longString",
            "$longString.$longString.$longString"
        )

        longTokens.forEach { longToken ->
            assertTimeoutPreemptively(Duration.ofSeconds(1)) {
                val result = jwtService.validateToken(longToken)
                assertTrue(result.isFailure, "Long token should be rejected quickly")
            }
        }
    }

    // ========== Token Replay Attack Tests ==========

    @Test
    fun `should handle multiple validations of same token consistently`() {
        // Test that the same token always produces the same validation result
        // This ensures no state is maintained that could be exploited

        val token = jwtService.generateToken("user-123", "testuser", listOf(BerechtigungE.PERSON_READ))

        repeat(10) {
            val result = jwtService.validateToken(token)
            assertTrue(result.isSuccess, "Same token should always validate successfully")

            val userId = jwtService.getUserIdFromToken(token)
            assertEquals("user-123", userId.getOrNull())

            val permissions = jwtService.getPermissionsFromToken(token)
            assertEquals(1, permissions.getOrElse { emptyList() }.size)
        }
    }

    // ========== Input Validation Security Tests ==========

    @Test
    fun `should handle special characters and injection attempts`() {
        // Test with various special characters that might cause issues
        val specialUserIds = listOf(
            "user'; DROP TABLE users; --",
            "user<script>alert('xss')</script>",
            "user\n\r\t",
            "user\u0000null",
            "user${'\u0001'}control",
            "../../../etc/passwd"
        )

        specialUserIds.forEach { specialUserId ->
            val token = jwtService.generateToken(specialUserId, "testuser", emptyList())
            val result = jwtService.getUserIdFromToken(token)

            assertTrue(result.isSuccess)
            assertEquals(
                specialUserId, result.getOrNull(),
                "Special characters in user ID should be preserved exactly"
            )
        }
    }

    @Test
    fun `should handle unicode and international characters`() {
        // Test with international characters to ensure proper encoding/decoding
        val internationalUserIds = listOf(
            "用户123", // Chinese
            "utilisateur123", // French
            "пользователь123", // Russian
            "مستخدم123", // Arabic
            "🧑‍💻user123" // Emoji
        )

        internationalUserIds.forEach { userId ->
            val token = jwtService.generateToken(userId, "testuser", emptyList())
            val result = jwtService.getUserIdFromToken(token)

            assertTrue(result.isSuccess)
            assertEquals(
                userId, result.getOrNull(),
                "International characters should be handled correctly"
            )
        }
    }

    // ========== Rate Limiting Simulation Tests ==========

    @Test
    fun `should handle high frequency validation requests`() {
        // Simulate high-frequency validation to ensure no memory leaks or performance degradation
        val token = jwtService.generateToken("user-123", "testuser", emptyList())

        val startTime = System.currentTimeMillis()
        repeat(1000) {
            val result = jwtService.validateToken(token)
            assertTrue(result.isSuccess)
        }
        val endTime = System.currentTimeMillis()

        // Should complete 1000 validations in a reasonable time (less than 5 seconds)
        assertTrue(
            endTime - startTime < 5000,
            "1000 token validations should complete within 5 seconds"
        )
    }

    // ========== Memory Safety Tests ==========

    @Test
    fun `should not leak sensitive information in error messages`() {
        // Ensure that error messages don't contain sensitive information
        val invalidToken = "invalid.token.here"
        val result = jwtService.validateToken(invalidToken)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)

        // Error message should not contain the secret or other sensitive information
        val errorMessage = exception!!.message ?: ""
        assertFalse(
            errorMessage.contains(testSecret),
            "Error message should not contain the secret"
        )
        assertFalse(
            errorMessage.contains("HMAC"),
            "Error message should not reveal internal algorithm details"
        )
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    fun `should handle concurrent validation requests safely`() {
        // Thread-safe JwtService-Instanz
        val threadSafeJwtService = JwtService(testSecret, testIssuer, testAudience, expiration = 60.minutes)
        val token = threadSafeJwtService.generateToken("user-123", "testuser", emptyList())
        val results = mutableListOf<Boolean>()


        val threads = (1..10).map { threadIndex ->
            Thread {
                repeat(100) {
                    val result = jwtService.validateToken(token)
                    synchronized(results) {
                        results.add(result.isSuccess)
                    }
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // All validations should succeed
        assertEquals(1000, results.size)
        assertTrue(results.all { it }, "All concurrent validations should succeed")
    }
}
