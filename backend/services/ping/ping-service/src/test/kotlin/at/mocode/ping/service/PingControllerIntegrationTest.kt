package at.mocode.ping.service

import at.mocode.ping.application.PingUseCase
import at.mocode.ping.domain.Ping
import at.mocode.ping.infrastructure.web.PingController
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

/**
 * Lightweight Spring MVC integration test (no full application context / datasource).
 */
@WebMvcTest(
  controllers = [PingController::class],
  excludeAutoConfiguration = [
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration::class
  ]
)
@Import(PingControllerIntegrationTest.TestConfig::class)
class PingControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @TestConfiguration
    class TestConfig {
        @Bean
        fun pingUseCase(): PingUseCase = mockk(relaxed = true)
    }

    @Test
    fun `should start MVC slice and serve endpoints`() {
        // Just verify the MVC wiring starts and endpoints respond 200
        mockMvc.perform(get("/ping/health")).andExpect(status().isOk)

        // For endpoints that require the use-case, the relaxed mock is sufficient,
        // but we still provide deterministic ping data.
        val useCase = TestConfig().pingUseCase()
        every { useCase.executePing(any()) } returns Ping(
            message = "Simple Ping",
            timestamp = Instant.parse("2023-10-01T10:00:00Z")
        )

        // Note: we don't assert the full JSON here (covered by PingControllerTest)
        val result = mockMvc.perform(get("/ping/simple")).andReturn()
        assertThat(result.response.status).isEqualTo(200)
    }
}
