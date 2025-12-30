package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.config.TestSecurityConfig
import at.mocode.infrastructure.gateway.support.GatewayTestContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Tests for Gateway routing functionality.
 * Uses mock backend services to test route forwarding.
 */
@GatewayTestContext
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
    fun `auth route is not configured anymore`() {
        webTestClient.post()
            .uri("/api/auth/login")
            .exchange()
            .expectStatus().isNotFound
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
            // no dedicated auth route anymore – clients should talk to Keycloak directly
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

        // removed auth mock endpoints – not needed anymore

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
