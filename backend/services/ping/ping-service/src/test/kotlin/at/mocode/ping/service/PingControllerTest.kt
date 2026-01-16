package at.mocode.ping.service

import at.mocode.ping.application.PingUseCase
import at.mocode.ping.domain.Ping
import at.mocode.ping.infrastructure.persistence.PingRepositoryAdapter
import at.mocode.ping.infrastructure.web.PingController
import at.mocode.ping.test.TestPingServiceApplication
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
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
    properties = ["spring.aop.proxy-target-class=true"]
)
@ContextConfiguration(classes = [TestPingServiceApplication::class])
@ActiveProfiles("test")
@Import(PingControllerTest.PingControllerTestConfig::class)
@AutoConfigureMockMvc
class PingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    @Qualifier("pingUseCaseMock")
    private lateinit var pingUseCase: PingUseCase

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Configuration
    class PingControllerTestConfig {
        @Bean("pingUseCaseMock")
        @Primary
        fun pingUseCase(): PingUseCase = mockk(relaxed = true)

        @Bean
        @Primary
        fun pingRepositoryAdapter(): PingRepositoryAdapter = mockk(relaxed = true)
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

        val body = result.response.contentAsString
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
        val json = objectMapper.readTree(body)
        assertThat(json.has("status")).isTrue
        assertThat(json["status"].asText()).isEqualTo("up")
        assertThat(json["service"].asText()).isEqualTo("ping-service")
    }
}
