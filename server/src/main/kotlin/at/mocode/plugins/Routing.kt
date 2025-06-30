package at.mocode.plugins

import at.mocode.config.AppConfig
import at.mocode.routes.RouteConfiguration.configureApiRoutes
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Configures all routes for the application using the centralized route configuration
 */
fun Application.configureRouting() {
    // Load application configuration
    val appConfig = AppConfig.loadConfig(this)

    routing {
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }

        // Serve static content (HTML, CSS, JS, images, etc.)
        staticResources("/", "static")

        // Root endpoint with basic information (API info endpoint)
        get("/api") {
            call.respondText(appConfig.getAppInfoString())
        }

        // Configure all API routes using the centralized configuration
        configureApiRoutes()
    }
}
