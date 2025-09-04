package at.mocode.infrastructure.gateway

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Tests für den Fallback Controller, der Circuit Breaker Szenarien behandelt.
 * Testet alle Fallback-Endpunkte für verschiedene Services.
 */
@SpringBootTest(
    classes = [GatewayApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        // Externe Abhängigkeiten für Fallback-Tests deaktivieren
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "spring.cloud.loadbalancer.enabled=false",
        // Circuit Breaker Health Indicator deaktivieren um Interferenzen zu vermeiden
        "resilience4j.circuitbreaker.configs.default.registerHealthIndicator=false",
        "management.health.circuitbreakers.enabled=false",
        // Custom Filter für reine Fallback-Tests deaktivieren
        "gateway.security.jwt.enabled=false",
        // Reaktiven Web-Anwendungstyp verwenden
        "spring.main.web-application-type=reactive",
        // Gateway Discovery deaktivieren
        "spring.cloud.gateway.discovery.locator.enabled=false",
        // Actuator Security deaktivieren
        "management.security.enabled=false",
        // Zufälligen Port setzen
        "server.port=0"
    ]
)
@ActiveProfiles("test")
class FallbackControllerTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `sollte Members Service Fallback Response zurückgeben`() {
        webTestClient.get()
            .uri("/fallback/members")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.message").isEqualTo("Member operations are temporarily unavailable")
            .jsonPath("$.service").isEqualTo("members-service")
            .jsonPath("$.status").isEqualTo(503)
            .jsonPath("$.suggestion")
            .isEqualTo("Please try again in a few moments. If the problem persists, contact support.")
            .jsonPath("$.timestamp").exists()
    }

    @Test
    fun `sollte Horses Service Fallback Response zurückgeben`() {
        webTestClient.get()
            .uri("/fallback/horses")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.message").isEqualTo("Horse registry operations are temporarily unavailable")
            .jsonPath("$.service").isEqualTo("horses-service")
            .jsonPath("$.status").isEqualTo(503)
            .jsonPath("$.suggestion").exists()
    }

    @Test
    fun `sollte Events Service Fallback Response zurückgeben`() {
        webTestClient.get()
            .uri("/fallback/events")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.message").isEqualTo("Event management operations are temporarily unavailable")
            .jsonPath("$.service").isEqualTo("events-service")
            .jsonPath("$.status").isEqualTo(503)
    }

    @Test
    fun `should return masterdata service fallback response`() {
        webTestClient.get()
            .uri("/fallback/masterdata")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.message").isEqualTo("Master data operations are temporarily unavailable")
            .jsonPath("$.service").isEqualTo("masterdata-service")
            .jsonPath("$.status").isEqualTo(503)
    }

    @Test
    fun `should return auth service fallback response`() {
        webTestClient.get()
            .uri("/fallback/auth")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.message").isEqualTo("Authentication operations are temporarily unavailable")
            .jsonPath("$.service").isEqualTo("auth-service")
            .jsonPath("$.status").isEqualTo(503)
    }

    @Test
    fun `should return default fallback response for unknown service`() {
        webTestClient.get()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.message").isEqualTo("Service is temporarily unavailable")
            .jsonPath("$.service").isEqualTo("unknown-service")
            .jsonPath("$.status").isEqualTo(503)
    }

    @Test
    fun `should handle POST requests to members fallback`() {
        webTestClient.post()
            .uri("/fallback/members")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.service").isEqualTo("members-service")
    }

    @Test
    fun `should handle POST requests to horses fallback`() {
        webTestClient.post()
            .uri("/fallback/horses")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.service").isEqualTo("horses-service")
    }

    @Test
    fun `should handle POST requests to events fallback`() {
        webTestClient.post()
            .uri("/fallback/events")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.service").isEqualTo("events-service")
    }

    @Test
    fun `should handle POST requests to masterdata fallback`() {
        webTestClient.post()
            .uri("/fallback/masterdata")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.service").isEqualTo("masterdata-service")
    }

    @Test
    fun `should handle POST requests to auth fallback`() {
        webTestClient.post()
            .uri("/fallback/auth")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.service").isEqualTo("auth-service")
    }

    @Test
    fun `should handle POST requests to default fallback`() {
        webTestClient.post()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .jsonPath("$.error").isEqualTo("SERVICE_UNAVAILABLE")
            .jsonPath("$.service").isEqualTo("unknown-service")
    }

    @Test
    fun `should return valid JSON structure for all fallback responses`() {
        val fallbackPaths = listOf(
            "/fallback/members",
            "/fallback/horses",
            "/fallback/events",
            "/fallback/masterdata",
            "/fallback/auth",
            "/fallback"
        )

        fallbackPaths.forEach { path ->
            webTestClient.get()
                .uri(path)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody()
                .jsonPath("$.error").isNotEmpty
                .jsonPath("$.message").isNotEmpty
                .jsonPath("$.service").isNotEmpty
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.status").isNumber
                .jsonPath("$.suggestion").isNotEmpty
        }
    }

    @Test
    fun `should have consistent error response structure`() {
        webTestClient.get()
            .uri("/fallback/members")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .consumeWith { result ->
                val body = String(result.responseBody ?: byteArrayOf())
                assert(body.contains("error"))
                assert(body.contains("message"))
                assert(body.contains("service"))
                assert(body.contains("timestamp"))
                assert(body.contains("status"))
                assert(body.contains("suggestion"))
            }
    }
}
