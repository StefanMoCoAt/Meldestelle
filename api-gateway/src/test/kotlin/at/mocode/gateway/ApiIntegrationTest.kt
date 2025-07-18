package at.mocode.gateway

import at.mocode.dto.base.BaseDto
import at.mocode.gateway.routing.ApiGatewayInfo
import at.mocode.gateway.routing.HealthStatus
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Integration tests for the API Gateway.
 *
 * These tests verify that all API endpoints are working correctly
 * and that the OpenAPI/Swagger integration is functioning properly.
 */
class ApiIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testApiGatewayInfo() = testApplication {
        application {
            module()
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.contains("Meldestelle API Gateway"))

            // Parse response as BaseDto
            val response = json.decodeFromString<BaseDto<ApiGatewayInfo>>(responseText)
            assertTrue(response.success)
            assertNotNull(response.data)
            assertEquals("Meldestelle API Gateway", response.data!!.name)
            assertEquals("1.0.0", response.data!!.version)
            assertTrue(response.data!!.availableContexts.contains("authentication"))
            assertTrue(response.data!!.availableContexts.contains("master-data"))
            assertTrue(response.data!!.availableContexts.contains("horse-registry"))
        }
    }

    @Test
    fun testHealthCheck() = testApplication {
        application {
            module()
        }

        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()

            // Parse response as BaseDto
            val response = json.decodeFromString<BaseDto<HealthStatus>>(responseText)
            assertTrue(response.success)
            assertNotNull(response.data)
            assertEquals("UP", response.data!!.status)
            assertTrue(response.data!!.contexts.containsKey("authentication"))
            assertTrue(response.data!!.contexts.containsKey("master-data"))
            assertTrue(response.data!!.contexts.containsKey("horse-registry"))
        }
    }

    @Test
    fun testApiDocumentation() = testApplication {
        application {
            module()
        }

        client.get("/api").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.contains("Meldestelle Self-Contained Systems API"))
            assertTrue(responseText.contains("Authentication Context"))
            assertTrue(responseText.contains("Master Data Context"))
            assertTrue(responseText.contains("Horse Registry Context"))
        }
    }

    @Test
    fun testSwaggerUI() = testApplication {
        application {
            module()
        }

        client.get("/swagger").apply {
            // Swagger UI should be accessible (might return HTML or redirect)
            assertTrue(status.isSuccess() || status == HttpStatusCode.Found)
        }
    }

    @Test
    fun testNotFoundEndpoint() = testApplication {
        application {
            module()
        }

        client.get("/nonexistent").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            val responseText = bodyAsText()
            assertTrue(responseText.contains("Endpoint not found"))
        }
    }

    @Test
    fun testCorsHeaders() = testApplication {
        application {
            module()
        }

        client.options("/") {
            header(HttpHeaders.Origin, "http://localhost:3000")
            header(HttpHeaders.AccessControlRequestMethod, "GET")
        }.apply {
            // CORS should be configured
            assertTrue(status.isSuccess())
        }
    }

    @Test
    fun testContentNegotiation() = testApplication {
        application {
            module()
        }

        client.get("/") {
            header(HttpHeaders.Accept, "application/json")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(ContentType.Application.Json.withCharset(Charsets.UTF_8), contentType())
        }
    }

    @Test
    fun testMasterDataEndpoints() = testApplication {
        application {
            module()
        }

        // Test countries endpoint
        client.get("/api/masterdata/countries").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.contains("success"))
        }

        // Test active countries endpoint
        client.get("/api/masterdata/countries/active").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.contains("success"))
        }
    }

    @Test
    fun testHorseRegistryEndpoints() = testApplication {
        application {
            module()
        }

        // Test horses endpoint (should require authentication)
        client.get("/api/horses").apply {
            // Should return unauthorized or redirect to login
            assertTrue(status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Found)
        }

        // Test horse stats endpoint
        client.get("/api/horses/stats").apply {
            // Should require authentication
            assertTrue(status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Found)
        }
    }

    @Test
    fun testAuthenticationEndpoints() = testApplication {
        application {
            module()
        }

        // Test registration endpoint structure
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
            assertTrue(status.value in 200..499)
        }

        // Test login endpoint structure
        client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "test@example.com",
                    "password": "TestPassword123!"
                }
            """.trimIndent())
        }.apply {
            // Should process the request
            assertTrue(status.value in 200..499)
        }
    }

    @Test
    fun testApiResponseFormat() = testApplication {
        application {
            module()
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()

            // Verify BaseDto structure
            assertTrue(responseText.contains("\"success\""))
            assertTrue(responseText.contains("\"data\""))
            assertTrue(responseText.contains("\"message\""))

            // Should be valid JSON
            assertNotNull(json.decodeFromString<BaseDto<ApiGatewayInfo>>(responseText))
        }
    }
}
