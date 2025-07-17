package at.mocode.gateway.config

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*

/**
 * Security configuration for the API Gateway.
 *
 * Configures CORS, authentication, and other security-related settings.
 */
fun Application.configureSecurity() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Requested-With")

        // Allow requests from common development origins
        allowHost("localhost:3000")
        allowHost("localhost:8080")
        allowHost("127.0.0.1:3000")
        allowHost("127.0.0.1:8080")

        // In production, configure specific allowed origins
        anyHost() // This should be restricted in production
    }

    // TODO: Add JWT authentication configuration
    // install(Authentication) {
    //     jwt("auth-jwt") {
    //         // JWT configuration
    //     }
    // }
}
