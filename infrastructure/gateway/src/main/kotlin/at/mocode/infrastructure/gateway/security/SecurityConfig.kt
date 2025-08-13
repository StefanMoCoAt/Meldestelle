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
 * Enhanced reactive security configuration for the Gateway.
 *
 * ARCHITECTURE OVERVIEW:
 * =====================
 * This configuration establishes the foundational security layer for the Spring Cloud Gateway.
 * It works in conjunction with several other security components:
 *
 * 1. JwtAuthenticationFilter (GlobalFilter) - Handles JWT token validation and user authentication
 * 2. RateLimitingFilter (GlobalFilter) - Provides IP-based rate limiting with user-aware limits
 * 3. CorrelationIdFilter (GlobalFilter) - Adds request tracing capabilities
 * 4. EnhancedLoggingFilter (GlobalFilter) - Provides structured request/response logging
 *
 * SECURITY STRATEGY:
 * ==================
 * The Gateway employs a layered security approach:
 * - This SecurityWebFilterChain provides foundational settings (CORS, CSRF, basic headers)
 * - JwtAuthenticationFilter handles actual authentication when enabled via property
 * - The SecurityWebFilterChain remains permissive (permitAll) to let the JWT filter control access
 * - Rate limiting and logging filters provide operational security and monitoring
 *
 * DESIGN RATIONALE:
 * =================
 * - During tests, Spring Security is on the classpath (testImplementation), which enables
 *   security autoconfiguration and can lock down all endpoints unless a SecurityWebFilterChain is provided
 * - The Gateway enforces authentication using JwtAuthenticationFilter when enabled via property,
 *   so the SecurityWebFilterChain should stay permissive and focus on foundational concerns
 * - Explicit CORS configuration ensures proper handling of cross-origin requests from web clients
 * - Configurable properties allow environment-specific security settings without code changes
 * - CSRF protection is disabled as it's not needed for stateless JWT-based authentication
 *
 * CORS INTEGRATION:
 * =================
 * The CORS configuration works with the existing filter chain:
 * - Allows requests from configured origins (dev/prod environments)
 * - Exposes custom headers from Gateway filters (correlation IDs, rate limits)
 * - Supports credentials for JWT authentication
 * - Caches preflight responses for performance
 *
 * TESTING CONSIDERATIONS:
 * =======================
 * - Configuration is designed to work seamlessly with existing security tests
 * - Test profile can override CORS settings if needed
 * - Permissive authorization ensures tests can focus on filter-level security
 */
