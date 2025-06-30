package at.mocode

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwaggerTest {

    @Test
    fun testSwaggerUIEndpoint() = testApplication {
        application {
            module()
        }

        client.get("/swagger").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("swagger", ignoreCase = true))
        }
    }

    @Test
    fun testOpenAPIEndpoint() = testApplication {
        application {
            module()
        }

        client.get("/openapi").apply {
            assertEquals(HttpStatusCode.OK, status)
            val content = bodyAsText()
            println("[DEBUG_LOG] OpenAPI endpoint response: $content")
            // Check if it's a JSON response instead of YAML
            assertTrue(content.isNotEmpty(), "OpenAPI response should not be empty")
            // More flexible checks
            assertTrue(content.contains("openapi") || content.contains("swagger"), "Response should contain OpenAPI or Swagger content")
        }
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
    fun testAPIInfoEndpoint() = testApplication {
        application {
            module()
        }

        client.get("/api").apply {
            assertEquals(HttpStatusCode.OK, status)
            // The response should contain some application info
            assertTrue(bodyAsText().isNotEmpty())
        }
    }
}
