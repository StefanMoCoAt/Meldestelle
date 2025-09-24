package at.mocode.ping.service

import io.mockk.mockk
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(LegacyPingController::class)
@Import(PingControllerTest.TestConfig::class)
class PingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @TestConfiguration
    class TestConfig {
        @Bean
        fun pingServiceCircuitBreaker(): PingServiceCircuitBreaker = mockk()

        @Bean
        fun circuitBreakerRegistry(): CircuitBreakerRegistry = mockk()
    }

    @Test
    fun `ping endpoint should return pong status`() {
        mockMvc.perform(get("/ping"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value("pong"))
    }
}
