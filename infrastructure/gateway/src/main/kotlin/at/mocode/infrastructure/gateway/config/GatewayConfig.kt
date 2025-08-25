package at.mocode.infrastructure.gateway.config

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Gateway-Konfiguration für erweiterte Funktionalitäten wie Logging, Rate Limiting und Security.
 */

/**
 * Global Filter für Korrelations-IDs zur Request-Verfolgung.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
class CorrelationIdFilter : GlobalFilter, Ordered {

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-ID"
    }

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val correlationId = request.headers.getFirst(CORRELATION_ID_HEADER)
            ?: UUID.randomUUID().toString()

        val mutatedRequest = request.mutate()
            .header(CORRELATION_ID_HEADER, correlationId)
            .build()

        val mutatedExchange = exchange.mutate()
            .request(mutatedRequest)
            .build()

        // Add a response header after processing
        mutatedExchange.response.headers.add(CORRELATION_ID_HEADER, correlationId)

        return chain.filter(mutatedExchange)
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}

/**
 * Enhanced Logging Filter für strukturiertes Logging mit Request/Response Details.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
class EnhancedLoggingFilter : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(EnhancedLoggingFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()
        val request = exchange.request
        val correlationId = request.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER)

        logRequest(request, correlationId)

        return chain.filter(exchange)
            .doOnSuccess {
                val responseTime = System.currentTimeMillis() - startTime
                logResponse(exchange.response, correlationId, responseTime)
            }
            .doOnError { error ->
                val responseTime = System.currentTimeMillis() - startTime
                logError(error, correlationId, responseTime)
            }
    }

    private fun logRequest(request: ServerHttpRequest, correlationId: String?) {
        logger.info("""
            [REQUEST] [{}]
            Method: {}
            URI: {}
            RemoteAddress: {}
            UserAgent: {}
        """.trimIndent(),
            correlationId,
            request.method,
            request.uri,
            request.remoteAddress,
            request.headers.getFirst("User-Agent")
        )
    }

    private fun logResponse(response: ServerHttpResponse, correlationId: String?, responseTime: Long) {
        logger.info("""
            [RESPONSE] [{}]
            Status: {}
            ResponseTime: {}ms
        """.trimIndent(),
            correlationId,
            response.statusCode,
            responseTime
        )
    }

    private fun logError(error: Throwable, correlationId: String?, responseTime: Long) {
        logger.error("""
            [ERROR] [{}]
            Error: {}
            ResponseTime: {}ms
        """.trimIndent(),
            correlationId,
            error.message,
            responseTime,
            error
        )
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1
}

/**
 * Rate Limiting Filter basierend auf IP-Adresse und User-Typ.
 *
 * Optimierungen:
 * - Memory-Leak-Schutz durch regelmäßige Bereinigung alter Einträge
 * - Sichere Rollenvalidierung basierend auf JWT-Authentifizierung
 * - Bessere Verteilung der Rate-Limits basierend auf Benutzerrollen
 */
@Component
@org.springframework.context.annotation.Profile("!test")
class RateLimitingFilter : GlobalFilter, Ordered {

    private val requestCounts = ConcurrentHashMap<String, RequestCounter>()
    private val logger = org.slf4j.LoggerFactory.getLogger(RateLimitingFilter::class.java)

    // Timestamp der letzten Bereinigung
    @Volatile
    private var lastCleanup = System.currentTimeMillis()

    companion object {
        const val RATE_LIMIT_ENABLED_HEADER = "X-RateLimit-Enabled"
        const val RATE_LIMIT_LIMIT_HEADER = "X-RateLimit-Limit"
        const val RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining"

        // Rate Limits pro Minute
        const val ANONYMOUS_LIMIT = 50
        const val AUTHENTICATED_LIMIT = 200
        const val ADMIN_LIMIT = 500
        const val AUTH_ENDPOINT_LIMIT = 20
        const val DEFAULT_LIMIT = 100

        // Bereinigungsintervall: alle 5 Minuten
        const val CLEANUP_INTERVAL_MS = 5 * 60 * 1000L
        // Einträge, die älter als 10 Minuten sind, werden entfernt
        const val ENTRY_MAX_AGE_MS = 10 * 60 * 1000L
    }

