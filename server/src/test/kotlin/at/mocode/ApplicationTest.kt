package at.mocode

import at.mocode.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * Comprehensive test suite for the Meldestelle Server Application.
 *
 * This test class verifies:
 * - Application startup and initialization
 * - Core routing functionality (health check, root endpoint)
 * - Plugin configuration (CORS, content negotiation, default headers)
 * - Error handling
 * - Basic HTTP functionality
 */
class ApplicationTest {

    companion object {
        init {
            // Set test environment property for database configuration
            // This ensures the application uses H2 in-memory database for testing
            System.setProperty("isTestEnvironment", "true")
        }
    }

    @Test
    fun testApplicationStartup() = testApplication {
        application {
            module()
        }
        // Test that the application starts without errors
        // This test passes if no exceptions are thrown during module initialization
    }

    @Test
    fun testHealthEndpoint() = testApplication {
        application {
            module()
        }
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }
    }

    @Test
    fun testRootEndpoint() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            // The root endpoint now serves the static HTML start page
            assertTrue(responseText.contains("<!DOCTYPE html>"), "Response should contain HTML doctype, but was: ${responseText.take(100)}...")
            assertTrue(responseText.contains("Meldestelle"), "Response should contain 'Meldestelle', but was: ${responseText.take(100)}...")
            assertTrue(responseText.contains("Österreichisches Pferdesport Management System"), "Response should contain system description")
        }
    }

    @Test
    fun testApiInfoEndpoint() = testApplication {
        application {
            module()
        }
        client.get("/api").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            // The API info endpoint format is: "Meldestelle API Server v1.0.0 - Running in development mode"
            assertTrue(responseText.contains("Meldestelle API Server"), "Response should contain 'Meldestelle API Server', but was: $responseText")
            assertTrue(responseText.contains("v1.0.0"), "Response should contain 'v1.0.0', but was: $responseText")
            assertTrue(responseText.contains("development"), "Response should contain 'development', but was: $responseText")
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
            assertTrue(responseText.contains("404: Page Not Found"))
        }
    }

    @Test
    fun testDefaultHeaders() = testApplication {
        application {
            module()
        }
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            // Check that default headers are set
            assertEquals("Ktor", headers["X-Engine"])
            assertEquals("nosniff", headers["X-Content-Type-Options"])
        }
    }

    @Test
    fun testCorsConfiguration() = testApplication {
        application {
            module()
        }
        client.get("/health") {
            header(HttpHeaders.Origin, "http://localhost:3000")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            // Check that CORS headers are present
            assertNotNull(headers[HttpHeaders.AccessControlAllowOrigin])
        }
    }

    @Test
    fun testContentNegotiation() = testApplication {
        application {
            module()
        }
        client.get("/health") {
            header(HttpHeaders.Accept, "application/json")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            // The response should still be text for the health endpoint
            assertEquals("OK", bodyAsText())
        }
    }

    @Test
    fun testOptionsRequest() = testApplication {
        application {
            module()
        }
        client.options("/health") {
            header(HttpHeaders.Origin, "http://localhost:3000")
            header(HttpHeaders.AccessControlRequestMethod, "GET")
        }.apply {
            // OPTIONS requests should be handled by CORS
            assertTrue(status.isSuccess() || status == HttpStatusCode.NotFound)
        }
    }

    @Test
    fun testBasicRoutingWithoutDatabase() = testApplication {
        application {
            // Test routing functionality without full application module
            // This isolates routing from database dependencies
            install(DefaultHeaders) {
                header("X-Engine", "Ktor")
                header("X-Content-Type-Options", "nosniff")
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            configureRouting()
        }

        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }
    }
}
