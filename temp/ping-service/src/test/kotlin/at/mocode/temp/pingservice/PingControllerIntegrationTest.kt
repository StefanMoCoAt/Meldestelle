package at.mocode.temp.pingservice

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

/**
 * Integration tests for PingController
 * Tests REST endpoints with circuit breaker functionality using TestRestTemplate
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PingControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    private val logger = LoggerFactory.getLogger(PingControllerIntegrationTest::class.java)

    private lateinit var circuitBreaker: CircuitBreaker

    @BeforeEach
    fun setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker(PingServiceCircuitBreaker.PING_CIRCUIT_BREAKER)
        // Reset circuit breaker state before each test
        circuitBreaker.reset()
    }

    private fun getUrl(endpoint: String) = "http://localhost:$port$endpoint"

    @Test
    fun `should return basic ping response from standard endpoint`() {
        // When
        val response = restTemplate.getForEntity(getUrl("/ping"), Map::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!["status"]).isEqualTo("pong")

        logger.info("Standard ping endpoint response: {}", response.body)
    }

    @Test
    fun `should return enhanced ping response when circuit breaker is closed`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When
        val response = restTemplate.getForEntity(getUrl("/ping/enhanced"), Map::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull

        val body = response.body!!
        assertThat(body["status"]).isEqualTo("pong")
        assertThat(body["service"]).isEqualTo("ping-service")
        assertThat(body["circuitBreaker"]).isEqualTo("CLOSED")
        assertThat(body["timestamp"]).isNotNull()

        logger.info("Enhanced ping response: {}", body)
    }

    @Test
    fun `should return enhanced ping response without simulation`() {
        // When
        val response = restTemplate.getForEntity(getUrl("/ping/enhanced?simulate=false"), Map::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull

        val body = response.body!!
        assertThat(body["status"]).isEqualTo("pong")
        assertThat(body["service"]).isEqualTo("ping-service")
        assertThat(body["circuitBreaker"]).isEqualTo("CLOSED")

        logger.info("Enhanced ping without simulation: {}", body)
    }

    @Test
    fun `should handle failure simulation in enhanced ping endpoint`() {
        // Multiple calls to potentially trigger failures due to random simulation
        repeat(3) { i ->
            val response = restTemplate.getForEntity(getUrl("/ping/enhanced?simulate=true"), Map::class.java)

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull

            val body = response.body!!
            logger.info("Attempt {}: Response status = {}, Circuit breaker state = {}",
                       i + 1, body["status"], circuitBreaker.state)

            // Response should be either success or fallback
            assertThat(body["status"]).isIn("pong", "fallback")
        }
    }

    @Test
    fun `should return health check response when circuit breaker is closed`() {
        // Given
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.CLOSED)

        // When
        val response = restTemplate.getForEntity(getUrl("/ping/health"), Map::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull

        val body = response.body!!
        assertThat(body["status"]).isEqualTo("UP")
        assertThat(body["circuitBreaker"]).isEqualTo("CLOSED")
        assertThat(body["timestamp"]).isNotNull()

        logger.info("Health check response: {}", body)
    }

    @Test
    fun `should return fallback health check when circuit breaker is open`() {
        // Given - manually open circuit breaker
        circuitBreaker.transitionToOpenState()
        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.OPEN)

        // When
        val response = restTemplate.getForEntity(getUrl("/ping/health"), Map::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull

        val body = response.body!!
        assertThat(body["status"]).isEqualTo("DOWN")
        assertThat(body["circuitBreaker"]).isEqualTo("OPEN")
        assertThat(body["message"]).isEqualTo("Health check temporarily unavailable")

        logger.info("Fallback health check response: {}", body)
    }

    @Test
    fun `should return response from test-failure endpoint`() {
        // When
        val response = restTemplate.getForEntity(getUrl("/ping/test-failure"), Map::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull

        val body = response.body!!

        // Due to 60% failure simulation, we expect either success or fallback
        assertThat(body["status"]).isIn("pong", "fallback")

        if (body["status"] == "fallback") {
            assertThat(body["service"]).isEqualTo("ping-service-fallback")
            assertThat(body["circuitBreaker"]).isEqualTo("OPEN")
            assertThat(body["message"]).isEqualTo("Service temporarily unavailable")
            assertThat(body["error"]).isNotNull()
        } else {
            assertThat(body["service"]).isEqualTo("ping-service")
            assertThat(body["circuitBreaker"]).isEqualTo("CLOSED")
        }

        logger.info("Test failure endpoint response: {}", body)
    }

    @Test
    fun `should handle multiple rapid requests correctly`() {
        // Execute multiple rapid requests
        val results = mutableListOf<Map<String, Any>>()

        repeat(5) { i ->
            val response = restTemplate.getForEntity(getUrl("/ping/enhanced"), Map::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull

            @Suppress("UNCHECKED_CAST")
            val body = response.body as Map<String, Any>
            results.add(body)

            logger.info("Rapid request {}: status = {}", i + 1, body["status"])
        }

        // All should be successful since we're not simulating failures
        results.forEach { response ->
            assertThat(response["status"]).isEqualTo("pong")
            assertThat(response["service"]).isEqualTo("ping-service")
        }
    }

    @Test
    fun `should maintain circuit breaker state across requests`() {
        // Given - manually open circuit breaker
        circuitBreaker.transitionToOpenState()

        // When - make multiple requests
        repeat(3) { i ->
            val response = restTemplate.getForEntity(getUrl("/ping/enhanced"), Map::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull

            val body = response.body!!

            // All should return fallback responses while circuit breaker is open
            assertThat(body["status"]).isEqualTo("fallback")
            assertThat(body["circuitBreaker"]).isEqualTo("OPEN")

            logger.info("Request {} with OPEN circuit breaker: {}", i + 1, body["status"])
        }

        assertThat(circuitBreaker.state).isEqualTo(CircuitBreaker.State.OPEN)
    }

    @Test
    fun `should test all endpoints return valid responses`() {
        val endpoints = listOf(
            "/ping",
            "/ping/enhanced",
            "/ping/health",
            "/ping/test-failure"
        )

        endpoints.forEach { endpoint ->
            val response = restTemplate.getForEntity(getUrl(endpoint), Map::class.java)

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull()
            assertThat(response.body!!).isNotEmpty()

            logger.info("Endpoint {} returned valid response: {}", endpoint, response.body)
        }
    }

    @Test
    fun `should track circuit breaker metrics after calls`() {
        // Given
        val initialMetrics = circuitBreaker.metrics
        logger.info("Initial metrics - Calls: {}, Failures: {}",
                   initialMetrics.numberOfBufferedCalls, initialMetrics.numberOfFailedCalls)

        // When - execute some calls
        repeat(3) {
            restTemplate.getForEntity(getUrl("/ping/enhanced"), Map::class.java)
        }

        // Then
        val newMetrics = circuitBreaker.metrics
        assertThat(newMetrics.numberOfBufferedCalls).isGreaterThanOrEqualTo(3)

        logger.info("Updated metrics - Calls: {}, Failure rate: {}%, Successful: {}, Failed: {}",
                   newMetrics.numberOfBufferedCalls,
                   newMetrics.failureRate,
                   newMetrics.numberOfSuccessfulCalls,
                   newMetrics.numberOfFailedCalls)
    }
}
