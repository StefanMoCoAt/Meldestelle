package at.mocode.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configures health check and root endpoint routing for the application
 */
fun Application.configureHealthRouting() {
    routing {
        // Health check endpoint for Docker - explicitly at root level
        get("/health") {
            call.respond(HttpStatusCode.OK, "OK")
        }

        get("/") {
            call.respondText("Ktor ist erreichbar! <br><a href='/static/test.html'>Go to API Test Page</a>", contentType = ContentType.Text.Html)
        }
    }
}
