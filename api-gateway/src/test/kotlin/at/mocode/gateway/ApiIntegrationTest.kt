package at.mocode.gateway

import at.mocode.dto.base.BaseDto
import at.mocode.gateway.routing.ApiGatewayInfo
import at.mocode.gateway.routing.HealthStatus
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.*

/**
 * Integration tests for the API Gateway.
 *
 * These tests verify that all API endpoints are working correctly
 * and that the OpenAPI/Swagger integration is functioning properly.
 *
 * Tests are organized into nested classes by functionality area.
 */
class ApiIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Helper function to verify common BaseDto structure
     */
    private fun verifyBaseDtoStructure(responseText: String) {
        assertTrue(responseText.contains("\"success\""), "Response should contain 'success' field")
        assertTrue(responseText.contains("\"data\""), "Response should contain 'data' field")
        assertTrue(responseText.contains("\"message\""), "Response should contain 'message' field")
    }

    /**
     * Tests for core API Gateway functionality
     */
    @Nested
    @DisplayName("Core API Gateway Tests")
    inner class CoreApiTests {
        @Test
        fun testApiGatewayInfo() = testApplication {
            application {
                module()
            }

            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status, "Status should be OK")
                val responseText = bodyAsText()
                assertTrue(responseText.contains("Meldestelle API Gateway"), "Response should contain gateway name")

                // Parse response as BaseDto
                val response = json.decodeFromString<BaseDto<ApiGatewayInfo>>(responseText)
                assertTrue(response.success, "Response should indicate success")
                assertNotNull(response.data, "Response data should not be null")
                assertEquals("Meldestelle API Gateway", response.data!!.name, "Gateway name should match")
                assertEquals("1.0.0", response.data!!.version, "Gateway version should match")

                // Verify all expected contexts are available
                val expectedContexts = listOf("authentication", "master-data", "horse-registry")
                expectedContexts.forEach { context ->
                    assertTrue(response.data!!.availableContexts.contains(context),
                        "Available contexts should contain $context")
                }

                // Verify BaseDto structure
                verifyBaseDtoStructure(responseText)
            }
        }

        @Test
        fun testHealthCheck() = testApplication {
            application {
                module()
            }

            client.get("/health").apply {
                assertEquals(HttpStatusCode.OK, status, "Health check status should be OK")
                val responseText = bodyAsText()

                // Parse response as BaseDto
                val response = json.decodeFromString<BaseDto<HealthStatus>>(responseText)
                assertTrue(response.success, "Health check response should indicate success")
                assertNotNull(response.data, "Health check data should not be null")
                assertEquals("UP", response.data!!.status, "Health status should be UP")

                // Verify all expected contexts are available in health check
                val expectedContexts = listOf("authentication", "master-data", "horse-registry")
                expectedContexts.forEach { context ->
                    assertTrue(response.data!!.contexts.containsKey(context),
                        "Health contexts should contain $context")
                }

                // Verify BaseDto structure
                verifyBaseDtoStructure(responseText)
            }
        }

        @Test
        fun testNotFoundEndpoint() = testApplication {
            application {
                module()
            }

            client.get("/nonexistent").apply {
                assertEquals(HttpStatusCode.NotFound, status, "Non-existent endpoint should return 404")
                val responseText = bodyAsText()
                assertTrue(responseText.contains("Endpoint not found"),
                    "Response should indicate endpoint not found")

                // Verify error response format
                assertTrue(responseText.contains("\"success\":false"),
                    "Error response should have success=false")
            }
        }

        @Test
        fun testInvalidMethod() = testApplication {
            application {
                module()
            }

            client.delete("/").apply {
                // Either method not allowed or not found is acceptable
                assertTrue(
                    status == HttpStatusCode.MethodNotAllowed || status == HttpStatusCode.NotFound,
                    "Invalid method should return 405 Method Not Allowed or 404 Not Found"
                )
            }
        }
    }

    /**
     * Tests for API documentation and Swagger UI
     */
    @Nested
    @DisplayName("Documentation Tests")
    inner class DocumentationTests {
        @Test
        fun testApiDocumentation() = testApplication {
            application {
                module()
            }

            client.get("/api").apply {
                assertEquals(HttpStatusCode.OK, status, "API documentation status should be OK")
                val responseText = bodyAsText()

                // Verify documentation contains expected sections
                val expectedSections = listOf(
                    "Meldestelle Self-Contained Systems API",
                    "Authentication Context",
                    "Master Data Context",
                    "Horse Registry Context"
                )

                expectedSections.forEach { section ->
                    assertTrue(responseText.contains(section),
                        "API documentation should contain section: $section")
                }
            }
        }

        @Test
        fun testSwaggerUI() = testApplication {
            application {
                module()
            }

            client.get("/swagger").apply {
                // Swagger UI should be accessible (might return HTML or redirect)
                assertTrue(
                    status.isSuccess() || status == HttpStatusCode.Found,
                    "Swagger UI should be accessible or redirect"
                )

                // If it's HTML, it should contain Swagger-related content
                if (status.isSuccess()) {
                    val responseText = bodyAsText()
                    assertTrue(
                        responseText.contains("swagger") || responseText.contains("openapi"),
                        "Swagger UI response should contain swagger-related content"
                    )
                }
            }
        }
    }

    /**
     * Tests for API technical features like CORS and content negotiation
     */
    @Nested
    @DisplayName("API Technical Features")
    inner class TechnicalFeatureTests {
        @Test
        fun testCorsHeaders() = testApplication {
            application {
                module()
            }

            // Test preflight request
            client.options("/") {
                header(HttpHeaders.Origin, "http://localhost:3000")
                header(HttpHeaders.AccessControlRequestMethod, "GET")
            }.apply {
                assertTrue(status.isSuccess(), "CORS preflight request should succeed")

                // Verify CORS headers
                assertTrue(
                    headers.contains(HttpHeaders.AccessControlAllowOrigin),
                    "Response should contain Access-Control-Allow-Origin header"
                )
                assertTrue(
                    headers.contains(HttpHeaders.AccessControlAllowMethods),
                    "Response should contain Access-Control-Allow-Methods header"
                )
            }

            // Test actual request with Origin header
            client.get("/") {
                header(HttpHeaders.Origin, "http://localhost:3000")
            }.apply {
                assertEquals(HttpStatusCode.OK, status, "CORS actual request should succeed")
                assertTrue(
                    headers.contains(HttpHeaders.AccessControlAllowOrigin),
                    "Response should contain Access-Control-Allow-Origin header"
                )
            }
        }

        @Test
        fun testContentNegotiation() = testApplication {
            application {
                module()
            }

            // Test JSON content type
            client.get("/") {
                header(HttpHeaders.Accept, "application/json")
            }.apply {
                assertEquals(HttpStatusCode.OK, status, "Content negotiation request should succeed")
                assertEquals(
                    ContentType.Application.Json.withCharset(Charsets.UTF_8),
                    contentType(),
                    "Response content type should be application/json"
                )
            }

            // Test with no Accept header (should default to JSON)
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status, "Default content type request should succeed")
                assertEquals(
                    ContentType.Application.Json.withCharset(Charsets.UTF_8),
                    contentType(),
                    "Default response content type should be application/json"
                )
            }
        }
    }

    /**
     * Tests for Master Data endpoints
     */
    @Nested
    @DisplayName("Master Data Endpoints")
    inner class MasterDataTests {
        @Test
        fun testCountriesEndpoint() = testApplication {
            application {
                module()
            }

            client.get("/api/masterdata/countries").apply {
                assertEquals(HttpStatusCode.OK, status, "Countries endpoint should return OK")
                val responseText = bodyAsText()

                // Verify response format
                verifyBaseDtoStructure(responseText)
                assertTrue(responseText.contains("\"success\":true"),
                    "Response should indicate success")
            }
        }

        @Test
        fun testActiveCountriesEndpoint() = testApplication {
            application {
                module()
            }

            client.get("/api/masterdata/countries/active").apply {
                assertEquals(HttpStatusCode.OK, status, "Active countries endpoint should return OK")
                val responseText = bodyAsText()

                // Verify response format
                verifyBaseDtoStructure(responseText)
                assertTrue(responseText.contains("\"success\":true"),
                    "Response should indicate success")
            }
        }

        @Test
        fun testCountriesWithPagination() = testApplication {
            application {
                module()
            }

            client.get("/api/masterdata/countries?limit=5&offset=0").apply {
                assertEquals(HttpStatusCode.OK, status, "Countries with pagination should return OK")
                val responseText = bodyAsText()

                // Verify response format
                verifyBaseDtoStructure(responseText)
                assertTrue(responseText.contains("\"success\":true"),
                    "Response should indicate success")
            }
        }
    }

    /**
     * Tests for Horse Registry endpoints
     */
    @Nested
    @DisplayName("Horse Registry Endpoints")
    inner class HorseRegistryTests {
        @Test
        fun testHorsesEndpointRequiresAuth() = testApplication {
            application {
                module()
            }

            client.get("/api/horses").apply {
                // Should return unauthorized or redirect to login
                assertTrue(
                    status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Found,
                    "Horses endpoint should require authentication"
                )
            }
        }

        @Test
        fun testHorseStatsEndpointRequiresAuth() = testApplication {
            application {
                module()
            }

            client.get("/api/horses/stats").apply {
                // Should require authentication
                assertTrue(
                    status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Found,
                    "Horse stats endpoint should require authentication"
                )
            }
        }
    }

    /**
     * Tests for Authentication endpoints
     */
    @Nested
    @DisplayName("Authentication Endpoints")
    inner class AuthenticationTests {
        @Test
        fun testRegistrationEndpoint() = testApplication {
            application {
                module()
            }

            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody("""
                    {
                        "email": "test@example.com",
                        "password": "TestPassword123!",
                        "firstName": "Test",
                        "lastName": "User",
                        "phoneNumber": "+43123456789"
                    }
                """.trimIndent())
            }.apply {
                // Should process the request (might fail due to validation or database issues)
                // But should not return server error
                assertTrue(status.value in 200..499,
                    "Registration endpoint should process request without server error")

                // If it's a client error, it should be due to validation or existing user
                if (status.value in 400..499) {
                    val responseText = bodyAsText()
                    assertTrue(
                        responseText.contains("validation") ||
                        responseText.contains("exist") ||
                        responseText.contains("already"),
                        "Client error should be due to validation or existing user"
                    )
                }
            }
        }

        @Test
        fun testLoginEndpoint() = testApplication {
            application {
                module()
            }

            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""
                    {
                        "email": "test@example.com",
                        "password": "TestPassword123!"
                    }
                """.trimIndent())
            }.apply {
                // Should process the request without server error
                assertTrue(status.value in 200..499,
                    "Login endpoint should process request without server error")

                // If it's a client error, it should be due to invalid credentials
                if (status.value in 400..499) {
                    val responseText = bodyAsText()
                    assertTrue(
                        responseText.contains("invalid") ||
                        responseText.contains("credentials") ||
                        responseText.contains("unauthorized"),
                        "Client error should be due to invalid credentials"
                    )
                }
            }
        }

        @Test
        fun testInvalidLoginRequest() = testApplication {
            application {
                module()
            }

            // Test with missing password
            client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""
                    {
                        "email": "test@example.com"
                    }
                """.trimIndent())
            }.apply {
                // Should return a client error
                assertTrue(status.value in 400..499,
                    "Invalid login request should return client error")

                val responseText = bodyAsText()
                assertTrue(
                    responseText.contains("validation") ||
                    responseText.contains("missing") ||
                    responseText.contains("required"),
                    "Error should indicate validation failure or missing field"
                )
            }
        }
    }
}
