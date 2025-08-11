package at.mocode.infrastructure.gateway

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import java.time.Duration
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.boot.test.context.TestConfiguration

@SpringBootTest(
    classes = [GatewayApplication::class],
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = [
        // Use a random port and disable discovery/consul for the test
        "server.port=0",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        // Disable security autoconfiguration for tests
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration",
        // Force a reactive web application so that Spring Cloud Gateway auto-config activates
        "spring.main.web-application-type=reactive",
        // Gateway discovery locator off; we use explicit test routes
        "spring.cloud.gateway.discovery.locator.enabled=false"
    ]
)
@AutoConfigureWebTestClient
@Import(GatewayApplicationTests.TestRoutes::class, GatewayApplicationTests.InternalHelloController::class, GatewayApplicationTests.TestSecurityConfig::class)
class GatewayApplicationTests {

    @Autowired
    lateinit var client: WebTestClient

    @Autowired
    lateinit var routeLocator: RouteLocator

    @Test
    fun contextLoads() {
        // If the application context fails to load, this test will fail.
    }

    @Test
    fun forwardRouteShouldReturnResponseFromInternalController() {
        client.get()
            .uri("/hello")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("OK")
    }

    @RestController
    class InternalHelloController {
        @GetMapping("/internal/hello")
        fun hello(): String = "OK"
    }

    @Configuration
    class TestRoutes {
        @Bean
        fun routeLocator(builder: RouteLocatorBuilder): RouteLocator = builder.routes()
            .route("test-forward") {
                it.path("/hello").uri("forward:/internal/hello")
            }
            .build()
    }

    @TestConfiguration
    class TestSecurityConfig {
        @Bean
        fun springSecurityFilterChain(): org.springframework.security.web.server.SecurityWebFilterChain =
            org.springframework.security.config.web.server.ServerHttpSecurity
                .http()
                .csrf { it.disable() }
                .authorizeExchange { exchanges -> exchanges.anyExchange().permitAll() }
                .build()
    }
}
