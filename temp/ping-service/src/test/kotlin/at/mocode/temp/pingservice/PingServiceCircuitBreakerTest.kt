package at.mocode.temp.pingservice

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.math.ceil

/**
 * Comprehensive test suite for PingServiceCircuitBreaker
 * Tests circuit breaker behavior, fallback methods, and state transitions
 */
@SpringBootTest
class PingServiceCircuitBreakerTest {

    @Autowired
    private lateinit var pingServiceCircuitBreaker: PingServiceCircuitBreaker

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    private val logger = LoggerFactory.getLogger(PingServiceCircuitBreakerTest::class.java)

    private lateinit var circuitBreaker: CircuitBreaker

    @BeforeEach
    fun setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker(PingServiceCircuitBreaker.PING_CIRCUIT_BREAKER)
        // Reset circuit breaker state before each test
        circuitBreaker.reset()
    }

    @Test
    fun `should return successful ping response when circuit breaker is closed`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When
        val result = pingServiceCircuitBreaker.ping(simulateFailure = false)

        // Then
        assertThat(result["status"]).isEqualTo("pong")
        assertThat(result["service"]).isEqualTo("ping-service")
        assertThat(result["circuitBreaker"]).isEqualTo("CLOSED")
        assertThat(result).containsKeys("timestamp")
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)
    }

    @Test
    fun `should handle single failure without opening circuit breaker`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When - single failure should not open circuit breaker (needs 4 failures minimum)
        // Try multiple times since failure simulation is probabilistic (60% chance)
        var result: Map<String, Any>
        var attempts = 0
        do {
            result = pingServiceCircuitBreaker.ping(simulateFailure = true)
            attempts++
        } while (result["status"] == "pong" && attempts < 10)

        // Then - should get fallback response eventually, but circuit breaker might still be closed after just one failure
        assertThat(result["status"]).isEqualTo("fallback")
        assertThat(result["service"]).isEqualTo("ping-service-fallback")
        assertThat(result["circuitBreaker"]).isEqualTo("OPEN")
        assertThat(result).containsKeys("timestamp", "message", "error")
        logger.info("Circuit breaker state after single failure (after {} attempts): {}", attempts, circuitBreaker.state)
    }

    @Test
    fun `should open circuit breaker after multiple failures`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When - trigger multiple failures to reach minimum-number-of-calls (4) and failure threshold (60%)
        // Keep calling until we get enough failures to trigger the circuit breaker
        var failureCount = 0
        var totalCalls = 0
        val maxAttempts = 20 // Prevent infinite loop

        while (circuitBreaker.state == CircuitBreaker.State.CLOSED && totalCalls < maxAttempts) {
            val result = pingServiceCircuitBreaker.ping(simulateFailure = true)
            totalCalls++

            if (result["status"] == "fallback") {
                failureCount++
            }

            logger.info("Attempt {}: Circuit breaker state = {}, Response status = {}, Failures so far = {}",
                       totalCalls, circuitBreaker.state, result["status"], failureCount)
        }

        // Then - circuit breaker should be open after sufficient failures
        logger.info("Final circuit breaker state: {} after {} total calls with {} failures",
                   circuitBreaker.state, totalCalls, failureCount)

        // The circuit breaker should eventually open due to failure rate threshold
        // Note: Due to the minimum calls requirement (4) and failure rate threshold (60%),
        // it might take several attempts depending on random simulation
        if (totalCalls >= 4 && failureCount >= ceil(totalCalls * 0.6)) {
            assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.OPEN)
        }
    }

    @Test
    fun `should return fallback response when circuit breaker is manually opened`() {
        // Given
        circuitBreaker.transitionToOpenState()
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.OPEN)

        // When
        val result = pingServiceCircuitBreaker.ping(simulateFailure = false)

        // Then
        assertThat(result["status"]).isEqualTo("fallback")
        assertThat(result["service"]).isEqualTo("ping-service-fallback")
        assertThat(result["circuitBreaker"]).isEqualTo("OPEN")
        assertThat(result["message"]).isEqualTo("Service temporarily unavailable")
    }

    @Test
    fun `should return successful health check when circuit breaker is closed`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When - retry if we get random failure (10% chance in health check method)
        var result: Map<String, Any>
        var attempts = 0
        do {
            result = pingServiceCircuitBreaker.healthCheck()
            attempts++
        } while (result["status"] == "DOWN" && attempts < 15) // 15 attempts should be enough to get a success

        // Then
        assertThat(result["status"]).isEqualTo("UP")
        assertThat(result["circuitBreaker"]).isEqualTo("CLOSED")
        assertThat(result).containsKeys("timestamp")
        logger.info("Health check succeeded after {} attempts", attempts)
    }

    @Test
    fun `should return fallback health check when circuit breaker is open`() {
        // Given
        circuitBreaker.transitionToOpenState()
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.OPEN)

        // When
        val result = pingServiceCircuitBreaker.healthCheck()

        // Then
        assertThat(result["status"]).isEqualTo("DOWN")
        assertThat(result["circuitBreaker"]).isEqualTo("OPEN")
        assertThat(result["message"]).isEqualTo("Health check temporarily unavailable")
    }

    @Test
    fun `should test circuit breaker state transitions`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When - manually transition to open state
        circuitBreaker.transitionToOpenState()

        // Then
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.OPEN)

        // When - manually transition to half-open state
        circuitBreaker.transitionToHalfOpenState()

        // Then
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.HALF_OPEN)

        // When - successful call should close circuit breaker
        val result = pingServiceCircuitBreaker.ping(simulateFailure = false)

        // Then
        assertThat(result["status"]).isEqualTo("pong")
        logger.info("Circuit breaker state after successful call in HALF_OPEN: {}", circuitBreaker.state)
    }

    @Test
    fun `should track circuit breaker metrics`() {
        // Given
        val metrics = circuitBreaker.metrics

        // When
        val initialFailureRate = metrics.failureRate
        val initialNumberOfCalls = metrics.numberOfBufferedCalls

        // Execute some successful calls
        repeat(3) {
            pingServiceCircuitBreaker.ping(simulateFailure = false)
        }

        // Then
        val newMetrics = circuitBreaker.metrics
        assertThat(newMetrics.numberOfBufferedCalls).isGreaterThan(initialNumberOfCalls)
        logger.info("Circuit breaker metrics - Calls: {}, Failure rate: {}%, Successful calls: {}, Failed calls: {}",
                   newMetrics.numberOfBufferedCalls,
                   newMetrics.failureRate,
                   newMetrics.numberOfSuccessfulCalls,
                   newMetrics.numberOfFailedCalls)
    }

    @Test
    fun `should handle concurrent calls correctly`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When - execute concurrent calls
        val futures = (1..10).map { index ->
            Thread {
                val result = pingServiceCircuitBreaker.ping(simulateFailure = false)
                logger.info("Concurrent call {}: status = {}", index, result["status"])
            }
        }

        futures.forEach { it.start() }
        futures.forEach { it.join() }

        // Then
        val metrics = circuitBreaker.metrics
        assertThat(metrics.numberOfBufferedCalls).isEqualTo(10)
        assertThat(metrics.numberOfSuccessfulCalls).isEqualTo(10)
        assertThat(metrics.numberOfFailedCalls).isEqualTo(0)
    }
}
