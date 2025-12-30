package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.config.TestSecurityConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import at.mocode.infrastructure.gateway.support.GatewayTestContext
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Tests für den Fallback Controller, der Circuit Breaker Szenarien behandelt.
 * Testet alle Fallback-Endpunkte für verschiedene Services.
 */
@GatewayTestContext
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
class FallbackControllerTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `sollte Members Service Fallback Response zurueckgeben`() {
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
    fun `sollte Horses Service Fallback Response zurueckgeben`() {
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
    fun `sollte Events Service Fallback Response zurueckgeben`() {
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
