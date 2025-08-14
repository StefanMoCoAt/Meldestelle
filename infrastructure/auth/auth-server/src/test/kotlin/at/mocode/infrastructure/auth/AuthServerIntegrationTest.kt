package at.mocode.infrastructure.auth

import at.mocode.infrastructure.auth.client.JwtService
import at.mocode.infrastructure.auth.client.model.BerechtigungE
import at.mocode.infrastructure.auth.config.AuthServerConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource

/**
 * Minimal integration tests for the Auth Server.
 * Tests essential functionality without full Spring Boot context complexity.
 * Focuses on core service integration and configuration validation.
 *
 * This implements "Option 1: Minimale Integration Tests" focusing on essentials
 * without vollständige Spring Boot Konfiguration.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [AuthServerConfiguration::class]
)
@TestPropertySource(properties = [
    "auth.jwt.secret=test-secret-for-testing-only-at-least-512-bits-long-for-hmac512-algorithm",
    "auth.jwt.issuer=test-issuer",
    "auth.jwt.audience=test-audience",
    "auth.jwt.expiration=60",
    "spring.main.web-application-type=none",
    "logging.level.org.springframework.security=WARN"
])
class AuthServerIntegrationTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var jwtService: JwtService

    private lateinit var testToken: String

    @BeforeEach
    fun setUp() {
        testToken = jwtService.generateToken(
            userId = "test-user-123",
            username = "testuser",
            permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.VEREIN_READ)
        )
    }

    // ========== Core Service Integration Tests ==========

    @Test
    fun `application context should load with minimal configuration`() {
        // Verify that the Spring context loads successfully
        assertNotNull(applicationContext)
        assertTrue(applicationContext.beanDefinitionCount > 0)

        println("[DEBUG_LOG] Application context loaded successfully")
        println("[DEBUG_LOG] Bean count: ${applicationContext.beanDefinitionCount}")
    }

    @Test
    fun `JwtService should be properly configured as Spring bean`() {
        // Verify that JwtService is available as a Spring bean
        assertTrue(applicationContext.containsBean("jwtService"))
        assertNotNull(jwtService)
        assertInstanceOf(JwtService::class.java, jwtService)

        println("[DEBUG_LOG] JwtService bean configured successfully")
    }

    @Test
    fun `JWT service should generate valid tokens`() {
        // Test token generation functionality
        val token = jwtService.generateToken(
            userId = "integration-test-user",
            username = "inttest",
            permissions = listOf(BerechtigungE.PERSON_CREATE, BerechtigungE.PFERD_READ)
        )

        assertNotNull(token)
        assertTrue(token.isNotEmpty())

        // Verify token can be validated
        val validationResult = jwtService.validateToken(token)
        assertTrue(validationResult.isSuccess)
        assertEquals(true, validationResult.getOrNull())

        println("[DEBUG_LOG] Token generated and validated successfully")
    }

    @Test
    fun `JWT service should extract user information correctly`() {
        // Test user ID extraction
        val userIdResult = jwtService.getUserIdFromToken(testToken)
        assertTrue(userIdResult.isSuccess)
        assertEquals("test-user-123", userIdResult.getOrNull())

        // Test permissions extraction
        val permissionsResult = jwtService.getPermissionsFromToken(testToken)
        assertTrue(permissionsResult.isSuccess)
        val permissions = permissionsResult.getOrNull()!!
        assertEquals(2, permissions.size)
        assertTrue(permissions.contains(BerechtigungE.PERSON_READ))
        assertTrue(permissions.contains(BerechtigungE.VEREIN_READ))

        println("[DEBUG_LOG] User information extracted correctly")
        println("[DEBUG_LOG] User ID: ${userIdResult.getOrNull()}")
        println("[DEBUG_LOG] Permissions: $permissions")
    }

    @Test
    fun `JWT service should handle invalid tokens properly`() {
        val invalidToken = "invalid.jwt.token"

        // Validation should fail
        val validationResult = jwtService.validateToken(invalidToken)
        assertTrue(validationResult.isFailure)

        // User ID extraction should fail
        val userIdResult = jwtService.getUserIdFromToken(invalidToken)
        assertTrue(userIdResult.isFailure)

        // Permissions extraction should fail
        val permissionsResult = jwtService.getPermissionsFromToken(invalidToken)
        assertTrue(permissionsResult.isFailure)

        println("[DEBUG_LOG] Invalid token handling works correctly")
    }

    // ========== Configuration Validation Tests ==========

    @Test
    fun `configuration properties should be properly loaded`() {
        // Test that JWT configuration is loaded correctly
        val jwtProperties = applicationContext.getBean(AuthServerConfiguration.JwtProperties::class.java)
        assertNotNull(jwtProperties)
        assertEquals("test-issuer", jwtProperties.issuer)
        assertEquals("test-audience", jwtProperties.audience)
        assertEquals(60L, jwtProperties.expiration)

        println("[DEBUG_LOG] Configuration properties loaded correctly")
        println("[DEBUG_LOG] Issuer: ${jwtProperties.issuer}")
        println("[DEBUG_LOG] Audience: ${jwtProperties.audience}")
        println("[DEBUG_LOG] Expiration: ${jwtProperties.expiration}")
    }

    @Test
    fun `essential beans should be properly configured`() {
        // Verify that essential beans for auth functionality are available
        val beanNames = applicationContext.beanDefinitionNames.toList()

        // Check for JWT service bean
        assertTrue(applicationContext.containsBean("jwtService")) {
            "JwtService bean should be configured"
        }

        // Check for configuration bean
        assertTrue(beanNames.any { it.contains("authServerConfiguration") }) {
            "AuthServerConfiguration bean should be configured"
        }

        println("[DEBUG_LOG] Essential beans configured successfully")
        println("[DEBUG_LOG] Auth-related beans: ${beanNames.filter { it.contains("jwt") || it.contains("auth") }}")
    }

    @Test
    fun `JWT configuration integration should work end-to-end`() {
        // Test the complete flow from configuration to token operations
        val userId = "end-to-end-test"
        val username = "e2etest"
        val permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.PERSON_CREATE)

        // Generate token
        val token = jwtService.generateToken(userId, username, permissions)
        assertNotNull(token)
        assertTrue(token.isNotEmpty())

        // Validate token
        val isValid = jwtService.validateToken(token)
        assertTrue(isValid.isSuccess)

        // Extract and verify data
        val extractedUserId = jwtService.getUserIdFromToken(token).getOrNull()
        val extractedPermissions = jwtService.getPermissionsFromToken(token).getOrElse { emptyList() }

        assertEquals(userId, extractedUserId)
        assertEquals(2, extractedPermissions.size)
        assertTrue(extractedPermissions.containsAll(permissions))

        println("[DEBUG_LOG] End-to-end test completed successfully")
        println("[DEBUG_LOG] Token validation: ${isValid.isSuccess}")
        println("[DEBUG_LOG] Extracted user: $extractedUserId")
        println("[DEBUG_LOG] Extracted permissions: $extractedPermissions")
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

    // ========== Service Functionality Tests ==========

    @Test
    fun `JWT service should handle different permission combinations`() {
        // Test various permission combinations
        val testCases = listOf(
            emptyList(),
            listOf(BerechtigungE.PERSON_READ),
            listOf(BerechtigungE.PERSON_READ, BerechtigungE.PERSON_CREATE),
            BerechtigungE.entries
        )

        testCases.forEach { permissions ->
            val token = jwtService.generateToken("test-user", "test", permissions)
            val validationResult = jwtService.validateToken(token)
            val extractedPermissions = jwtService.getPermissionsFromToken(token).getOrElse { emptyList() }

            assertTrue(validationResult.isSuccess)
            assertEquals(permissions.size, extractedPermissions.size)
            assertTrue(extractedPermissions.containsAll(permissions))
        }

        println("[DEBUG_LOG] Different permission combinations handled correctly")
    }

    @Test
    fun `JWT service should be thread-safe for concurrent access`() {
        // Test concurrent token operations
        val threads = (1..5).map { threadIndex ->
            Thread {
                repeat(10) { iteration ->
                    val token = jwtService.generateToken("user-$threadIndex-$iteration", "test", listOf(BerechtigungE.PERSON_READ))
                    val isValid = jwtService.validateToken(token).isSuccess
                    assertTrue(isValid)
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        println("[DEBUG_LOG] Concurrent access test completed successfully")
    }
}
