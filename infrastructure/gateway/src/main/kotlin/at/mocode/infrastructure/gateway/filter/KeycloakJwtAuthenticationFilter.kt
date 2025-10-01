package at.mocode.infrastructure.gateway.filter

import at.mocode.infrastructure.auth.client.JwtService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.Base64

@Component
@ConditionalOnProperty(
    value = ["gateway.security.keycloak.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class KeycloakJwtAuthenticationFilter(
    private val jwtService: JwtService
) : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(KeycloakJwtAuthenticationFilter::class.java)
    private val pathMatcher = AntPathMatcher()
    private val objectMapper = jacksonObjectMapper()

    @Value("\${keycloak.realm:meldestelle}")
    private lateinit var realm: String

    @Value("\${keycloak.issuer-uri:http://keycloak:8080/realms/meldestelle}")
    private lateinit var issuerUri: String

    // Öffentliche Pfade aus Konfiguration
    @Value("\${gateway.security.public-paths:/,/health/**,/actuator/**,/api/ping/**,/api/auth/**,/fallback/**,/docs/**,/swagger-ui/**}")
    private lateinit var publicPathsConfig: String

    private val publicPaths by lazy {
        publicPathsConfig.split(",").map { it.trim() }
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()

        logger.debug("Processing request for path: {}", path)

        // Prüfe öffentliche Pfade
        if (isPublicPath(path)) {
            logger.debug("Path {} is public, allowing without authentication", path)
            return chain.filter(exchange)
        }

        // JWT Token extrahieren
        val authHeader = request.headers.getFirst("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path)
            return handleUnauthorized(exchange, "Missing or invalid Authorization header")
        }

        val token = authHeader.substring(7)
        return validateToken(token, exchange, chain)
    }

    private fun validateToken(
        token: String,
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void> {
        return try {
            // Verwende JwtService für Validierung
            val validationResult = jwtService.validateToken(token)
            if (validationResult.isFailure) {
                logger.warn("JWT validation failed: {}", validationResult.exceptionOrNull()?.message)
                return handleUnauthorized(exchange, "Invalid JWT token")
            }

            // Claims extrahieren
            val claims = parseJwtClaims(token)

            // Issuer validieren
            val issuer = claims["iss"]?.toString()
            if (!issuer.equals(issuerUri)) {
                logger.warn("Invalid issuer in token: {} (expected: {})", issuer, issuerUri)
                return handleUnauthorized(exchange, "Invalid token issuer")
            }

            // User-Informationen extrahieren
            val userId = claims["sub"]?.toString() ?: "unknown"
            val username = claims["preferred_username"]?.toString()
                ?: claims["name"]?.toString()
                ?: "unknown"
            val email = claims["email"]?.toString() ?: ""
            val roles = extractRoles(claims)
            val userRole = determineUserRole(roles)

            logger.debug("Token validated for user: {} (ID: {}) with roles: {}", username, userId, roles)

            // Request mit User-Context erweitern
            val mutatedRequest = exchange.request.mutate()
                .header("X-User-ID", userId)
                .header("X-User-Name", username)
                .header("X-User-Email", email)
                .header("X-User-Role", userRole)
                .header("X-User-Roles", roles.joinToString(","))
                .header("X-Auth-Method", "keycloak-jwt")
                .build()

            val mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build()

            chain.filter(mutatedExchange)

        } catch (e: Exception) {
            logger.error("JWT validation failed unexpectedly: {}", e.message, e)
            handleUnauthorized(exchange, "JWT validation failed")
        }
    }

    private fun isPublicPath(path: String): Boolean {
        return publicPaths.any { publicPath ->
            pathMatcher.match(publicPath, path)
        }
    }

    private fun parseJwtClaims(token: String): Map<String, Any> {
        val parts = token.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT format")
        }

        val payload = parts[1]
        val decoded = Base64.getUrlDecoder().decode(payload)
        return objectMapper.readValue(decoded, Map::class.java) as Map<String, Any>
    }

    private fun extractRoles(claims: Map<String, Any>): List<String> {
        return try {
            // Keycloak realm roles
            @Suppress("UNCHECKED_CAST")
            val realmAccess = claims["realm_access"] as? Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val realmRoles = realmAccess?.get("roles") as? List<String> ?: emptyList()

            // Keycloak resource access (client-specific roles)
            @Suppress("UNCHECKED_CAST")
            val resourceAccess = claims["resource_access"] as? Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val clientAccess = resourceAccess?.get("api-gateway") as? Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val clientRoles = clientAccess?.get("roles") as? List<String> ?: emptyList()

            (realmRoles + clientRoles).distinct()
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
        response.headers.add("WWW-Authenticate", "Bearer realm=\"$realm\"")

        val errorResponse = mapOf(
            "error" to "UNAUTHORIZED",
            "message" to message,
            "timestamp" to java.time.Instant.now().toString(),
            "status" to 401,
            "realm" to realm,
            "path" to exchange.request.path.value()
        )

        val errorJson = objectMapper.writeValueAsString(errorResponse)
        val buffer = response.bufferFactory().wrap(errorJson.toByteArray())
        return response.writeWith(Mono.just(buffer))
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 3
}
