package at.mocode.infrastructure.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Spring Security Konfiguration für den Auth-Server.
 * Ermöglicht öffentlichen Zugriff auf Actuator Health-Endpoints für Consul Health Checks.
 */
@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { authz ->
                authz
                    // Erlaubt öffentlichen Zugriff auf Health-Endpoints für Consul
                    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                    .requestMatchers("/actuator/info").permitAll()
                    // Alle anderen Endpoints benötigen Authentifizierung
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .build()
    }
}
