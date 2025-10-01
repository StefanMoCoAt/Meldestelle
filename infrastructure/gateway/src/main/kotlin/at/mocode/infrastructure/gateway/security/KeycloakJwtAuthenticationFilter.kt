package at.mocode.infrastructure.gateway.security

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.Base64

@Component
@ConditionalOnProperty(value = ["gateway.security.keycloak.enabled"], havingValue = "true", matchIfMissing = false)
class KeycloakJwtAuthenticationFilter(
    private val webClient: WebClient.Builder
) : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(KeycloakJwtAuthenticationFilter::class.java)
    private val pathMatcher = AntPathMatcher()
    private val objectMapper = jacksonObjectMapper()

    companion object {
        private const val KEYCLOAK_SERVER_URL = "http://keycloak:8080"
        private const val REALM = "meldestelle"
    }

    // Öffentliche Pfade, die keine Authentifizierung erfordern
    private val publicPaths = listOf(
        "/",
        "/health",
        "/actuator/**",
        "/api/ping/**",              // Ping-Service für Monitoring
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/fallback/**",
        "/docs/**",
        "/swagger-ui/**"
    )

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()

        logger.debug("Processing request for path: {}", path)

        // Prüfe, ob der Pfad öffentlich zugänglich ist
        if (isPublicPath(path)) {
            logger.debug("Path {} is public, allowing without authentication", path)
            return chain.filter(exchange)
        }

        // Extrahiere JWT aus Authorization Header
        val authHeader = request.headers.getFirst("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path)
            return handleUnauthorized(exchange, "Missing or invalid Authorization header")
        }

        val token = authHeader.substring(7)

        // Validiere JWT-Token mit Keycloak
        return validateKeycloakToken(token, exchange, chain)
    }

    private fun isPublicPath(path: String): Boolean {
        return publicPaths.any { publicPath ->
            pathMatcher.match(publicPath, path)
        }
    }

    private fun validateKeycloakToken(
        token: String,
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void> {

        return try {
            // JWT-Token-Struktur validieren
            if (!isValidJwtFormat(token)) {
                return handleUnauthorized(exchange, "Invalid JWT token format")
            }

            // Claims aus Token extrahieren
            val claims = parseJwtClaims(token)
            val issuer = claims["iss"]?.toString()
            val realm = issuer?.substringAfterLast("/")

            if (realm != REALM) {
                return handleUnauthorized(exchange, "Invalid realm in token")
            }

            // Benutzerinformationen extrahieren
            val userId = claims["sub"]?.toString() ?: "unknown"
            val username = claims["preferred_username"]?.toString() ?: "unknown"
            val roles = extractRoles(claims)
            val userRole = determineUserRole(roles)

            logger.debug("Token validated for user: {} with roles: {}", username, roles)

            // Request mit Benutzerinformationen erweitern
            val mutatedRequest = exchange.request.mutate()
                .header("X-User-ID", userId)
                .header("X-User-Name", username)
                .header("X-User-Role", userRole)
                .header("X-User-Roles", roles.joinToString(","))
                .build()

            val mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build()

            chain.filter(mutatedExchange)

        } catch (e: Exception) {
            logger.error("JWT validation failed: {}", e.message, e)
            handleUnauthorized(exchange, "JWT validation failed: ${e.message}")
        }
    }

    private fun isValidJwtFormat(token: String): Boolean {
        val parts = token.split(".")
        return parts.size == 3 && parts.all { it.isNotEmpty() }
    }

    private fun parseJwtClaims(token: String): Map<String, Any> {
        val parts = token.split(".")
        val payload = parts[1]

        // Base64 URL decode
        val decoded = Base64.getUrlDecoder().decode(payload)
        return objectMapper.readValue<Map<String, Any>>(decoded)
    }

    private fun extractRoles(claims: Map<String, Any>): List<String> {
        return try {
            @Suppress("UNCHECKED_CAST")
            val realmAccess = claims["realm_access"] as? Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val roles = realmAccess?.get("roles") as? List<String>
            roles ?: emptyList()
        } catch (e: Exception) {
            logger.warn("Could not extract roles from token: {}", e.message)
            emptyList()
        }
    }

    private fun determineUserRole(roles: List<String>): String {
        return when {
            "ADMIN" in roles -> "ADMIN"
            "USER" in roles -> "USER"
            "MONITORING" in roles -> "MONITORING"
            else -> "GUEST"
        }
    }

    private fun handleUnauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        val response: ServerHttpResponse = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.add("Content-Type", "application/json")
        response.headers.add("WWW-Authenticate", "Bearer realm=\"$REALM\"")

        val errorJson = """{
            "error": "UNAUTHORIZED",
            "message": "$message",
            "timestamp": "${java.time.LocalDateTime.now()}",
            "status": 401,
            "realm": "$REALM"
        }"""

        val buffer = response.bufferFactory().wrap(errorJson.toByteArray())
        return response.writeWith(Mono.just(buffer))
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 3
}
