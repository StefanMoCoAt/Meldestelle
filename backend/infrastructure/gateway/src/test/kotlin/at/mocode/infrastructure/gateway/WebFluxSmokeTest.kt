package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.support.GatewayTestContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@GatewayTestContext
@Import(WebFluxSmokeTest.SmokeConfig::class)
class WebFluxSmokeTest {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Test
  fun `should load reactive web context and serve smoke endpoint`() {
    webTestClient.get()
      .uri("/smoke")
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .isEqualTo("ok")
  }

  @Configuration
  class SmokeConfig {
    @Bean
    fun smokeController(): SmokeController = SmokeController()
  }

  @RestController
  @RequestMapping
  class SmokeController {
    @GetMapping("/smoke")
    fun smoke(): String = "ok"
  }
}
