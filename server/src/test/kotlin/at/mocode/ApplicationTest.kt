package at.mocode

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.* // Wichtig für testApplication
import kotlin.test.* // Wichtig für assertEquals, assertTrue etc.

class ApplicationTest {

    @Test
    fun testRootRoute() = testApplication {
         application {
             module() // Ruft deine Konfigurationsfunktion auf
         }

        // Sendet eine GET-Anfrage an "/" innerhalb der Test-App
        val response = client.get("/")

        // Überprüfungen (Assertions)
        assertEquals(HttpStatusCode.OK, response.status, "Status Code should be OK")
        val content = response.bodyAsText() // Holt den HTML-Body als Text
        assertTrue(content.contains("Ktor: Hello, Java 21.0.6!"), "Welcome message missing")

    }

}