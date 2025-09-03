package at.mocode.infrastructure.gateway.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import java.time.Duration

/**
 * Erweiterte reaktive Sicherheitskonfiguration für das Gateway.
 *
 * ARCHITEKTUR-ÜBERBLICK:
 * ======================
 * Diese Konfiguration stellt die grundlegende Sicherheits-Schicht für das Spring Cloud Gateway bereit.
 * Sie arbeitet zusammen mit mehreren weiteren Sicherheitskomponenten:
 *
 * 1. JwtAuthenticationFilter (GlobalFilter) – Validiert JWT-Tokens und authentifiziert Benutzer
 * 2. RateLimitingFilter (GlobalFilter) – Bietet IP-basiertes Rate-Limiting mit benutzerbezogenen Limits
 * 3. CorrelationIdFilter (GlobalFilter) – Fügt Request-Tracing-Fähigkeiten hinzu
 * 4. EnhancedLoggingFilter (GlobalFilter) – Liefert strukturiertes Request/Response-Logging
 *
 * SICHERHEITSSTRATEGIE:
 * =====================
 * Das Gateway verwendet einen mehrschichtigen Sicherheitsansatz:
 * - Diese SecurityWebFilterChain liefert grundlegende Einstellungen (CORS, CSRF, Basis-Header)
 * - Der JwtAuthenticationFilter übernimmt die eigentliche Authentifizierung, wenn per Property aktiviert
 * - Die SecurityWebFilterChain bleibt permissiv (permitAll), damit der JWT-Filter den Zugriff steuert
 * - Rate-Limiting- und Logging-Filter liefern operative Sicherheit und Monitoring
 *
 * ENTWURFSBEGRÜNDUNG:
 * ===================
 * - Während Tests ist Spring Security auf dem Classpath (testImplementation), was
 *   die Auto-Konfiguration aktiviert und alle Endpunkte sperren kann, sofern keine SecurityWebFilterChain bereitgestellt wird
 * - Das Gateway erzwingt Authentifizierung über den JwtAuthenticationFilter (falls per Property aktiviert),
 *   daher sollte die SecurityWebFilterChain permissiv bleiben und sich auf grundlegende Belange konzentrieren
 * - Explizite CORS-Konfiguration stellt eine korrekte Behandlung von Cross-Origin-Anfragen aus Web-Clients sicher
 * - Konfigurierbare Properties erlauben umgebungsspezifische Sicherheitseinstellungen ohne Codeänderungen
 * - CSRF-Schutz ist deaktiviert, da er für zustandslose JWT-basierte Authentifizierung nicht benötigt wird
 *
 * CORS-INTEGRATION:
 * =================
 * Die CORS-Konfiguration arbeitet mit der bestehenden Filterkette zusammen:
 * - Erlaubt Anfragen von konfigurierten Ursprüngen (Dev/Prod-Umgebungen)
 * - Gibt benutzerdefinierte Header aus Gateway-Filtern frei (Korrelations-IDs, Rate-Limits)
 * - Unterstützt Credentials für JWT-Authentifizierung
 * - Cacht Preflight-Antworten für bessere Performance
 *
 * TESTHINWEISE:
 * =============
 * - Die Konfiguration ist so gestaltet, dass sie nahtlos mit bestehenden Sicherheitstests funktioniert
 * - Das Test-Profil kann CORS-Einstellungen bei Bedarf überschreiben
 * - Eine permissive Autorisierung stellt sicher, dass Tests sich auf die Sicherheit der Filterebene konzentrieren können
 */
