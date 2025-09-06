package at.mocode.temp.pingservice

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * Service demonstrating a Circuit Breaker pattern with Resilience
 *
 * This service simulates potential failures and uses circuit breaker
 * to handle service degradation gracefully with fallback responses.
 */
@Service
class PingServiceCircuitBreaker {

    private val logger = LoggerFactory.getLogger(PingServiceCircuitBreaker::class.java)

    companion object {
        const val PING_CIRCUIT_BREAKER = "pingCircuitBreaker"
        private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME //.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    /**
     * Primary ping method with circuit breaker protection
     *
     * @param simulateFailure - if true, randomly throws exceptions to test circuit breaker
     * @return Map containing ping response with timestamp and status
     */
    @CircuitBreaker(name = PING_CIRCUIT_BREAKER, fallbackMethod = "fallbackPing")
    fun ping(simulateFailure: Boolean = false): Map<String, Any> {
        logger.info("Executing ping service call...")

        if (simulateFailure && Random.nextDouble() < 0.6) {
            logger.warn("Simulating service failure for circuit breaker testing")
            throw RuntimeException("Simulated service failure")
        }

        val currentTime = LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC).format(formatter)
        logger.info("Ping service call successful")

        return mapOf(
            "status" to "pong",
            "timestamp" to currentTime,
            "service" to "ping-service",
            "circuitBreaker" to "CLOSED"
        )
    }

    /**
     * Fallback method called when circuit breaker is OPEN
     *
     * @param simulateFailure - original parameter (ignored in fallback)
     * @param exception - the exception that triggered the fallback
     * @return Map containing fallback response
     */
    fun fallbackPing(simulateFailure: Boolean = false, exception: Exception): Map<String, Any> {
        // Die volle Exception nur loggen, nicht an den Client weitergeben.
        logger.warn("Circuit breaker fallback triggered due to: {}", exception.toString())

        val currentTime = LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC).format(formatter)
        val correlatedId = java.util.UUID.randomUUID().toString()

        return mapOf(
            "status" to "fallback",
            "message" to "Service temporarily unavailable",
            "timestamp" to currentTime,
            "service" to "ping-service-fallback",
            "circuitBreaker" to "OPEN",
            "error" to correlatedId // Diese ID kann für Support-Anfragen genutzt werden.
        )
    }

    /**
     * Health check method with circuit breaker protection
     */
    @CircuitBreaker(name = PING_CIRCUIT_BREAKER, fallbackMethod = "fallbackHealth")
    fun healthCheck(): Map<String, Any> {
        logger.info("Executing health check...")

        // Health check is now deterministic for reliable integration testing
        // Random failures were causing intermittent test failures

        return mapOf(
            "status" to "UP",
            "timestamp" to LocalDateTime.now().format(formatter),
            "circuitBreaker" to "CLOSED"
        )
    }

    /**
     * Fallback for health check
     */
    fun fallbackHealth(exception: Exception): Map<String, Any> {
        logger.warn("Health check fallback triggered: {}", exception.message)

        return mapOf(
            "status" to "DOWN",
            "message" to "Health check temporarily unavailable",
            "timestamp" to LocalDateTime.now().format(formatter),
            "circuitBreaker" to "OPEN"
        )
    }
}
