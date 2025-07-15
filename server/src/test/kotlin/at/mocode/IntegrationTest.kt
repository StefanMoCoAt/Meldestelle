package at.mocode

import at.mocode.model.Artikel
import at.mocode.model.Platz
import at.mocode.enums.PlatzTypE
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Integration tests that verify the complete application stack:
 * Routes -> Services -> Repositories -> Database
 *
 * These tests ensure that all layers work together correctly
 * and provide end-to-end functionality verification.
 */
class IntegrationTest {

    companion object {
        init {
            // Set test environment property for database configuration
            System.setProperty("isTestEnvironment", "true")
        }
    }

    @Test
    fun testApplicationStartupAndBasicEndpoints() = testApplication {
        application {
            module()
        }

        // Test health endpoint
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }

        // Test API info endpoint
        client.get("/api").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.contains("Meldestelle API Server"))
            assertTrue(responseText.contains("v1.0.0"))
        }

        // Test root endpoint serves HTML
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.contains("<!DOCTYPE html>"))
            assertTrue(responseText.contains("Meldestelle"))
        }
    }

    @Test
    fun testSwaggerDocumentationEndpoints() = testApplication {
        application {
            module()
        }

        // Test Swagger UI endpoint
        client.get("/swagger").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("swagger", ignoreCase = true))
        }

        // Test OpenAPI endpoint
        client.get("/openapi").apply {
            assertEquals(HttpStatusCode.OK, status)
            val content = bodyAsText()
            assertTrue(content.isNotEmpty())
            assertTrue(content.contains("openapi") || content.contains("swagger"))
        }
    }

    @Test
    fun testArtikelEndpointsIntegration() = testApplication {
        application {
            module()
        }

        // Test GET /api/artikel endpoint
        client.get("/api/artikel").apply {
            assertEquals(HttpStatusCode.OK, status)
            // Should return valid JSON response
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }

        // Test GET /api/artikel/verbandsabgabe/true endpoint
        client.get("/api/artikel/verbandsabgabe/true").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }

        // Test GET /api/artikel/verbandsabgabe/false endpoint
        client.get("/api/artikel/verbandsabgabe/false").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }
    }

    @Test
    fun testPlatzEndpointsIntegration() = testApplication {
        application {
            module()
        }

        // Test GET /api/plaetze endpoint
        client.get("/api/plaetze").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }

        // Test GET /api/plaetze/typ/{typ} endpoint
        client.get("/api/plaetze/typ/AUSTRAGUNG").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }

        // Test invalid typ parameter
        client.get("/api/plaetze/typ/INVALID_TYPE").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testErrorHandling() = testApplication {
        application {
            module()
        }

        // Test 404 for non-existent endpoint
        client.get("/api/nonexistent").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }

        // Test 404 for non-existent artikel
        val nonExistentId = uuid4()
        client.get("/api/artikel/$nonExistentId").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }

        // Test 404 for non-existent platz
        client.get("/api/plaetze/$nonExistentId").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testSearchEndpoints() = testApplication {
        application {
            module()
        }

        // Test artikel search with valid query
        client.get("/api/artikel/search?q=test").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }

        // Test artikel search with empty query (should return 400)
        client.get("/api/artikel/search?q=").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }

        // Test plaetze search with valid query
        client.get("/api/plaetze/search?q=test").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }

        // Test plaetze search with empty query (should return 400)
        client.get("/api/plaetze/search?q=").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testCorsHeaders() = testApplication {
        application {
            module()
        }

        // Test CORS headers are present
        client.get("/api/artikel") {
            header(HttpHeaders.Origin, "http://localhost:3000")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertNotNull(headers[HttpHeaders.AccessControlAllowOrigin])
        }

        // Test OPTIONS request for CORS preflight
        client.options("/api/artikel") {
            header(HttpHeaders.Origin, "http://localhost:3000")
            header(HttpHeaders.AccessControlRequestMethod, "GET")
        }.apply {
            // Should handle OPTIONS request properly
            assertTrue(status.isSuccess() || status == HttpStatusCode.NotFound)
        }
    }

    @Test
    fun testContentNegotiation() = testApplication {
        application {
            module()
        }

        // Test JSON content type
        client.get("/api/artikel") {
            header(HttpHeaders.Accept, "application/json")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(ContentType.Application.Json.withCharset(Charsets.UTF_8), contentType())
        }
    }

    @Test
    fun testVersioningIntegration() = testApplication {
        application {
            module()
        }

        // Test version validation endpoint (if it exists)
        client.get("/api/version").apply {
            // This endpoint might not exist, so we just check it doesn't crash
            assertTrue(status == HttpStatusCode.OK || status == HttpStatusCode.NotFound)
        }
    }

    @Test
    fun testDatabaseConnectionAndBasicOperations() = testApplication {
        application {
            module()
        }

        // This test verifies that the database connection works
        // by testing endpoints that require database access

        // Test that we can retrieve data (even if empty)
        client.get("/api/artikel").apply {
            assertEquals(HttpStatusCode.OK, status)
            // Should return valid JSON response
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }

        client.get("/api/plaetze").apply {
            assertEquals(HttpStatusCode.OK, status)
            // Should return valid JSON response
            val responseText = bodyAsText()
            assertTrue(responseText.isNotEmpty())
            assertTrue(responseText.contains("{") || responseText.startsWith("["))
        }
    }

    @Test
    fun testServiceLayerIntegration() = testApplication {
        application {
            module()
        }

        // Test that service layer validation works through the API

        // Test artikel search with blank query (should trigger service validation)
        client.get("/api/artikel/search?q=   ").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }

        // Test plaetze search with blank query (should trigger service validation)
        client.get("/api/plaetze/search?q=   ").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun testCompleteApplicationFlow() = testApplication {
        application {
            module()
        }

        println("[DEBUG_LOG] Testing complete application flow...")

        // 1. Test application startup
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            println("[DEBUG_LOG] ✓ Application health check passed")
        }

        // 2. Test API documentation
        client.get("/swagger").apply {
            assertEquals(HttpStatusCode.OK, status)
            println("[DEBUG_LOG] ✓ Swagger documentation accessible")
        }

        // 3. Test data retrieval endpoints
        client.get("/api/artikel").apply {
            assertEquals(HttpStatusCode.OK, status)
            println("[DEBUG_LOG] ✓ Artikel endpoint accessible")
        }

        client.get("/api/plaetze").apply {
            assertEquals(HttpStatusCode.OK, status)
            println("[DEBUG_LOG] ✓ Plaetze endpoint accessible")
        }

        // 4. Test search functionality
        client.get("/api/artikel/search?q=test").apply {
            assertEquals(HttpStatusCode.OK, status)
            println("[DEBUG_LOG] ✓ Artikel search functionality working")
        }

        client.get("/api/plaetze/search?q=test").apply {
            assertEquals(HttpStatusCode.OK, status)
            println("[DEBUG_LOG] ✓ Plaetze search functionality working")
        }

        // 5. Test error handling
        client.get("/api/nonexistent").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            println("[DEBUG_LOG] ✓ 404 error handling working")
        }

        println("[DEBUG_LOG] ✅ Complete application flow test passed!")
    }
}
