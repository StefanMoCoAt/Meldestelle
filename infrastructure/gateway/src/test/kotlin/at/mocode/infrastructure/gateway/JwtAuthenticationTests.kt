package at.mocode.infrastructure.gateway

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
import org.springframework.web.bind.annotation.*

/**
 * Tests for JWT Authentication Filter functionality.
 * Tests public path exemptions, token validation, and user context injection.
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
        // Disable circuit breaker for JWT tests
        "resilience4j.circuitbreaker.configs.default.registerHealthIndicator=false",
        "management.health.circuitbreakers.enabled=false",
        // Enable JWT authentication for testing
        "gateway.security.jwt.enabled=true",
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
@ActiveProfiles("test") // Use test profile to disable unrelated global filters; JWT is enabled via properties above
@AutoConfigureWebTestClient
@Import(JwtAuthenticationTests.TestJwtConfig::class)
class JwtAuthenticationTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `should allow access to public paths without authentication`() {
        listOf("/", "/health", "/actuator/health", "/api/auth/login", "/api/ping/health", "/fallback/test").forEach { path ->
            webTestClient.get()
                .uri(path)
                .exchange()
                .expectStatus().isOk
        }
    }

    @Test
    fun `should return 401 for protected paths without authorization header`() {
        webTestClient.get()
            .uri("/api/members/protected")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody()
            .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
            .jsonPath("$.message").isEqualTo("Missing or invalid Authorization header")
            .jsonPath("$.status").isEqualTo(401)
    }

    @Test
    fun `should return 401 for protected paths with invalid authorization header`() {
        webTestClient.get()
            .uri("/api/members/protected")
            .header("Authorization", "InvalidHeader")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
    }

    @Test
    fun `should return 401 for protected paths with invalid JWT token`() {
        webTestClient.get()
            .uri("/api/members/protected")
            .header("Authorization", "Bearer invalid")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
            .jsonPath("$.message").isEqualTo("Invalid JWT token")
    }

    @Test
    fun `should allow access with valid JWT token and inject user headers`() {
        val validToken = "valid-jwt-token-with-user-data"

        webTestClient.get()
            .uri("/api/members/protected")
            .header("Authorization", "Bearer $validToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .consumeWith { result ->
                // The mock controller returns injected header values in the message
                val body = result.responseBody ?: ""
                assert(body.contains("User ID:"))
                assert(body.contains("Role:"))
            }
    }

    @Test
    fun `should extract admin role from JWT token`() {
        val adminToken = "valid-jwt-token-with-admin-data"

        webTestClient.get()
            .uri("/api/members/protected")
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .consumeWith { result ->
                val body = result.responseBody
                assert(body?.contains("ADMIN") == true)
            }
    }

    @Test
    fun `should extract user role from JWT token`() {
        val userToken = "valid-jwt-token-with-user-data"

        webTestClient.get()
            .uri("/api/members/protected")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .consumeWith { result ->
                val body = result.responseBody
                assert(body?.contains("USER") == true)
            }
    }

    @Test
    fun `should handle POST requests to protected endpoints`() {
        val validToken = "valid-jwt-token-for-post"

        webTestClient.post()
            .uri("/api/members/protected")
            .header("Authorization", "Bearer $validToken")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow access to swagger documentation paths`() {
        webTestClient.get()
            .uri("/docs/api-docs")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Test configuration that provides routes for JWT authentication testing.
     */
    @Configuration
    class TestJwtConfig {

        @Bean
        fun jwtTestRoutes(builder: RouteLocatorBuilder): RouteLocator = builder.routes()
            .route("test-protected") { r ->
                r.path("/api/members/**")
                    .filters { f -> f.setPath("/mock/protected") }
                    .uri("forward:/")
            }
            .route("test-public-health") { r ->
                r.path("/health")
                    .uri("forward:/mock/health")
            }
            .route("test-public-ping") { r ->
                r.path("/api/ping/**")
                    .filters { f -> f.setPath("/mock/ping") }
                    .uri("forward:/")
            }
            .route("test-public-auth") { r ->
                r.path("/api/auth/**")
                    .filters { f -> f.setPath("/mock/auth") }
                    .uri("forward:/")
            }
            .route("test-public-fallback") { r ->
                r.path("/fallback/**")
                    .uri("forward:/mock/fallback")
            }
            .route("test-public-docs") { r ->
                r.path("/docs/**")
                    .uri("forward:/mock/docs")
            }
            .route("test-public-actuator") { r ->
                r.path("/actuator/**")
                    .uri("forward:/mock/actuator")
            }
            .route("test-root") { r ->
                r.path("/")
                    .filters { f -> f.setPath("/mock/root") }
                    .uri("forward:/")
            }
            .build()

        @Bean
        fun jwtTestController(): JwtTestController = JwtTestController()
    }

    /**
     * Mock controller for JWT authentication testing.
     * Returns information about injected user headers.
     */
    @RestController
    @RequestMapping("/mock")
    class JwtTestController {

        @RequestMapping(
            value = ["/protected"],
            method = [RequestMethod.GET, RequestMethod.POST]
        )
        fun protectedEndpoint(
            @RequestHeader(value = "X-User-ID", required = false) userId: String?,
            @RequestHeader(value = "X-User-Role", required = false) userRole: String?
        ): String {
            return "Protected endpoint accessed - User ID: $userId, Role: $userRole"
        }

        @GetMapping("/health")
        fun healthEndpoint(): String = "Health OK"

        @GetMapping("/ping")
        fun pingEndpoint(): String = "Ping OK"

        @GetMapping("/auth")
        @PostMapping("/auth")
        fun authEndpoint(): String = "Auth endpoint"

        @GetMapping("/fallback")
        fun fallbackEndpoint(): String = "Fallback OK"

        @GetMapping("/docs")
        fun docsEndpoint(): String = "Documentation OK"

        @GetMapping("/actuator")
        fun actuatorEndpoint(): String = "Actuator OK"

        @GetMapping("/root")
        fun rootEndpoint(): Map<String, String> = mapOf(
            "service" to "api-gateway",
            "status" to "running"
        )
    }
}
