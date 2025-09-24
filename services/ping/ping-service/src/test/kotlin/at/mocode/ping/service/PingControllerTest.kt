package at.mocode.ping.service

import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Unit tests for PingController
 * Tests REST endpoints with mocked dependencies
 */
@WebMvcTest(PingController::class)
@Import(PingControllerTest.TestConfig::class)
class PingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var pingService: PingServiceCircuitBreaker

    @TestConfiguration
    class TestConfig {
        @Bean
        fun pingServiceCircuitBreaker(): PingServiceCircuitBreaker = mockk(relaxed = true)
    }

    @BeforeEach
    fun setUp() {
        // Reset mocks before each test
        io.mockk.clearMocks(pingService)
    }

    @Test
    fun `should return simple ping response`() {
        // When & Then
        mockMvc.perform(get("/ping/simple"))
            .andExpect(status().isOk)
    }

    @Test
    fun `should return enhanced ping response without simulation`() {
        // Given
        val expectedResponse = EnhancedPingResponse(
            status = "pong",
            timestamp = "2023-10-01T10:00:00Z",
            service = "ping-service",
            circuitBreakerState = "CLOSED",
            responseTime = 10L
        )
        every { pingService.ping(false) } returns expectedResponse

        // When & Then
        mockMvc.perform(get("/ping/enhanced"))
            .andExpect(status().isOk)

        // Verify
        verify { pingService.ping(false) }
    }

    @Test
    fun `should return enhanced ping response with simulation enabled`() {
        // Given
        val expectedResponse = EnhancedPingResponse(
            status = "fallback",
            timestamp = "2023-10-01T10:00:00Z",
            service = "ping-service-fallback",
            circuitBreakerState = "OPEN",
            responseTime = 5L
        )
        every { pingService.ping(true) } returns expectedResponse

        // When & Then
        mockMvc.perform(get("/ping/enhanced?simulate=true"))
            .andExpect(status().isOk)

        // Verify
        verify { pingService.ping(true) }
    }

    @Test
    fun `should return health check response`() {
        // Given
        val expectedResponse = HealthResponse(
            status = "pong",
            timestamp = "2023-10-01T10:00:00Z",
            service = "ping-service",
            healthy = true
        )
        every { pingService.healthCheck() } returns expectedResponse

        // When & Then
        mockMvc.perform(get("/ping/health"))
            .andExpect(status().isOk)

        // Verify
        verify { pingService.healthCheck() }
    }

    @Test
    fun `should handle missing simulate parameter with default false`() {
        // Given
        val expectedResponse = EnhancedPingResponse(
            status = "pong",
            timestamp = "2023-10-01T10:00:00Z",
            service = "ping-service",
            circuitBreakerState = "CLOSED",
            responseTime = 8L
        )
        every { pingService.ping(false) } returns expectedResponse

        // When & Then
        mockMvc.perform(get("/ping/enhanced"))
            .andExpect(status().isOk)

        // Verify default parameter is used
        verify { pingService.ping(false) }
    }
}
