package at.mocode.temp.pingservice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController(
    private val pingServiceCircuitBreaker: PingServiceCircuitBreaker
) {

    /**
     * Standard ping endpoint - maintains backward compatibility
     */
    @GetMapping("/ping")
    fun ping(): Map<String, String> {
        return mapOf("status" to "pong")
    }

    /**
     * Enhanced ping endpoint with circuit breaker protection
     *
     * @param simulate - whether to simulate failures for testing circuit breaker
     */
    @GetMapping("/ping/enhanced")
    fun enhancedPing(@RequestParam(defaultValue = "false") simulate: Boolean): Map<String, Any> {
        return pingServiceCircuitBreaker.ping(simulate)
    }

    /**
     * Health check endpoint with circuit breaker protection
     */
    @GetMapping("/ping/health")
    fun health(): Map<String, Any> {
        return pingServiceCircuitBreaker.healthCheck()
    }

    /**
     * Endpoint to test circuit breaker behavior by forcing failures
     */
    @GetMapping("/ping/test-failure")
    fun testFailure(): Map<String, Any> {
        return pingServiceCircuitBreaker.ping(simulateFailure = true)
    }
}
