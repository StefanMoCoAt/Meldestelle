package at.mocode.plugins

import at.mocode.routes.artikelRoutes
import at.mocode.routes.personRoutes
import at.mocode.routes.vereinRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Configures all routes for the application
 */
fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }

        // Root endpoint with basic information
        get("/") {
            // Read application info from config if available
            val appName = application.environment.config.propertyOrNull("application.name")?.getString() ?: "Meldestelle API Server"
            val appVersion = application.environment.config.propertyOrNull("application.version")?.getString() ?: "1.0.0"
            val appEnv = application.environment.config.propertyOrNull("application.environment")?.getString() ?: "development"

            call.respondText("$appName v$appVersion - Running in $appEnv mode")
        }

        // API routes
        personRoutes()
        vereinRoutes()
        artikelRoutes()
    }
}
