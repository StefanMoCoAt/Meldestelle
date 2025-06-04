package at.mocode.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Configures Cross-Origin Resource Sharing (CORS) for the application.
 * This allows the frontend to make requests to the backend API from different origins.
 */
fun Application.configureCORS() {
    install(CORS) {
        // Production hosts
        // Allow requests from the frontend container in the Docker network
        allowHost("frontend", schemes = listOf("http", "https"))

        // Development hosts configuration
        val isDevelopment = System.getProperty("io.ktor.development")?.toBoolean() ?: false
        if (isDevelopment) {
            // Allow various localhost ports for development
            allowHost("localhost:3000", schemes = listOf("http", "https")) // React dev server
            allowHost("localhost:9090", schemes = listOf("http", "https")) // Webpack dev server
            allowHost("localhost:8080", schemes = listOf("http", "https")) // Ktor dev server
            allowHost("localhost:8081", schemes = listOf("http", "https")) // Alternative port
        }

        // Allow requests with credentials (cookies, authorization headers)
        allowCredentials = true

        // Allow necessary HTTP methods for REST API
        allowMethod(HttpMethod.Options) // Preflight requests
        allowMethod(HttpMethod.Get)     // Read operations
        allowMethod(HttpMethod.Post)    // Create operations
        allowMethod(HttpMethod.Put)     // Update operations
        allowMethod(HttpMethod.Delete)  // Delete operations

        // Allow common headers
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        // Cache CORS preflight results for 1 hour (3600 seconds)
        maxAgeInSeconds = 3600
    }
}
