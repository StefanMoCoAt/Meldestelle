package at.mocode.infrastructure.gateway.config

import at.mocode.infrastructure.auth.client.JwtService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlin.time.Duration.Companion.minutes

/**
 * Spring-Konfiguration für JWT-Verarbeitung im Gateway.
 * Stellt den JwtService-Bean für die Token-Validierung bereit.
 */
@Configuration
@EnableConfigurationProperties(JwtConfiguration.JwtProperties::class)
class JwtConfiguration {

    /**
     * Erstellt einen JwtService-Bean für JWT-Token-Validierung im Gateway.
     */
    @Bean
    fun jwtService(jwtProperties: JwtProperties): JwtService {
        // Basic safeguard: warn if default secret is used
        if (jwtProperties.secret == "default-secret-for-development-only-please-change-in-production") {
            System.err.println("[GATEWAY SECURITY WARNING] Using default JWT secret – DO NOT use this in production!")
        }
        return JwtService(
            secret = jwtProperties.secret,
            issuer = jwtProperties.issuer,
            audience = jwtProperties.audience,
            expiration = jwtProperties.expiration.minutes
        )
    }

    /**
     * Konfigurationseigenschaften für JWT-Einstellungen im Gateway.
     */
    @ConfigurationProperties(prefix = "gateway.security.jwt")
    @Validated
    data class JwtProperties(
        @field:NotBlank
        @field:Size(min = 32, message = "JWT secret must be at least 32 characters for HMAC512")
        val secret: String = "default-secret-for-development-only-please-change-in-production",
        @field:NotBlank
        val issuer: String = "meldestelle-auth-server",
        @field:NotBlank
        val audience: String = "meldestelle-services",
        @field:Min(1)
        val expiration: Long = 60 // minutes
    )
}
