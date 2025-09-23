package at.mocode.temp.pingservice

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RestController
@CrossOrigin(
    origins = ["http://localhost:8080", "http://localhost:8083", "http://localhost:4000"],
    methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS],
    allowedHeaders = ["*"],
    allowCredentials = "true"
)
class PingController(
    private val pingService: PingServiceCircuitBreaker
) {

    /**
     * Standard ping endpoint - maintains backward compatibility
     * NOW HANDLES BOTH /ping AND /ping/ping paths for Gateway compatibility
     */
    @GetMapping("/ping", "/ping/ping")
    fun ping(): Map<String, String> {
        val now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return mapOf(
            "status" to "pong",
            "timestamp" to now,
            "service" to "ping-service"
        )
    }

    /**
     * Enhanced ping endpoint with circuit breaker protection
     */
    @GetMapping("/ping/enhanced")
    fun enhancedPing(@RequestParam(defaultValue = "false") simulate: Boolean): Map<String, Any> {
        // Delegate to service with circuit breaker
        return pingService.ping(simulateFailure = simulate)
    }

    /**
     * Health check endpoint with circuit breaker protection
     */
    @GetMapping("/ping/health")
    fun health(): Map<String, Any> {
        return pingService.healthCheck()
    }

    /**
     * Endpoint to test circuit breaker behavior by forcing failures
     * Uses simulate=true to increase chance of fallback
     */
    @GetMapping("/ping/test-failure")
    fun testFailure(): Map<String, Any> {
        return try {
            pingService.ping(simulateFailure = true)
        } catch (ex: Exception) {
            // Although CircuitBreaker should handle it, ensure safe fallback
            pingService.fallbackPing(simulateFailure = true, exception = ex)
        }
    }
}
