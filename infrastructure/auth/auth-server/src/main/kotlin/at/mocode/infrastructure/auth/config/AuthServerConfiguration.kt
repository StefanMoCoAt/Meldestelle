package at.mocode.infrastructure.auth.config

import at.mocode.infrastructure.auth.client.JwtService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.time.Duration.Companion.minutes

/**
 * Spring configuration for the Auth Server module.
 * Provides the necessary beans and configuration for JWT handling and authentication.
 */
@Configuration
@EnableConfigurationProperties(AuthServerConfiguration.JwtProperties::class)
class AuthServerConfiguration {

    /**
     * Creates a JwtService bean with configuration from application properties.
     */
    @Bean
    fun jwtService(jwtProperties: JwtProperties): JwtService {
        return JwtService(
            secret = jwtProperties.secret,
            issuer = jwtProperties.issuer,
            audience = jwtProperties.audience,
            expiration = jwtProperties.expiration.minutes
        )
    }

    /**
     * Configuration properties for JWT settings.
     */
    @ConfigurationProperties(prefix = "auth.jwt")
    data class JwtProperties(
        val secret: String = "default-secret-for-development-only-please-change-in-production",
        val issuer: String = "meldestelle-auth-server",
        val audience: String = "meldestelle-services",
        val expiration: Long = 60 // minutes
    )
}
