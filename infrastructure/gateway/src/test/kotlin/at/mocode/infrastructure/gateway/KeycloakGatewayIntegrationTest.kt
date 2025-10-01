package at.mocode.infrastructure.gateway

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("keycloak-integration-test")
@TestPropertySource(properties = [
    "gateway.security.keycloak.enabled=true",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:\${keycloak.port}/realms/meldestelle",
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.consul.enabled=false",
    "spring.cloud.consul.config.enabled=false",
    "spring.cloud.consul.discovery.register=false",
    "spring.cloud.loadbalancer.enabled=false",
    "management.security.enabled=false"
])
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Temporarily disabled due to Bean definition conflicts - needs separate integration test profile")
class KeycloakGatewayIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("keycloak")
            .withUsername("keycloak")
            .withPassword("keycloak")

        @Container
        @JvmStatic
        val keycloak: GenericContainer<*> = GenericContainer("quay.io/keycloak/keycloak:26.0.7")
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withEnv("KC_DB", "postgres")
            .withEnv("KC_DB_URL", "jdbc:postgresql://postgres:5432/keycloak")
            .withEnv("KC_DB_USERNAME", "keycloak")
            .withEnv("KC_DB_PASSWORD", "keycloak")
            .withCommand("start-dev")
            .dependsOn(postgres)
            .waitingFor(
                Wait.forHttp("/health/ready")
                    .forPort(8080)
                    .withStartupTimeout(Duration.ofMinutes(3))
            )
    }

    @Test
    fun `should start with Keycloak integration`() {
        // Basic test to verify containers start correctly
        assert(postgres.isRunning) { "PostgreSQL should be running" }
        assert(keycloak.isRunning) { "Keycloak should be running" }

        val keycloakPort = keycloak.getMappedPort(8080)
        println("Keycloak running on port: $keycloakPort")

        // Test can be extended with actual JWT token validation
    }
}
