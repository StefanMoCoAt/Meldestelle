package at.mocode.infrastructure.gateway

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewaySmokeTest {

  @LocalServerPort
  private var port: Int = 0

  @Test
  @DisplayName("Actuator Health ist öffentlich erreichbar")
  fun healthIsAccessible() {
    val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    client
      .get()
      .uri("http://localhost:$port/actuator/health")
      .exchange()
      .expectStatus().isOk
      .expectBody()
  }

  @Test
  @DisplayName("Fallback-Endpoint liefert 503 zurück")
  fun fallbackWorks() {
    val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    client
      .get()
      .uri("http://localhost:$port/fallback")
      .exchange()
      .expectStatus().isEqualTo(503)
  }
}
