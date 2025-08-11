package at.mocode.temp.pingservice

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(PingController::class)
class PingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `ping endpoint should return pong status`() {
        mockMvc.perform(get("/ping"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value("pong"))
    }
}
