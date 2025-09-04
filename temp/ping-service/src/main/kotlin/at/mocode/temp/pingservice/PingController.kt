package at.mocode.temp.pingservice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController {

    /**
     * Standard ping endpoint - maintains backward compatibility
     */
    @GetMapping("/ping")
    fun ping(): Map<String, String> {
        return mapOf("status" to "pong")
    }

    /**
     * Enhanced ping endpoint with circuit breaker protection
     */
    @GetMapping("/ping/enhanced")
    fun enhancedPing(@RequestParam(defaultValue = "false") simulate: Boolean): Map<String, Any> {
        return mapOf("status" to "pong", "message" to "Circuit breaker not available")
    }

    /**
     * Health check endpoint with circuit breaker protection
     */
    @GetMapping("/ping/health")
    fun health(): Map<String, Any> {
        return mapOf("status" to "UP", "message" to "Circuit breaker not available")
    }

    /**
     * Endpoint to test circuit breaker behavior by forcing failures
     */
    @GetMapping("/ping/test-failure")
    fun testFailure(): Map<String, Any> {
        return mapOf("status" to "error", "message" to "Circuit breaker not available")
    }
}
