package at.mocode.infrastructure.gateway

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Basic test to verify that the Gateway application context loads successfully.
 * Uses test profile to disable production filters and external dependencies.
 */
@SpringBootTest(
    classes = [GatewayApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        // Disable all external dependencies for context loading test
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "spring.cloud.loadbalancer.enabled=false",
        // Disable circuit breaker for tests
        "resilience4j.circuitbreaker.configs.default.registerHealthIndicator=false",
        "management.health.circuitbreakers.enabled=false",
        // Disable custom security and filters
        "gateway.security.jwt.enabled=false",
        // Use reactive web application type
        "spring.main.web-application-type=reactive",
        // Disable gateway discovery
        "spring.cloud.gateway.discovery.locator.enabled=false",
        // Disable actuator security
        "management.security.enabled=false",
        // Set random port
        "server.port=0"
    ]
)
@ActiveProfiles("test")
class GatewayApplicationTests {

    @Test
    fun contextLoads() {
        // This test passes if the Spring application context loads successfully
        // without any configuration errors or missing bean dependencies
    }
}
