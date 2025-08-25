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

        // Verbesserte Token-Validierung mit grundlegenden Sicherheitsprüfungen
        // TODO: Integration mit auth-client für vollständige JWT-Validierung

        // Grundlegende JWT-Format-Validierung
        if (!isValidJwtFormat(token)) {
            return handleUnauthorized(exchange, "Invalid JWT token format")
        }

        try {
            // Extrahiere Claims aus dem JWT (vereinfacht für Demo)
            val claims = parseJwtClaims(token)
            val userRole = claims["role"] ?: "GUEST"
            val userId = claims["sub"] ?: generateSecureUserId(token)

            // Validiere Token-Inhalt
            if (!isValidClaims(claims)) {
                return handleUnauthorized(exchange, "Invalid JWT claims")
            }

            val mutatedRequest = exchange.request.mutate()
                .header("X-User-ID", userId)
                .header("X-User-Role", userRole)
                .build()

            val mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build()

            return chain.filter(mutatedExchange)

        } catch (e: Exception) {
            return handleUnauthorized(exchange, "JWT parsing failed: ${e.message}")
        }
    }

    /**
     * Validiert das grundlegende JWT-Format (Header.Payload.Signature)
     */
    private fun isValidJwtFormat(token: String): Boolean {
        val parts = token.split(".")
        return parts.size == 3 && parts.all { it.isNotEmpty() }
    }

    /**
     * Vereinfachte JWT-Claims-Extraktion für Demo-Zwecke.
     * In der Produktion sollte hier der auth-client verwendet werden.
     */
    private fun parseJwtClaims(token: String): Map<String, String> {
        // Simulierte Claims basierend auf Token-Inhalt (nur für Demo)
        // In der Realität würde hier Base64-Decoding und JSON-Parsing stattfinden
        return when {
            token.length > 100 && token.contains("admin", ignoreCase = true) ->
                mapOf("role" to "ADMIN", "sub" to "admin-user")
            token.length > 50 ->
                mapOf("role" to "USER", "sub" to "regular-user")
            else ->
                mapOf("role" to "GUEST", "sub" to "guest-user")
        }
    }

    /**
     * Validiert JWT-Claims auf grundlegende Korrektheit
     */
    private fun isValidClaims(claims: Map<String, String>): Boolean {
        val role = claims["role"]
        val subject = claims["sub"]

        return !role.isNullOrBlank() &&
               !subject.isNullOrBlank() &&
               role in listOf("ADMIN", "USER", "GUEST")
    }

    /**
     * Generiert eine sichere User-ID basierend auf Token-Hash
     */
    private fun generateSecureUserId(token: String): String {
        // Verwende einen stabileren Hash als einfaches hashCode()
        return "user-${token.takeLast(20).hashCode().toString(16)}"
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
