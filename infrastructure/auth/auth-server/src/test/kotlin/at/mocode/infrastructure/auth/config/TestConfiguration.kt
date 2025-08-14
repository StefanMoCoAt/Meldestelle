package at.mocode.infrastructure.auth.config

import at.mocode.infrastructure.auth.client.JwtService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import kotlin.time.Duration.Companion.minutes

/**
 * Test configuration for Auth Server integration tests.
 * Provides minimal bean configuration needed for tests to run.
 */
@TestConfiguration
class AuthServerTestConfiguration {

    /**
     * Provides a JwtService bean for testing with test-specific configuration.
     */
    @Bean
    @Primary
    fun testJwtService(): JwtService {
        return JwtService(
            secret = "test-secret-for-testing-only-at-least-512-bits-long-for-hmac512-algorithm",
            issuer = "test-issuer",
            audience = "test-audience",
            expiration = 60.minutes
        )
    }
}
