package at.mocode.ping.infrastructure

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/ping/simple", "/ping/enhanced", "/ping/health", "/ping/history").permitAll()
                    // Secure endpoints
                    .requestMatchers("/ping/secure").authenticated()
                    // Default deny
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }

        return http.build()
    }
}
