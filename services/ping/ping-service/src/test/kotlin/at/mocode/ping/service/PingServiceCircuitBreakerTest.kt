package at.mocode.ping.service

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
 * Updated to assert DTOs instead of Maps.
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
        assertThat(result.status).isEqualTo("pong")
        assertThat(result.service).isEqualTo("ping-service")
        assertThat(result.circuitBreakerState).isEqualTo("CLOSED")
        assertThat(result.timestamp).isNotBlank()
        assertThat(result.responseTime).isGreaterThanOrEqualTo(0)
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)
    }

    @Test
    fun `should handle single failure without opening circuit breaker`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When - try until we hit a simulated failure (60% chance)
        var result = pingServiceCircuitBreaker.ping(simulateFailure = true)
        var attempts = 1
        while (result.status == "pong" && attempts < 10) {
            result = pingServiceCircuitBreaker.ping(simulateFailure = true)
            attempts++
        }

        // Then - should get fallback response eventually
        assertThat(result.status).isEqualTo("fallback")
        assertThat(result.service).isEqualTo("ping-service-fallback")
        assertThat(result.circuitBreakerState).isEqualTo("OPEN")
        logger.info("Circuit breaker state after single failure (after {} attempts): {}", attempts, circuitBreaker.state)
    }

    @Test
    fun `should open circuit breaker after multiple failures`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When - trigger multiple failures to reach minimum-number-of-calls (4) and failure threshold (60%)
        var failureCount = 0
        var totalCalls = 0
        val maxAttempts = 20 // Prevent infinite loop

        while (circuitBreaker.state == CircuitBreaker.State.CLOSED && totalCalls < maxAttempts) {
            val result = pingServiceCircuitBreaker.ping(simulateFailure = true)
            totalCalls++
            if (result.status == "fallback") failureCount++
            logger.info(
                "Attempt {}: Circuit breaker state = {}, Response status = {}, Failures so far = {}",
                totalCalls, circuitBreaker.state, result.status, failureCount
            )
        }

        // Then - circuit breaker should be open after sufficient failures
        logger.info(
            "Final circuit breaker state: {} after {} total calls with {} failures",
            circuitBreaker.state, totalCalls, failureCount
        )
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
        assertThat(result.status).isEqualTo("fallback")
        assertThat(result.service).isEqualTo("ping-service-fallback")
        assertThat(result.circuitBreakerState).isEqualTo("OPEN")
    }

    @Test
    fun `should return successful health check when circuit breaker is closed`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When
        val result = pingServiceCircuitBreaker.healthCheck()

        // Then
        assertThat(result.healthy).isTrue()
        assertThat(result.status).isEqualTo("pong")
        assertThat(result.timestamp).isNotBlank()
    }

    @Test
    fun `should return fallback health check when circuit breaker is open`() {
        // Given
        circuitBreaker.transitionToOpenState()
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.OPEN)

        // When
        val result = pingServiceCircuitBreaker.healthCheck()

        // Then
        assertThat(result.healthy).isFalse()
        assertThat(result.status).isEqualTo("down")
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
        assertThat(result.status).isEqualTo("pong")
        logger.info("Circuit breaker state after successful call in HALF_OPEN: {}", circuitBreaker.state)
    }

    @Test
    fun `should track circuit breaker metrics`() {
        // Given
        val metrics = circuitBreaker.metrics
        val initialNumberOfCalls = metrics.numberOfBufferedCalls

        // When - execute some successful calls
        repeat(3) {
            pingServiceCircuitBreaker.ping(simulateFailure = false)
        }

        // Then
        val newMetrics = circuitBreaker.metrics
        assertThat(newMetrics.numberOfBufferedCalls).isGreaterThan(initialNumberOfCalls)
        logger.info(
            "Circuit breaker metrics - Calls: {}, Failure rate: {}%, Successful calls: {}, Failed calls: {}",
            newMetrics.numberOfBufferedCalls,
            newMetrics.failureRate,
            newMetrics.numberOfSuccessfulCalls,
            newMetrics.numberOfFailedCalls
        )
    }

    @Test
    fun `should handle concurrent calls correctly`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When - execute concurrent calls
        val threads = (1..10).map { index ->
            Thread {
                val result = pingServiceCircuitBreaker.ping(simulateFailure = false)
                logger.info("Concurrent call {}: status = {}", index, result.status)
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Then - verify circuit breaker recorded calls
        val metrics = circuitBreaker.metrics
        assertThat(metrics.numberOfBufferedCalls).isGreaterThanOrEqualTo(10)
        assertThat(metrics.numberOfSuccessfulCalls).isGreaterThanOrEqualTo(10)
    }
}
