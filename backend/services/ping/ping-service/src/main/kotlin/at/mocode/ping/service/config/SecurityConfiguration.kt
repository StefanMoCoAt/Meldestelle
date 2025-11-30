package at.mocode.ping.service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Security configuration for the Ping Service.
 * Enables method-level security for fine-grained authorization control.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Allow health check endpoints
                    .requestMatchers("/actuator/**", "/health/**").permitAll()
                    // Allow ping endpoints for monitoring (these are typically public)
                    .requestMatchers("/ping/**").permitAll()
                    // All other endpoints require authentication (handled by method-level security)
                    .anyRequest().authenticated()
            }
            .build()
    }
}
