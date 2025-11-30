package at.mocode.ping.service

import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
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
     * Primary ping method with circuit breaker protection returning DTO directly
     *
     * @param simulateFailure - if true, randomly throws exceptions to test circuit breaker
     */
    @CircuitBreaker(name = PING_CIRCUIT_BREAKER, fallbackMethod = "fallbackPing")
    fun ping(simulateFailure: Boolean = false): EnhancedPingResponse {
        val start = System.nanoTime()
        logger.info("Executing ping service call...")

        if (simulateFailure && Random.nextDouble() < 0.6) {
            logger.warn("Simulating service failure for circuit breaker testing")
            throw RuntimeException("Simulated service failure")
        }

        val currentTime = LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC).format(formatter)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        logger.info("Ping service call successful")

        return EnhancedPingResponse(
            status = "pong",
            timestamp = currentTime,
            service = "ping-service",
            circuitBreakerState = "CLOSED",
            responseTime = elapsedMs
        )
    }

    /**
     * Fallback method called when circuit breaker is OPEN
     *
     * @param simulateFailure - original parameter (ignored in fallback)
     * @param exception - the exception that triggered the fallback
     */
    fun fallbackPing(simulateFailure: Boolean = false, exception: Exception): EnhancedPingResponse {
        val start = System.nanoTime()
        // Die volle Exception nur loggen, nicht an den Client weitergeben.
        logger.warn("Circuit breaker fallback triggered due to: {}", exception.toString())

        val currentTime = LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC).format(formatter)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000

        return EnhancedPingResponse(
            status = "fallback",
            timestamp = currentTime,
            service = "ping-service-fallback",
            circuitBreakerState = "OPEN",
            responseTime = elapsedMs
        )
    }

    /**
     * Health check method with circuit breaker protection returning DTO directly
     */
    @CircuitBreaker(name = PING_CIRCUIT_BREAKER, fallbackMethod = "fallbackHealth")
    fun healthCheck(): HealthResponse {
        logger.info("Executing health check...")

        val currentTime = LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC).format(formatter)
        return HealthResponse(
            status = "pong",
            timestamp = currentTime,
            service = "ping-service",
            healthy = true
        )
    }

    /**
     * Fallback for health check returning DTO
     */
    fun fallbackHealth(exception: Exception): HealthResponse {
        logger.warn("Health check fallback triggered: {}", exception.message)

        val currentTime = LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC).format(formatter)
        return HealthResponse(
            status = "down",
            timestamp = currentTime,
            service = "ping-service",
            healthy = false
        )
    }
}
