package at.mocode.infrastructure.auth

import at.mocode.infrastructure.auth.config.AuthServerConfiguration
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Basic tests for the Auth Server application and configuration.
 * These tests verify the application structure without requiring full Spring context.
 *
 * Note: Custom JWT handling has been removed. Authentication is now fully handled
 * by Keycloak via OAuth2 Resource Server.
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
    fun `auth server configuration should be present`() {
        // Arrange & Act
        val config = AuthServerConfiguration()

        // Assert
        assertNotNull(config)
        assertTrue(config::class.java.isAnnotationPresent(org.springframework.context.annotation.Configuration::class.java)) {
            "AuthServerConfiguration should be annotated with @Configuration"
        }
    }
}
