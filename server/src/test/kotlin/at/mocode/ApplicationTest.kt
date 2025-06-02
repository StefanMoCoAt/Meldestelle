package at.mocode

import at.mocode.model.Nennung
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Ktor ist erreichbar!"))
        assertTrue(response.bodyAsText().contains("Go to API Test Page"))
    }

    @Test
    fun testApiDebug() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/debug")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Debug endpoint is working"))
    }

    @Test
    fun testApiNennungGet() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/nennung")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("GET /api/nennung endpoint is working"))
    }

    @Test
    fun testApiNennungPost() = testApplication {
        application {
            module()
        }

        val testNennung = Nennung(
            riderName = "Test Rider",
            horseName = "Test Horse",
            email = "test@example.com",
            phone = "123456789",
            selectedEvents = listOf("Event 1", "Event 2"),
            comments = "Test comments"
        )

        val response = client.post("/api/nennung") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(testNennung))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Nennung erfolgreich empfangen"))
    }
}
