package at.mocode.infrastructure.gateway.security

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

/**
 * JWT Authentication Filter für das Gateway.
 * Validiert JWT-Tokens für alle geschützten Endpunkte.
 */
@Component
@ConditionalOnProperty(value = ["gateway.security.jwt.enabled"], havingValue = "true", matchIfMissing = true)
class JwtAuthenticationFilter : GlobalFilter, Ordered {

    private val pathMatcher = AntPathMatcher()

    // Öffentliche Pfade, die keine Authentifizierung erfordern
    private val publicPaths = listOf(
        "/",
        "/health",
        "/actuator/**",
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/fallback/**",
        "/docs/**",
        "/swagger-ui/**",
        "/api/ping/**" // Ping Service für Monitoring
    )

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.value()

        // Prüfe, ob der Pfad öffentlich zugänglich ist
        if (isPublicPath(path)) {
            return chain.filter(exchange)
        }

        // Extrahiere JWT aus Authorization Header
        val authHeader = request.headers.getFirst("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange, "Missing or invalid Authorization header")
        }

        val token = authHeader.substring(7)

        // Hier würde normalerweise die JWT-Validierung mit dem auth-client erfolgen,
        // für diese Implementation verwenden wir eine vereinfachte Validierung
        return validateJwtToken(token, exchange, chain)
    }

    private fun isPublicPath(path: String): Boolean {
        return publicPaths.any { publicPath ->
            pathMatcher.match(publicPath, path)
        }
    }

    private fun validateJwtToken(
        token: String,
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void> {

        // Einfache Token-Validierung (in der Realität würde hier der auth-client verwendet)
        if (token.isEmpty() || token.length < 10) {
            return handleUnauthorized(exchange, "Invalid JWT token")
        }

        // Füge User-Informationen zu Headers hinzu (simuliert)
        val userRole = extractUserRole(token)
        val userId = extractUserId(token)

        val mutatedRequest = exchange.request.mutate()
            .header("X-User-ID", userId)
            .header("X-User-Role", userRole)
            .build()

        val mutatedExchange = exchange.mutate()
            .request(mutatedRequest)
            .build()

        return chain.filter(mutatedExchange)
    }

    private fun extractUserRole(token: String): String {
        // Vereinfachte Rollenextraktion (normalerweise aus JWT Claims)
        return when {
            token.contains("admin") -> "ADMIN"
            token.contains("user") -> "USER"
            else -> "GUEST"
        }
    }

    private fun extractUserId(token: String): String {
        // Vereinfachte User-ID Extraktion (normalerweise aus JWT Subject)
        return "user-${token.hashCode()}"
    }

    private fun handleUnauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        val response: ServerHttpResponse = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.add("Content-Type", "application/json")

        val errorJson = """{
            "error": "UNAUTHORIZED",
            "message": "$message",
            "timestamp": "${java.time.LocalDateTime.now()}",
            "status": 401
        }"""

        val buffer = response.bufferFactory().wrap(errorJson.toByteArray())
        return response.writeWith(Mono.just(buffer))
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 3
}
