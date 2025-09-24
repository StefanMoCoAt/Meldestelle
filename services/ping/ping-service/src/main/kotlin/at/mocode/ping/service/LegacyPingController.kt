package at.mocode.ping.service

import at.mocode.ping.api.PingResponse
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@RestController
@CrossOrigin(
    origins = ["http://localhost:8080", "http://localhost:8083", "http://localhost:4000"],
    methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS],
    allowedHeaders = ["*"],
    allowCredentials = "true"
)
class LegacyPingController(
    private val pingService: PingServiceCircuitBreaker,
    private val circuitBreakerRegistry: CircuitBreakerRegistry
) {

    @GetMapping("/ping", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun legacySimplePing(): ResponseEntity<PingResponse> {
        val now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val resp = PingResponse(
            status = "pong",
            timestamp = now,
            service = "ping-service"
        )
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(resp)
    }

    @GetMapping("/ping/enhanced")
    fun legacyEnhanced(@RequestParam(required = false, defaultValue = "false") simulate: Boolean): Map<String, Any> {
        val dto = pingService.ping(simulate)
        val map = mutableMapOf<String, Any>(
            "status" to dto.status,
            "timestamp" to dto.timestamp,
            "service" to dto.service,
            "circuitBreaker" to dto.circuitBreakerState
        )
        if (dto.status.equals("fallback", ignoreCase = true) || dto.service.contains("fallback")) {
            map["message"] = "Service temporarily unavailable"
            map["error"] = UUID.randomUUID().toString()
        }
        return map
    }

    @GetMapping("/ping/health")
    fun legacyHealth(): Map<String, Any> {
        val dto = pingService.healthCheck()
        val state = circuitBreakerRegistry
            .circuitBreaker(PingServiceCircuitBreaker.PING_CIRCUIT_BREAKER)
            .state
        val cb = if (state.name == "OPEN") "OPEN" else "CLOSED"
        val map = mutableMapOf<String, Any>(
            "status" to if (dto.healthy) "UP" else "DOWN",
            "timestamp" to dto.timestamp,
            "circuitBreaker" to cb
        )
        if (!dto.healthy) {
            map["message"] = "Health check temporarily unavailable"
        }
        return map
    }

    @GetMapping("/ping/test-failure")
    fun legacyTestFailure(): Map<String, Any> {
        val dto = pingService.ping(simulateFailure = true)
        val map = mutableMapOf<String, Any>(
            "status" to dto.status,
            "timestamp" to dto.timestamp,
            "service" to dto.service,
            "circuitBreaker" to dto.circuitBreakerState
        )
        if (dto.status.equals("fallback", ignoreCase = true) || dto.service.contains("fallback")) {
            map["message"] = "Service temporarily unavailable"
            map["error"] = UUID.randomUUID().toString()
        }
        return map
    }
}
