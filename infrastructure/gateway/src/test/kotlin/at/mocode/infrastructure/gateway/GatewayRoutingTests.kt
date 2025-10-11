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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Tests for Gateway routing functionality.
 * Uses mock backend services to test route forwarding.
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
        // Disable circuit breaker for routing tests
        "resilience4j.circuitbreaker.configs.default.registerHealthIndicator=false",
        "management.health.circuitbreakers.enabled=false",
        // Disable custom filters for pure routing tests
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
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class, GatewayRoutingTests.TestRoutesConfig::class)
class GatewayRoutingTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `should route members service requests`() {
        webTestClient.get()
            .uri("/api/members/test")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("members-service-mock")
    }

    @Test
    fun `should route horses service requests`() {
        webTestClient.get()
            .uri("/api/horses/test")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("horses-service-mock")
    }

    @Test
    fun `should route events service requests`() {
        webTestClient.get()
            .uri("/api/events/test")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("events-service-mock")
    }

    @Test
    fun `should route masterdata service requests`() {
        webTestClient.get()
            .uri("/api/masterdata/test")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("masterdata-service-mock")
    }

    @Test
    fun `should route auth service requests`() {
        webTestClient.post()
            .uri("/api/auth/login")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("auth-service-mock")
    }

    @Test
    fun `should route ping service requests`() {
        webTestClient.get()
            .uri("/api/ping/health")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("ping-service-mock")
    }

    @Test
    fun `should handle gateway info path request`() {
        webTestClient.get()
            .uri("/gateway-info")
            .exchange()
            .expectStatus().isOk
    }

    /**
     * Test configuration that provides mock backend services and custom routes.
     */
    @Configuration
    class TestRoutesConfig {

        @Bean
        fun testRouteLocator(builder: RouteLocatorBuilder): RouteLocator = builder.routes()
            .route("test-members") { r ->
                r.path("/api/members/**")
                    .filters { f -> f.setPath("/mock/members") }
                    .uri("forward:/")
            }
            .route("test-horses") { r ->
                r.path("/api/horses/**")
                    .filters { f -> f.setPath("/mock/horses") }
                    .uri("forward:/")
            }
            .route("test-events") { r ->
                r.path("/api/events/**")
                    .filters { f -> f.setPath("/mock/events") }
                    .uri("forward:/")
            }
            .route("test-masterdata") { r ->
                r.path("/api/masterdata/**")
                    .filters { f -> f.setPath("/mock/masterdata") }
                    .uri("forward:/")
            }
            .route("test-auth-login") { r ->
                r.path("/api/auth/login")
                    .uri("forward:/mock/auth/login")
            }
            .route("test-ping") { r ->
                r.path("/api/ping/**")
                    .filters { f -> f.setPath("/mock/ping") }
                    .uri("forward:/")
            }
            .route("test-root") { r ->
                r.path("/gateway-info")
                    .uri("forward:/mock/gateway-info")
            }
            .build()

        @Bean
        fun mockBackendController(): MockBackendController = MockBackendController()
    }

    /**
     * Mock backend controller that simulates the responses from actual microservices.
     */
    @RestController
    @RequestMapping("/mock")
    class MockBackendController {

        @GetMapping(value = ["/members", "/members/**"])
        @PostMapping(value = ["/members", "/members/**"])
        fun membersServiceMock(): String = "members-service-mock"

        @GetMapping(value = ["/horses", "/horses/**"])
        @PostMapping(value = ["/horses", "/horses/**"])
        fun horsesServiceMock(): String = "horses-service-mock"

        @GetMapping(value = ["/events", "/events/**"])
        @PostMapping(value = ["/events", "/events/**"])
        fun eventsServiceMock(): String = "events-service-mock"

        @GetMapping(value = ["/masterdata", "/masterdata/**"])
        @PostMapping(value = ["/masterdata", "/masterdata/**"])
        fun masterdataServiceMock(): String = "masterdata-service-mock"

        @GetMapping(value = ["/auth", "/auth/**"])
        @PostMapping(value = ["/auth", "/auth/**"])
        fun authServiceMock(): String = "auth-service-mock"

        @PostMapping("/auth/login")
        fun authLoginPost(): String = "auth-service-mock"

        @GetMapping(value = ["/ping", "/ping/**"])
        @PostMapping(value = ["/ping", "/ping/**"])
        fun pingServiceMock(): String = "ping-service-mock"

        @GetMapping("/gateway-info")
        fun gatewayInfoMock(): Map<String, String> = mapOf(
            "service" to "api-gateway",
            "status" to "running"
        )
    }
}
