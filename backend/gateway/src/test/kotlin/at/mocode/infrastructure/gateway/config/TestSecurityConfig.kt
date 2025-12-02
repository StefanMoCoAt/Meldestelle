package at.mocode.infrastructure.gateway.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Test-Konfiguration für Security-Beans.
 * Stellt einen Mock ReactiveJwtDecoder und eine Security-Konfiguration bereit,
 * die alle Anfragen für Test-Zwecke erlaubt.
 */
@TestConfiguration
class TestSecurityConfig {

    /**
     * Mock ReactiveJwtDecoder für Tests.
     * Validiert keine echten JWTs, sondern akzeptiert alle Token für Test-Zwecke.
     */
    @Bean
    @Primary
    fun mockReactiveJwtDecoder(): ReactiveJwtDecoder {
        return ReactiveJwtDecoder { token ->
            // Erstelle ein Mock-JWT mit minimalen Claims
            val jwt = Jwt.withTokenValue(token)
                .header("alg", "none")
                .header("typ", "JWT")
                .claim("sub", "test-user")
                .claim("scope", "read write")
                .claim("preferred_username", "test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()

            Mono.just(jwt)
        }
    }

    /**
     * Test Security Web Filter Chain, die alle Anfragen erlaubt.
     * Dies ermöglicht Tests von Routing, CORS und Filtern ohne Authentifizierung.
     */
    @Bean
    @Primary
    fun testSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            authorizeExchange {
                authorize(anyExchange, permitAll)
            }
        }
    }
}
