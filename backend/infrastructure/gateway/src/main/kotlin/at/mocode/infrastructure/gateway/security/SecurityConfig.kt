package at.mocode.infrastructure.gateway.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import java.time.Duration

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(GatewaySecurityProperties::class)
class SecurityConfig(
  private val securityProperties: GatewaySecurityProperties
) {

  private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

  /**
   * Konfiguriert die zentrale Security-Filter-Kette für das Gateway.
   *
   * Diese Konfiguration nutzt den Standard-OAuth2-Resource-Server von Spring Security,
   * um JWTs (z.B. von Keycloak) automatisch zu validieren.
   */
  @Bean
  fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http { // Start der modernen Kotlin-DSL
      // 1. CORS-Konfiguration anwenden
      cors { }

      // 2. CSRF deaktivieren (für zustandslose APIs)
      csrf { disable() }

      // 3. Routen-Berechtigungen definieren
      authorizeExchange {
        // Öffentlich zugängliche Pfade aus der .yml-Datei laden
        authorize(
          pathMatchers(*securityProperties.publicPaths.toTypedArray()),
          permitAll
        )
        // Ping-API erfordert Admin-Rolle (Realm-Rolle "admin")
        authorize(pathMatchers("/api/ping/**"), hasRole("admin"))
        // Alle anderen Pfade erfordern eine Authentifizierung
        authorize(anyExchange, authenticated)
      }

      // 4. JWT-Validierung via Keycloak aktivieren
      oauth2ResourceServer {
        jwt {
          // Realm-Rollen (Keycloak) -> ROLE_* Authorities
          jwtAuthenticationConverter = realmRolesJwtAuthenticationConverter()
        }
      }
    }
  }

  /**
   * Erstellt einen ReactiveJwtDecoder für die JWT-Validierung.
   *
   * Verwendet die JWK Set URI aus der Konfiguration, um die öffentlichen Schlüssel
   * von Keycloak zu laden. Falls die URI nicht konfiguriert ist oder Keycloak
   * nicht erreichbar ist, wird trotzdem ein Bean erstellt, um Startfehler zu vermeiden.
   */
  @Bean
  fun reactiveJwtDecoder(
    @Value($$"${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") jwkSetUri: String
  ): ReactiveJwtDecoder {
    return if (jwkSetUri.isNotBlank()) {
      try {
        NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build()
      } catch (e: Exception) {
        // Log warning and return a no-op decoder to allow startup
        logger.warn("Failed to configure JWT decoder with JWK Set URI: {} - {}", jwkSetUri, e.message)
        logger.warn("JWT authentication will not work until Keycloak is available")
        createNoOpJwtDecoder()
      }
    } else {
      logger.info("No JWK Set URI configured, using no-op JWT decoder")
      createNoOpJwtDecoder()
    }
  }

  /**
   * Erstellt einen No-Op JWT Decoder für Fälle, in denen Keycloak nicht verfügbar ist.
   * Dieser Decoder lehnt alle Token ab, erlaubt aber den Anwendungsstart.
   */
  private fun createNoOpJwtDecoder(): ReactiveJwtDecoder {
    return ReactiveJwtDecoder { token ->
      throw IllegalStateException("JWT validation is not available - Keycloak may not be running")
    }
  }

  /**
   * Konvertiert Keycloak Realm-Rollen (realm_access.roles) in Spring Authorities (ROLE_*),
   * sodass hasRole("admin") funktioniert.
   */
  @Bean
  fun realmRolesJwtAuthenticationConverter(): org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter {
    val converter = org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter()
    converter.setJwtGrantedAuthoritiesConverter { jwt ->
      val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
      val roles = realmAccess?.get("roles") as? Collection<*> ?: emptyList<Any>()
      roles
        .filterIsInstance<String>()
        .map { role -> org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_${role.uppercase()}") }
    }
    return org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter(converter)
  }

  /**
   * Definiert die zentrale und einzige CORS-Konfiguration für das Gateway.
   */
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

    return UrlBasedCorsConfigurationSource().apply {
      registerCorsConfiguration("/**", configuration)
    }
  }
}

/**
 * Configurations-Properties für alle sicherheitsrelevanten Einstellungen des Gateways.
 */
@ConfigurationProperties(prefix = "gateway.security")
data class GatewaySecurityProperties(
  val cors: CorsProperties = CorsProperties(),
  val publicPaths: List<String> = listOf(
    "/",
    "/fallback/**",
    "/actuator/**",
    "/webjars/**",
    "/v3/api-docs/**",
    "/api/auth/**" // Alle Auth-Endpunkte
  )
)

/**
 * DTO für CORS-Properties mit sinnvollen Standardwerten.
 */
data class CorsProperties(
  val allowedOriginPatterns: Set<String> = setOf("http://localhost:[*]", "https://*.meldestelle.at"),
  val allowedMethods: Set<String> = setOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"),
  val allowedHeaders: Set<String> = setOf("*"),
  val exposedHeaders: Set<String> = setOf("X-Correlation-ID", "X-RateLimit-Limit", "X-RateLimit-Remaining"),
  val allowCredentials: Boolean = true,
  val maxAge: Duration = Duration.ofHours(1)
)
