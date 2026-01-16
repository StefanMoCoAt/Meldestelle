package at.mocode.ping.service

import at.mocode.ping.application.PingUseCase
import at.mocode.ping.domain.Ping
import at.mocode.ping.infrastructure.persistence.PingRepositoryAdapter
import at.mocode.ping.infrastructure.web.PingController
import at.mocode.ping.test.TestPingServiceApplication
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

/**
 * Lightweight Spring MVC integration test (no full application context / datasource).
 */
@WebMvcTest(
  controllers = [PingController::class],
  properties = ["spring.aop.proxy-target-class=true"]
)
@ContextConfiguration(classes = [TestPingServiceApplication::class])
@ActiveProfiles("test")
@Import(PingControllerIntegrationTest.PingControllerIntegrationTestConfig::class)
class PingControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    @Qualifier("pingUseCaseIntegrationMock")
    private lateinit var pingUseCase: PingUseCase

    @Configuration
    class PingControllerIntegrationTestConfig {
        @Bean("pingUseCaseIntegrationMock")
        @Primary
        fun pingUseCase(): PingUseCase = mockk(relaxed = true)

        @Bean
        @Primary
        fun pingRepositoryAdapter(): PingRepositoryAdapter = mockk(relaxed = true)
    }

    @Test
    fun `should start MVC slice and serve endpoints`() {
        // Just verify the MVC wiring starts and endpoints respond 200
        mockMvc.perform(get("/ping/health")).andExpect(status().isOk)

        // For endpoints that require the use-case, the relaxed mock is sufficient,
        // but we still provide deterministic ping data.
        every { pingUseCase.executePing(any()) } returns Ping(
            message = "Simple Ping",
            timestamp = Instant.parse("2023-10-01T10:00:00Z")
        )

        // Note: we don't assert the full JSON here (covered by PingControllerTest)
        val result = mockMvc.perform(get("/ping/simple")).andReturn()
        assertThat(result.response.status).isEqualTo(200)
    }
}
