package at.mocode.infrastructure.auth

import at.mocode.infrastructure.auth.config.AuthServerConfiguration
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

/**
 * Minimal integration tests for the Auth Server.
 *
 * Note: Custom JWT handling has been removed. Authentication is now fully handled
 * by Keycloak via OAuth2 Resource Server. This test verifies the basic Spring
 * context loads correctly.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [AuthServerConfiguration::class]
)
@TestPropertySource(properties = [
    "spring.main.web-application-type=none",
    "logging.level.org.springframework.security=WARN"
])
class AuthServerIntegrationTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `application context should load with minimal configuration`() {
        // Verify that the Spring context loads successfully
        assertNotNull(applicationContext)
        assertTrue(applicationContext.beanDefinitionCount > 0)

        println("[DEBUG_LOG] Application context loaded successfully")
        println("[DEBUG_LOG] Bean count: ${applicationContext.beanDefinitionCount}")
    }

    @Test
    fun `configuration bean should be present`() {
        // Verify that essential beans are available
        val beanNames = applicationContext.beanDefinitionNames.toList()

        // Check for configuration bean
        assertTrue(beanNames.any { it.contains("authServerConfiguration") }) {
            "AuthServerConfiguration bean should be configured"
        }

        println("[DEBUG_LOG] Essential beans configured successfully")
        println("[DEBUG_LOG] Auth-related beans: ${beanNames.filter { it.contains("auth") }}")
    }

    @Test
    fun `application context should have minimal footprint`() {
        // Verify that we're running with minimal configuration
        val beanCount = applicationContext.beanDefinitionCount
        assertTrue(beanCount < 100) {
            "Bean count should be minimal (was $beanCount)"
        }

        // Verify no web-related beans are loaded
        val webBeans = applicationContext.beanDefinitionNames.filter {
            it.contains("mvc") || it.contains("servlet") || it.contains("tomcat")
        }
        assertTrue(webBeans.isEmpty()) {
            "No web-related beans should be loaded: $webBeans"
        }

        println("[DEBUG_LOG] Minimal application context verified")
        println("[DEBUG_LOG] Total bean count: $beanCount")
        println("[DEBUG_LOG] Web-related beans: $webBeans")
    }
}
