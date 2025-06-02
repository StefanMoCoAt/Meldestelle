package at.mocode.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Configures CORS for the application
 */
fun Application.configureCORS() {
    install(CORS) {
        // Production hosts
        // frontend - Frontend container in the Docker network
        allowHost("frontend", schemes = listOf("http", "https"))

        // Development hosts - these should be disabled in production
        val isDevelopment = System.getProperty("io.ktor.development")?.toBoolean() ?: false
        if (isDevelopment) {
            allowHost("localhost:3000", schemes = listOf("http", "https"))
            allowHost("localhost:9090", schemes = listOf("http", "https"))
            allowHost("localhost:8080", schemes = listOf("http", "https"))
        }

        // Allow requests with credentials (cookies, authorization headers)
        allowCredentials = true

        // Allow only necessary HTTP methods
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)

        // Allow common headers
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        // Maximum age (in seconds) the browser should cache CORS information
        maxAgeInSeconds = 3600
    }
}