@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties::class)
class SecurityConfig(
    private val securityProperties: GatewaySecurityProperties
) {

    /**
     * Hauptkonfiguration der Spring-Security-Filterkette.
     *
     * Diese Methode konfiguriert die reaktive Sicherheits-Filterkette mit:
     * - CSRF deaktiviert für zustandslosen API-Betrieb
     * - Expliziter CORS-Konfiguration für Cross-Origin-Unterstützung
     * - Permissiver Autorisierung (Authentifizierung durch den JWT-Filter)
     *
     * Die Konfiguration bleibt kompatibel mit der bestehenden Filterarchitektur
     * und bietet zugleich bessere CORS-Steuerung und Konfigurierbarkeit.
     */
    @Bean
    fun springSecurityFilterChain(): SecurityWebFilterChain {
        return ServerHttpSecurity.http()
            .csrf { csrf ->
                // CSRF für zustandsloses API-Gateway deaktivieren
                // CSRF-Schutz ist für JWT-basierte zustandslose Authentifizierung nicht erforderlich
                // Das Gateway arbeitet als zustandsloser Proxy ohne Session-Zustand
                csrf.disable()
            }
            .cors { cors ->
                // Explizite CORS-Konfiguration anstelle des Defaults verwenden
                // Dies ermöglicht eine bessere Kontrolle über Cross-Origin-Zugriffsrichtlinien
                cors.configurationSource(corsConfigurationSource())
            }
            .httpBasic { basic ->
                // HTTP Basic Auth für zustandslose API deaktivieren
                basic.disable()
            }
            .formLogin { form ->
                // Formular-Login für API-Gateway deaktivieren
                form.disable()
            }
            .authorizeExchange { exchanges ->
                // Alle Anfragen durch Spring Security erlauben
                // Authentifizierung und Autorisierung erfolgen durch den JwtAuthenticationFilter
                // Dieser Ansatz bewahrt die bestehende Sicherheitsarchitektur und
                // ermöglicht dem JWT-Filter granulare Zugriffskontroll-Entscheidungen
                exchanges.anyExchange().permitAll()
            }
            .build()
    }

    /**
     * Explizite CORS-Konfigurationsquelle.
     *
     * Dieser Bean bietet eine detaillierte Steuerung der Cross-Origin-Resource-Sharing-Einstellungen
     * und ersetzt die leere Standard-CORS-Konfiguration durch explizite, konfigurierbare Einstellungen.
     *
     * Schlüsselfunktionen:
     * - Umgebungsspezifische erlaubte Ursprünge (Allowed Origins)
     * - Umfassende Unterstützung für HTTP-Methoden
     * - JWT-bewusste Header-Konfiguration
     * - Integration mit Headern aus Gateway-Filtern
     * - Performance-optimiertes Preflight-Caching
     *
     * Die Konfiguration ist darauf ausgelegt, mit typischen Webanwendungs-Architekturen zu funktionieren,
     * bei denen ein JavaScript-Frontend API-Aufrufe an das Gateway sendet.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // Erlaubte Ursprünge – pro Umgebung konfigurierbar
            // Entwicklung: localhost-URLs für lokale Tests
            // Produktion: domainspezifische URLs für ausgelieferte Anwendungen
            allowedOrigins = securityProperties.cors.allowedOrigins.toList()


            // Erlaubte HTTP-Methoden – umfassende REST-API-Unterstützung
            // Enthält alle Standardmethoden plus OPTIONS für Preflight-Anfragen
            allowedMethods = securityProperties.cors.allowedMethods.toList()

            // Erlaubte Request-Header – beinhaltet JWT und benutzerdefinierte Header
            // Authorization: für JWT Bearer Tokens
            // X-Correlation-ID: für Request-Tracing
            // Standard-Header: Content-Type, Accept, etc.
            allowedHeaders = securityProperties.cors.allowedHeaders.toList()

            // Sichtbare Response-Header – ermöglicht Client-Zugriff auf benutzerdefinierte Header
            // Beinhaltet Header, die von Gateway-Filtern hinzugefügt werden:
            // - X-Correlation-ID vom CorrelationIdFilter
            // - X-RateLimit-* vom RateLimitingFilter
            exposedHeaders = securityProperties.cors.exposedHeaders.toList()

            // Credentials erlauben – erforderlich für JWT-Authentifizierung
            // Aktiviert Cookies und Authorization-Header in Cross-Origin-Anfragen
            allowCredentials = securityProperties.cors.allowCredentials

            // Preflight-Cache-Dauer – Performance-Optimierung
            // Reduziert die Anzahl an OPTIONS-Anfragen für wiederholte API-Aufrufe
            maxAge = securityProperties.cors.maxAge.seconds
        }

        return UrlBasedCorsConfigurationSource().apply {
            // CORS-Konfiguration auf alle Gateway-Routen anwenden
            registerCorsConfiguration("/**", configuration)
        }
    }
}

/**
 * Konfigurationseigenschaften für die Sicherheits-Einstellungen des Gateways.
 *
 * Ermöglicht umgebungsspezifische Sicherheitskonfiguration über application.yml/-properties.
 * Dieser Ansatz erlaubt unterschiedliche Sicherheitseinstellungen für Entwicklung, Test und
 * Produktion, ohne Codeänderungen vornehmen zu müssen.
 *
 * Beispielkonfiguration in application.yml:
 * ```yaml
 * gateway:
 *   security:
 *     cors:
 *       allowed-origins:
 *         - http://localhost:3000
 *         - https://app.meldestelle.at
 *       allowed-methods:
 *         - GET
 *         - POST
 *         - PUT
 *         - DELETE
 *       allow-credentials: true
 *       max-age: PT2H
 * ```
 */
@ConfigurationProperties(prefix = "gateway.security")
data class GatewaySecurityProperties(
    val cors: CorsProperties = CorsProperties()
)

/**
 * CORS-spezifische Konfigurationseigenschaften mit sinnvollen Defaults.
 *
 * Die Default-Werte sind so gewählt, dass sie mit typischen Entwicklungs- und Produktions-Setups funktionieren:
 * - Übliche Entwicklungs-URLs (localhost mit Standardports)
 * - Produktions-Domain-Muster
 * - Volle Unterstützung der REST-API-Methoden
 * - Unterstützung für JWT- und Gateway-Filter-Header
 * - Sinnvolle Preflight-Cache-Dauer
 */
data class CorsProperties(
    /**
     * Erlaubte Ursprünge (Allowed Origins) für CORS-Anfragen.
     *
     * Defaults unterstützen gängige Entwicklungs- und Produktionsszenarien:
     * - localhost:3000 – typischer React-Entwicklungsserver
     * - localhost:8080 – gängiger alternativer Entwicklungsport
     * - localhost:4200 – typischer Angular-Entwicklungsserver
     * - Spezifische Subdomains von meldestelle.at für die Produktion
     *
     * Kann je Umgebung bei Bedarf überschrieben werden.
     */
    val allowedOrigins: Set<String> = setOf(
        "http://localhost:3000",
        "http://localhost:8080",
        "http://localhost:4200",
        "https://app.meldestelle.at",
        "https://frontend.meldestelle.at",
        "https://www.meldestelle.at"
    ),


    /**
     * Erlaubte HTTP-Methoden für CORS-Anfragen.
     *
     * Enthält alle Standard-REST-API-Methoden sowie OPTIONS für Preflight-
     * und HEAD für Metadaten-Anfragen.
     */
    val allowedMethods: Set<String> = setOf(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
    ),

    /**
     * Erlaubte Request-Header für CORS-Anfragen.
     *
     * Beinhaltet:
     * - Standard-Header: Content-Type, Accept, etc.
     * - JWT-Authentifizierung: Authorization
     * - Gateway-Tracing: X-Correlation-ID
     * - Cache-Steuerung: Cache-Control, Pragma
     */
    val allowedHeaders: Set<String> = setOf(
        "Authorization",
        "Content-Type",
        "X-Requested-With",
        "X-Correlation-ID",
        "Accept",
        "Origin",
        "Cache-Control",
        "Pragma"
    ),

    /**
     * Sichtbare Response-Header für CORS-Anfragen.
     *
     * Header, auf die Client-JavaScript in Antworten zugreifen darf.
     * Beinhaltet benutzerdefinierte Header, die von Gateway-Filtern hinzugefügt werden:
     * - X-Correlation-ID: Request-Tracing (CorrelationIdFilter)
     * - X-RateLimit-*: Informationen zum Rate-Limiting (RateLimitingFilter)
     * - Standard-Header: Content-Length, Date
     */
    val exposedHeaders: Set<String> = setOf(
        "X-Correlation-ID",
        "X-RateLimit-Limit",
        "X-RateLimit-Remaining",
        "X-RateLimit-Enabled",
        "Content-Length",
        "Date"
    ),

    /**
     * Credentials in CORS-Anfragen erlauben.
     *
     * Auf true setzen, um zu unterstützen:
     * - JWT Bearer Tokens im Authorization-Header
     * - Cookies (falls verwendet)
     * - Client-Zertifikate (falls verwendet)
     */
    val allowCredentials: Boolean = true,

    /**
     * Maximales Alter für das Caching von Preflight-Anfragen.
     *
     * Dauer, für die Browser Preflight-Antworten cachen können, wodurch
     * die Anzahl der OPTIONS-Anfragen für wiederholte API-Aufrufe reduziert wird.
     * Default: 1 Stunde (guter Kompromiss zwischen Performance und Flexibilität)
     */
    val maxAge: Duration = Duration.ofHours(1)
)
