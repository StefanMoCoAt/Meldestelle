package at.mocode.infrastructure.gateway.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * Minimal reactive security configuration for the Gateway.
 *
 * Rationale:
 * - During tests, Spring Security is on the classpath (testImplementation), which enables
 *   security auto-configuration and can lock down all endpoints unless a SecurityWebFilterChain is provided.
 * - The Gateway enforces auth using a GlobalFilter (JwtAuthenticationFilter) when enabled via property,
 *   so the SecurityWebFilterChain should stay permissive and let the filter do the auth work.
 */
@Configuration
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { }
            .authorizeExchange { exchanges ->
                exchanges
                    .anyExchange().permitAll()
            }
            .build()
    }
}