    data class RequestCounter(
        var count: Int = 0,
        var lastReset: Long = System.currentTimeMillis()
    )

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response
        val clientIp = getClientIp(request)
        val path = request.path.value()

        // Periodische Bereinigung des Caches zur Vermeidung von Memory Leaks
        performPeriodicCleanup()

        val limit = determineRateLimit(request, path)
        val counter = requestCounts.computeIfAbsent(clientIp) { RequestCounter() }

        // Reset counter if more than a minute has passed
        val now = System.currentTimeMillis()
        if (now - counter.lastReset > 60_000) {
            counter.count = 0
            counter.lastReset = now
        }

        counter.count++

        // Add rate limit headers
        response.headers.add(RATE_LIMIT_ENABLED_HEADER, "true")
        response.headers.add(RATE_LIMIT_LIMIT_HEADER, limit.toString())
        response.headers.add(RATE_LIMIT_REMAINING_HEADER, maxOf(0, limit - counter.count).toString())

        return if (counter.count > limit) {
            response.statusCode = HttpStatus.TOO_MANY_REQUESTS
            response.setComplete()
        } else {
            chain.filter(exchange)
        }
    }

    private fun getClientIp(request: ServerHttpRequest): String {
        return request.headers.getFirst("X-Forwarded-For")?.split(",")?.first()?.trim()
            ?: request.headers.getFirst("X-Real-IP")
            ?: request.remoteAddress?.address?.hostAddress
            ?: "unknown"
    }

    private fun determineRateLimit(request: ServerHttpRequest, path: String): Int {
        return when {
            path.startsWith("/api/auth") -> AUTH_ENDPOINT_LIMIT
            isAdminUser(request) -> ADMIN_LIMIT
            isAuthenticatedUser(request) -> AUTHENTICATED_LIMIT
            else -> ANONYMOUS_LIMIT
        }
    }

    private fun isAuthenticatedUser(request: ServerHttpRequest): Boolean {
        return request.headers.getFirst("Authorization") != null
    }

    private fun isAdminUser(request: ServerHttpRequest): Boolean {
        // Sichere Rollenvalidierung basierend auf JWT-Authentifizierung
        // Die X-User-Role wird vom JwtAuthenticationFilter nach erfolgreicher JWT-Validierung gesetzt
        val userRole = request.headers.getFirst("X-User-Role")
        val userId = request.headers.getFirst("X-User-ID")

        // Zusätzliche Sicherheitsprüfung: Beide Header müssen vorhanden sein
        // Dies reduziert die Wahrscheinlichkeit von Header-Spoofing
        return userRole == "ADMIN" && userId != null
    }

    /**
     * Bereinigt alte Einträge aus dem requestCounts Cache zur Vermeidung von Memory Leaks.
     * Wird nur alle CLEANUP_INTERVAL_MS ausgeführt für bessere Performance.
     */
    private fun performPeriodicCleanup() {
        val now = System.currentTimeMillis()
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            val sizeBefore = requestCounts.size
            val cutoffTime = now - ENTRY_MAX_AGE_MS

            // Entferne alle Einträge, die älter als ENTRY_MAX_AGE_MS sind
            requestCounts.entries.removeIf { (_, counter) ->
                counter.lastReset < cutoffTime
            }

            lastCleanup = now
            val sizeAfter = requestCounts.size

            if (sizeBefore > sizeAfter) {
                logger.debug("Rate limit cache cleanup: removed {} old entries, {} entries remaining",
                    sizeBefore - sizeAfter, sizeAfter)
            }
        }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 2
}
