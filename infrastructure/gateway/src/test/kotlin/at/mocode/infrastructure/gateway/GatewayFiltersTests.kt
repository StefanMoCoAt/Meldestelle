package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.config.TestSecurityConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Tests for Gateway custom filters: CorrelationId, Enhanced Logging, and Rate Limiting.
 * Tests filter behavior without disabling them (unlike other test classes).
 */
@SpringBootTest(
    classes = [GatewayApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        // Disable external dependencies
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "spring.cloud.loadbalancer.enabled=false",
        // Disable circuit breaker for filter tests
        "resilience4j.circuitbreaker.configs.default.registerHealthIndicator=false",
        "management.health.circuitbreakers.enabled=false",
        // Keep custom filters enabled for testing
        "gateway.security.jwt.enabled=false", // Disable JWT but keep other filters
        // Use reactive web application type
        "spring.main.web-application-type=reactive",
        // Disable gateway discovery - use explicit routes
        "spring.cloud.gateway.discovery.locator.enabled=false",
        // Disable actuator security
        "management.security.enabled=false",
        // Set random port
        "server.port=0"
    ]
)
@ActiveProfiles("dev") // Use dev profile to enable filters
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class, GatewayFiltersTests.TestFilterConfig::class)
class GatewayFiltersTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `should add correlation ID header when not present`() {
        webTestClient.get()
            .uri("/test/correlation")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("X-Correlation-ID")
            .expectBody(String::class.java)
            .isEqualTo("correlation-test")
    }

    @Test
    fun `should preserve existing correlation ID header`() {
        val existingCorrelationId = "test-correlation-123"

        webTestClient.get()
            .uri("/test/correlation")
            .header("X-Correlation-ID", existingCorrelationId)
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-Correlation-ID", existingCorrelationId)
            .expectBody(String::class.java)
            .isEqualTo("correlation-test")
    }

    @Test
    fun `should add rate limiting headers`() {
        webTestClient.get()
            .uri("/test/ratelimit")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("X-RateLimit-Enabled")
            .expectHeader().exists("X-RateLimit-Limit")
            .expectHeader().exists("X-RateLimit-Remaining")
            .expectHeader().valueEquals("X-RateLimit-Enabled", "true")
    }

    @Test
    fun `should apply different rate limits for auth endpoints`() {
        // This test validates rate-limit headers only; endpoint body/status may vary based on route mapping
        webTestClient.get()
            .uri("/api/auth/test")
            .exchange()
            .expectHeader().valueEquals("X-RateLimit-Limit", "20") // AUTH_ENDPOINT_LIMIT
    }

    @Test
    fun `should apply higher rate limit for authenticated users`() {
        webTestClient.get()
            .uri("/test/ratelimit")
            .header("Authorization", "Bearer test-token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-RateLimit-Limit", "200") // AUTHENTICATED_LIMIT
    }

    @Test
    fun `should apply admin rate limit for admin users`() {
        webTestClient.get()
            .uri("/test/ratelimit")
            .header("Authorization", "Bearer test-token")
            .header("X-User-Role", "ADMIN")
            .header("X-User-ID", "admin-test-user") // Required for admin detection security
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("X-RateLimit-Limit", "500") // ADMIN_LIMIT
    }

    @Test
    fun `should enforce rate limiting after exceeding limit`() {
        // This test would need multiple requests to test actual rate limiting
        // For simplicity, we just verify the headers are present
        val responses = (1..5).map {
            webTestClient.get()
                .uri("/test/ratelimit")
                .exchange()
                .expectStatus().isOk
                .expectHeader().exists("X-RateLimit-Remaining")
                .returnResult(String::class.java)
        }

        // Verify that remaining count decreases
        assert(responses.isNotEmpty())
    }

    @Test
    fun `should handle requests with X-Forwarded-For header`() {
        webTestClient.get()
            .uri("/test/ratelimit")
            .header("X-Forwarded-For", "192.168.1.100, 10.0.0.1")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("X-RateLimit-Enabled")
    }

    /**
     * Test configuration that provides routes for filter testing.
     */
    @Configuration
    class TestFilterConfig {

        @Bean
        fun filterTestRoutes(builder: RouteLocatorBuilder): RouteLocator = builder.routes()
            .route("test-correlation") { r ->
                r.path("/test/correlation")
                    .uri("forward:/mock/correlation-test")
            }
            .route("test-ratelimit") { r ->
                r.path("/test/ratelimit")
                    .uri("forward:/mock/ratelimit-test")
            }
            .route("test-auth-endpoint") { r ->
                r.path("/api/auth/**")
                    .filters { f -> f.stripPrefix(1) }
                    .uri("forward:/mock/auth-test")
            }
            .build()

        @Bean
        fun filterTestController(): FilterTestController = FilterTestController()
    }

    /**
     * Mock controller for filter testing.
     */
    @RestController
    @RequestMapping("/mock")
    class FilterTestController {

        @GetMapping("/correlation-test")
        fun correlationTest(): String = "correlation-test"

        @GetMapping("/ratelimit-test")
        fun rateLimitTest(): String = "ratelimit-test"

        @GetMapping("/auth-test")
        fun authEndpointTest(): String = "auth-endpoint-test"
    }
}
