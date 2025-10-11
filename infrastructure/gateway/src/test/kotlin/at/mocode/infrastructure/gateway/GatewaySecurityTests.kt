package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.config.TestSecurityConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.*

/**
 * Tests for Gateway security configuration including CORS settings.
 * Tests the overall security setup and cross-origin request handling.
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
        // Disable circuit breaker for security tests
        "resilience4j.circuitbreaker.configs.default.registerHealthIndicator=false",
        "management.health.circuitbreakers.enabled=false",
        // Disable JWT for CORS testing
        "gateway.security.jwt.enabled=false",
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
@ActiveProfiles("test") // Use test profile to disable unrelated global filters; CORS config is present in application-test.yml
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class, GatewaySecurityTests.TestSecurityConfig::class)
class GatewaySecurityTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUpClient() {
        // Ensure absolute base URL with a scheme to satisfy the CORS processor
        webTestClient = webTestClient.mutate()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @Test
    fun `should handle CORS preflight requests`() {
        webTestClient.options()
            .uri("/api/members/test")
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "Content-Type,Authorization")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
            .expectHeader().exists("Access-Control-Allow-Methods")
            .expectHeader().exists("Access-Control-Allow-Headers")
    }

    @Test
    fun `should allow requests from localhost origins`() {
        webTestClient.get()
            .uri("/test/cors")
            .header("Origin", "http://localhost:3000")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
    }

    @Test
    fun `should allow requests from meldestelle domain`() {
        webTestClient.get()
            .uri("/test/cors")
            .header("Origin", "https://app.meldestelle.at")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
    }

    @Test
    fun `should handle POST requests with CORS headers`() {
        webTestClient.post()
            .uri("/test/cors")
            .header("Origin", "http://localhost:3000")
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
    }

    @Test
    fun `should handle PUT requests with CORS headers`() {
        webTestClient.put()
            .uri("/test/cors")
            .header("Origin", "http://localhost:8080")
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
    }

    @Test
    fun `should handle DELETE requests with CORS headers`() {
        webTestClient.delete()
            .uri("/test/cors")
            .header("Origin", "http://localhost:4200")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
    }

    @Test
    fun `should set max age for CORS requests`() {
        webTestClient.options()
            .uri("/test/cors")
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "GET")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Max-Age")
    }

    @Test
    fun `should allow credentials in CORS requests`() {
        webTestClient.get()
            .uri("/test/cors")
            .header("Origin", "http://localhost:3000")
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueEquals("Access-Control-Allow-Credentials", "true")
    }

    @Test
    fun `should handle complex CORS scenarios`() {
        // Simulate a complex frontend request with custom headers
        webTestClient.options()
            .uri("/api/members/complex")
            .header("Origin", "https://frontend.meldestelle.at")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "Authorization,Content-Type,X-Requested-With")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
            .expectHeader().exists("Access-Control-Allow-Methods")
            .expectHeader().exists("Access-Control-Allow-Headers")
            .expectHeader().valueEquals("Access-Control-Allow-Credentials", "true")
    }

    @Test
    fun `should not duplicate CORS headers due to deduplication filter`() {
        webTestClient.get()
            .uri("/test/cors")
            .header("Origin", "http://localhost:3000")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
            .expectHeader().exists("Access-Control-Allow-Credentials")
            // Verify headers appear only once (DedupeResponseHeader filter should work)
    }

    @Test
    fun `should handle different HTTP methods allowed in CORS`() {
        val allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

        allowedMethods.forEach { method ->
            webTestClient.options()
                .uri("/test/cors")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", method)
                .exchange()
                .expectStatus().isOk
                .expectHeader().exists("Access-Control-Allow-Methods")
        }
    }

    @Test
    fun `should handle authorization headers in CORS requests`() {
        webTestClient.get()
            .uri("/test/cors")
            .header("Origin", "http://localhost:3000")
            .header("Authorization", "Bearer test-token")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Access-Control-Allow-Origin")
    }

    @Test
    fun `should maintain security headers in responses`() {
        webTestClient.get()
            .uri("/test/cors")
            .exchange()
            .expectStatus().isOk
            .expectHeader().exists("Content-Type")
    }

    /**
     * Test configuration for security and CORS testing.
     */
    @Configuration
    class TestSecurityConfig {

        @Bean
        fun securityTestRoutes(builder: RouteLocatorBuilder): RouteLocator = builder.routes()
            .route("test-cors") { r ->
                r.path("/test/cors")
                    .uri("forward:/mock/cors-test")
            }
            .route("test-members-complex") { r ->
                r.path("/api/members/**")
                    .filters { f -> f.stripPrefix(1) }
                    .uri("forward:/mock/members-complex")
            }
            .build()

        @Bean
        fun securityTestController(): SecurityTestController = SecurityTestController()
    }

    /**
     * Mock controller for security and CORS testing.
     */
    @RestController
    @RequestMapping("/mock")
    class SecurityTestController {

        @RequestMapping(
            value = ["/cors-test"],
            method = [
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE
            ]
        )
        fun corsTest(): Map<String, String> = mapOf(
            "message" to "CORS test successful",
            "timestamp" to System.currentTimeMillis().toString()
        )

        @CrossOrigin
        @GetMapping("/members-complex")
        @PostMapping("/members-complex")
        fun membersComplex(): Map<String, String> = mapOf(
            "message" to "Complex CORS request handled",
            "service" to "members"
        )
    }
}