@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties::class)
class SecurityConfig(
    private val securityProperties: GatewaySecurityProperties
) {

    /**
     * Main Spring Security filter chain configuration.
     *
     * This method configures the reactive security filter chain with:
     * - CSRF disabled for stateless API operation
     * - Explicit CORS configuration for cross-origin support
     * - Permissive authorization (authentication handled by JWT filter)
     *
     * The configuration maintains compatibility with the existing filter architecture
     * while providing enhanced CORS control and configurability.
     */
    @Bean
    fun springSecurityFilterChain(): SecurityWebFilterChain {
        return ServerHttpSecurity.http()
            .csrf { csrf ->
                // Disable CSRF for stateless API gateway
                // CSRF protection is not required for JWT-based stateless authentication
                // The Gateway operates as a stateless proxy with no session state
                csrf.disable()
            }
            .cors { cors ->
                // Use explicit CORS configuration instead of default
                // This provides better control over cross-origin access policies
                cors.configurationSource(corsConfigurationSource())
            }
            .httpBasic { basic ->
                // Disable HTTP Basic auth for stateless API
                basic.disable()
            }
            .formLogin { form ->
                // Disable form login for API gateway
                form.disable()
            }
            .authorizeExchange { exchanges ->
                // Allow all requests through Spring Security
                // Authentication and authorization are handled by JwtAuthenticationFilter
                // This approach maintains the existing security architecture while
                // allowing the JWT filter to make granular access control decisions
                exchanges.anyExchange().permitAll()
            }
            .build()
    }

    /**
     * Explicit CORS configuration source.
     *
     * This bean provides detailed control over cross-origin resource sharing settings,
     * replacing the default empty CORS configuration with explicit, configurable settings.
     *
     * Key features:
     * - Environment-specific allowed origins
     * - Comprehensive HTTP method support
     * - JWT-aware header configuration
     * - Integration with Gateway filter headers
     * - Performance-optimized preflight caching
     *
     * The configuration is designed to work with typical web application architectures
     * where a JavaScript frontend makes API calls to the Gateway.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // Allowed origins - configurable per environment
            // Development: localhost URLs for local testing
            // Production: domain-specific URLs for deployed applications
            allowedOrigins = securityProperties.cors.allowedOrigins.toList()


            // Allowed HTTP methods - comprehensive REST API support
            // Includes all standard methods plus OPTIONS for preflight requests
            allowedMethods = securityProperties.cors.allowedMethods.toList()

            // Allowed request headers - includes JWT and custom headers
            // Authorization: for JWT Bearer tokens
            // X-Correlation-ID: for request tracing
            // Standard headers: Content-Type, Accept, etc.
            allowedHeaders = securityProperties.cors.allowedHeaders.toList()

            // Exposed response headers - allows client access to custom headers
            // Includes headers added by Gateway filters:
            // - X-Correlation-ID from CorrelationIdFilter
            // - X-RateLimit-* from RateLimitingFilter
            exposedHeaders = securityProperties.cors.exposedHeaders.toList()

            // Allow credentials - required for JWT authentication
            // Enables cookies and authorization headers in cross-origin requests
            allowCredentials = securityProperties.cors.allowCredentials

            // Preflight cache duration - performance optimization
            // Reduces the number of OPTIONS requests for repeated API calls
            maxAge = securityProperties.cors.maxAge.seconds
        }

        return UrlBasedCorsConfigurationSource().apply {
            // Apply CORS configuration to all Gateway routes
            registerCorsConfiguration("/**", configuration)
        }
    }
}

/**
 * Configuration properties for Gateway security settings.
 *
 * Enables environment-specific security configuration via application.yml/properties.
 * This approach allows different security settings across development, testing, and
 * production environments without requiring code changes.
 *
 * Example application.yml configuration:
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
 * CORS-specific configuration properties with sensible defaults.
 *
 * Default values are chosen to work with typical development and production setups:
 * - Common development URLs (localhost with standard ports)
 * - Production domain pattern matching
 * - Full REST API method support
 * - JWT and Gateway filter header support
 * - Reasonable preflight cache duration
 */
data class CorsProperties(
    /**
     * Allowed origins for CORS requests.
     *
     * Defaults support common development and production scenarios:
     * - localhost:3000 - typical React development server
     * - localhost:8080 - common alternative development port
     * - localhost:4200 - typical Angular development server
     * - Specific meldestelle.at subdomains for production
     *
     * Can be overridden per environment as needed.
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
     * Allowed HTTP methods for CORS requests.
     *
     * Includes all standard REST API methods plus OPTIONS for preflight
     * and HEAD for metadata requests.
     */
    val allowedMethods: Set<String> = setOf(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
    ),

    /**
     * Allowed request headers for CORS requests.
     *
     * Includes:
     * - Standard headers: Content-Type, Accept, etc.
     * - JWT authentication: Authorization
     * - Gateway tracing: X-Correlation-ID
     * - Cache control: Cache-Control, Pragma
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
     * Exposed response headers for CORS requests.
     *
     * Headers that client JavaScript can access in responses.
     * Includes custom headers added by Gateway filters:
     * - X-Correlation-ID: request tracing (CorrelationIdFilter)
     * - X-RateLimit-*: rate limiting info (RateLimitingFilter)
     * - Standard headers: Content-Length, Date
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
     * Allow credentials in CORS requests.
     *
     * Set to true to support:
     * - JWT Bearer tokens in Authorization headers
     * - Cookies (if used)
     * - Client certificates (if used)
     */
    val allowCredentials: Boolean = true,

    /**
     * Maximum age for preflight request caching.
     *
     * Duration that browsers can cache preflight responses, reducing
     * the number of OPTIONS requests for repeated API calls.
     * Default: 1 hour (reasonable balance of performance vs. flexibility)
     */
    val maxAge: Duration = Duration.ofHours(1)
)
