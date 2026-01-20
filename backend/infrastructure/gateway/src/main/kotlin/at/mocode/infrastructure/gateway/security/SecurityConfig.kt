package at.mocode.infrastructure.gateway.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono
import java.time.Duration

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(GatewaySecurityProperties::class)
class SecurityConfig(
    private val securityProperties: GatewaySecurityProperties
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(*securityProperties.publicPaths.toTypedArray()).permitAll()
                    .pathMatchers("/api/ping/**").hasRole("MELD_USER") // Beispiel Rolle
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(realmRolesJwtAuthenticationConverter())
                }
            }
            .build()
    }

    @Bean
    fun reactiveJwtDecoder(
        @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") jwkSetUri: String
    ): ReactiveJwtDecoder {
        return ResilienceReactiveJwtDecoder(jwkSetUri)
    }

    class ResilienceReactiveJwtDecoder(private val jwkSetUri: String) : ReactiveJwtDecoder {
        private val logger = LoggerFactory.getLogger(ResilienceReactiveJwtDecoder::class.java)
        private var delegate: ReactiveJwtDecoder? = null

        override fun decode(token: String): Mono<Jwt> {
            if (delegate == null) {
                synchronized(this) {
                    if (delegate == null) {
                        try {
                            if (jwkSetUri.isBlank()) {
                                throw IllegalArgumentException("JWK Set URI is missing")
                            }
                            logger.info("Attempting to initialize JWT Decoder with URI: {}", jwkSetUri)
                            delegate = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build()
                            logger.info("JWT Decoder successfully initialized.")
                        } catch (e: Exception) {
                            logger.warn("Could not initialize JWT Decoder: {}", e.message)
                            return Mono.error(IllegalStateException("Identity Provider unavailable"))
                        }
                    }
                }
            }
            return delegate!!.decode(token)
                .onErrorResume { e ->
                    logger.debug("JWT decoding failed: {}", e.message)
                    Mono.error(e)
                }
        }
    }

    @Bean
    fun realmRolesJwtAuthenticationConverter(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
            val roles = realmAccess?.get("roles") as? Collection<*> ?: emptyList<Any>()
            roles
                .filterIsInstance<String>()
                .map { role -> SimpleGrantedAuthority("ROLE_${role.uppercase()}") }
        }
        return ReactiveJwtAuthenticationConverterAdapter(converter)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = securityProperties.cors.allowedOriginPatterns.toList()
            allowedMethods = securityProperties.cors.allowedMethods.toList()
            allowedHeaders = securityProperties.cors.allowedHeaders.toList()
            exposedHeaders = securityProperties.cors.exposedHeaders.toList()
            allowCredentials = securityProperties.cors.allowCredentials
            maxAge = securityProperties.cors.maxAge.seconds
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

@ConfigurationProperties(prefix = "gateway.security")
data class GatewaySecurityProperties(
    val cors: CorsProperties = CorsProperties(),
    val publicPaths: List<String> = listOf(
        "/",
        "/fallback/**",
        "/actuator/**",
        "/webjars/**",
        "/v3/api-docs/**",
        "/api/auth/**",
        "/api/ping/public",
        "/api/ping/health",
        "/api/ping/simple"
    )
)

data class CorsProperties(
    val allowedOriginPatterns: Set<String> = setOf("http://localhost:*", "https://*.meldestelle.at"),
    val allowedMethods: Set<String> = setOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"),
    val allowedHeaders: Set<String> = setOf("*"),
    val exposedHeaders: Set<String> = setOf("X-Correlation-ID"),
    val allowCredentials: Boolean = true,
    val maxAge: Duration = Duration.ofHours(1)
)
