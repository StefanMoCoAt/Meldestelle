package at.mocode.ping.infrastructure.web

import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingEvent
import at.mocode.ping.api.PingResponse
import at.mocode.ping.application.PingUseCase
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi

/**
 * Driving Adapter (REST Controller).
 * Nutzt den Application Port (PingUseCase).
 */
@RestController
// Spring requires using `originPatterns` (not wildcard `origins`) when credentials are enabled.
@CrossOrigin(allowedHeaders = ["*"], allowCredentials = "true", originPatterns = ["*"])
@OptIn(ExperimentalUuidApi::class)
class PingController(
    private val pingUseCase: PingUseCase
) : PingApi {

    private val logger = LoggerFactory.getLogger(PingController::class.java)
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    companion object {
        const val PING_CIRCUIT_BREAKER = "pingCircuitBreaker"
    }

    @GetMapping("/ping/simple")
    override suspend fun simplePing(): PingResponse {
        val domainPing = pingUseCase.executePing("Simple Ping")
        return createResponse(domainPing, "pong")
    }

    @GetMapping("/ping/enhanced")
    @CircuitBreaker(name = PING_CIRCUIT_BREAKER, fallbackMethod = "fallbackPing")
    override suspend fun enhancedPing(
        @RequestParam(required = false, defaultValue = "false") simulate: Boolean
    ): EnhancedPingResponse {
        val start = System.nanoTime()

        if (simulate && Random.nextDouble() < 0.6) {
            throw RuntimeException("Simulated service failure")
        }

        val domainPing = pingUseCase.executePing("Enhanced Ping")
        val elapsedMs = (System.nanoTime() - start) / 1_000_000

        return EnhancedPingResponse(
            status = "pong",
            timestamp = domainPing.timestamp.atOffset(ZoneOffset.UTC).format(formatter),
            service = "ping-service",
            circuitBreakerState = "CLOSED",
            responseTime = elapsedMs
        )
    }

    // Neue Endpunkte

    @GetMapping("/ping/public")
    override suspend fun publicPing(): PingResponse {
        val domainPing = pingUseCase.executePing("Public Ping")
        return createResponse(domainPing, "public-pong")
    }

    @GetMapping("/ping/secure")
    @PreAuthorize("hasRole('MELD_USER') or hasRole('MELD_ADMIN')") // Beispiel-Rollen
    override suspend fun securePing(): PingResponse {
        val domainPing = pingUseCase.executePing("Secure Ping")
        return createResponse(domainPing, "secure-pong")
    }

    @GetMapping("/ping/sync")
    override suspend fun syncPings(
        // Changed the parameter name to 'since' to match SyncManager convention
        @RequestParam(required = false, defaultValue = "0") since: Long
    ): List<PingEvent> {
        return pingUseCase.getPingsSince(since).map {
            PingEvent(
                id = it.id.toString(),
                message = it.message,
                lastModified = it.timestamp.toEpochMilli()
            )
        }
    }

    // Helper
    private fun createResponse(domainPing: at.mocode.ping.domain.Ping, status: String) = PingResponse(
        status = status,
        timestamp = domainPing.timestamp.atOffset(ZoneOffset.UTC).format(formatter),
        service = "ping-service"
    )

    // Fallback
    @Suppress("unused", "UNUSED_PARAMETER")
    fun fallbackPing(simulate: Boolean, ex: Exception): EnhancedPingResponse {
        logger.warn("Circuit breaker fallback triggered: {}", ex.message)
        return EnhancedPingResponse(
            status = "fallback",
            timestamp = java.time.OffsetDateTime.now().format(formatter),
            service = "ping-service-fallback",
            circuitBreakerState = "OPEN",
            responseTime = 0
        )
    }

    @GetMapping("/ping/health")
    override suspend fun healthCheck(): HealthResponse {
        return HealthResponse(
            status = "up",
            timestamp = java.time.OffsetDateTime.now().format(formatter),
            service = "ping-service",
            healthy = true
        )
    }

    @GetMapping("/ping/history")
    fun getHistory() = pingUseCase.getPingHistory().map {
        mapOf("id" to it.id.toString(), "message" to it.message, "time" to it.timestamp.toString())
    }
}
