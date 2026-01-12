package at.mocode.ping.service

import at.mocode.ping.domain.Ping
import at.mocode.ping.infrastructure.web.PingController
import at.mocode.ping.application.PingUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

/**
 * Unit tests for PingController
 * Tests REST endpoints with mocked dependencies
 */
@WebMvcTest(
    controllers = [PingController::class],
    excludeAutoConfiguration = [
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration::class
    ]
)
@Import(PingControllerTest.TestConfig::class)
@AutoConfigureMockMvc
class PingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var pingUseCase: PingUseCase

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @TestConfiguration
    class TestConfig {
        @Bean
        fun pingUseCase(): PingUseCase = mockk(relaxed = true)
    }

    @BeforeEach
    fun setUp() {
        // Reset mocks before each test
        io.mockk.clearMocks(pingUseCase)
    }

    @Test
    fun `should return simple ping response`() {
        // Given
        every { pingUseCase.executePing(any()) } returns Ping(
            message = "Simple Ping",
            timestamp = Instant.parse("2023-10-01T10:00:00Z")
        )

        // When & Then
        val mvcResult: MvcResult = mockMvc.perform(get("/ping/simple"))
            .andExpect(request().asyncStarted())
            .andReturn()

        val result = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andReturn()

        // In some environments the JSONPath matcher fails to parse the response body.
        // We still validate the serialized output contains the expected fields.
        val body = result.response.contentAsString
        System.out.println("[DEBUG_LOG] /ping/simple response status=${result.response.status} contentType=${result.response.contentType} body=$body")
        val json = objectMapper.readTree(body)
        assertThat(json.has("status")).isTrue
        assertThat(json["status"].asText()).isEqualTo("pong")
        assertThat(json["service"].asText()).isEqualTo("ping-service")

        verify { pingUseCase.executePing("Simple Ping") }
    }

    @Test
    fun `should return enhanced ping response`() {
        // Given
        every { pingUseCase.executePing(any()) } returns Ping(
            message = "Enhanced Ping",
            timestamp = Instant.parse("2023-10-01T10:00:00Z")
        )

        // When & Then
        val mvcResult: MvcResult = mockMvc.perform(get("/ping/enhanced"))
            .andExpect(request().asyncStarted())
            .andReturn()

        val result = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andReturn()

        val body = result.response.contentAsString
        System.out.println("[DEBUG_LOG] /ping/enhanced response status=${result.response.status} contentType=${result.response.contentType} body=$body")
        val json = objectMapper.readTree(body)
        assertThat(json.has("status")).isTrue
        assertThat(json["status"].asText()).isEqualTo("pong")
        assertThat(json["service"].asText()).isEqualTo("ping-service")

        verify { pingUseCase.executePing("Enhanced Ping") }
    }

    @Test
    fun `should return health check response`() {
        // When & Then
        val mvcResult: MvcResult = mockMvc.perform(get("/ping/health"))
            .andExpect(request().asyncStarted())
            .andReturn()

        val result = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andReturn()

        val body = result.response.contentAsString
        System.out.println("[DEBUG_LOG] /ping/health response status=${result.response.status} contentType=${result.response.contentType} body=$body")
        val json = objectMapper.readTree(body)
        assertThat(json.has("status")).isTrue
        assertThat(json["status"].asText()).isEqualTo("up")
        assertThat(json["service"].asText()).isEqualTo("ping-service")
    }
}
