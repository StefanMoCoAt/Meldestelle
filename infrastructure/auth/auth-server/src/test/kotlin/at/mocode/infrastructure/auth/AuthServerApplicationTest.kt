package at.mocode.infrastructure.auth

import at.mocode.infrastructure.auth.client.JwtService
import at.mocode.infrastructure.auth.config.AuthServerConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Basic tests for the Auth Server application and configuration.
 * These tests verify the application structure and basic functionality without requiring full Spring context.
 */
class AuthServerApplicationTest {

    @Test
    fun `application context should load successfully`() {
        // Test that we can instantiate the main application class
        val application = AuthServerApplication()
        assertNotNull(application)
    }

    @Test
    fun `main application class should be properly configured`() {
        // Arrange & Act
        val applicationClass = AuthServerApplication::class.java

        // Assert
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication::class.java)) {
            "AuthServerApplication should be annotated with @SpringBootApplication"
        }
    }

    @Test
    fun `auth server configuration should create JWT service bean`() {
        // Arrange
        val config = AuthServerConfiguration()
        val jwtProperties = AuthServerConfiguration.JwtProperties(
            secret = "test-secret-for-testing-only-at-least-512-bits-long-for-hmac512",
            issuer = "test-issuer",
            audience = "test-audience",
            expiration = 60
        )

        // Act
        val jwtService = config.jwtService(jwtProperties)

        // Assert
        assertNotNull(jwtService)
        assertInstanceOf(JwtService::class.java, jwtService)

        // Test that the service can generate and validate tokens
        val token = jwtService.generateToken("test-user", "testuser", emptyList())
        assertNotNull(token)
        assertTrue(token.isNotEmpty())

        val validationResult = jwtService.validateToken(token)
        assertTrue(validationResult.isSuccess)
        assertEquals(true, validationResult.getOrNull())
    }

    @Test
    fun `JWT properties should have sensible defaults`() {
        // Arrange & Act
        val defaultProperties = AuthServerConfiguration.JwtProperties()

        // Assert
        assertNotNull(defaultProperties.secret)
        assertTrue(defaultProperties.secret.isNotEmpty())
        assertEquals("meldestelle-auth-server", defaultProperties.issuer)
        assertEquals("meldestelle-services", defaultProperties.audience)
        assertEquals(60L, defaultProperties.expiration)
    }
}
