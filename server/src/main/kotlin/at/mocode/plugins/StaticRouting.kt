package at.mocode.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

/**
 * Configures static content routing for the application
 */
fun Application.configureStaticRouting() {
    routing {
        // Serve static files from resources/static directory
        static("/static") {
            resources("static")
        }
    }
}
