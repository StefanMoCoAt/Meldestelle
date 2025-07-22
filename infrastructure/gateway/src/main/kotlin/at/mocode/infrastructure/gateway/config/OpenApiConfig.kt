package at.mocode.infrastructure.gateway.config

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

/**
 * Configuration for OpenAPI/Swagger documentation.
 *
 * This module configures the OpenAPI specification generation and Swagger UI
 * for the API Gateway, providing comprehensive API documentation.
 *
 * The OpenAPI specification is loaded from a static YAML file located at:
 * resources/openapi/documentation.yaml
 */
fun Application.configureOpenApi() {
    // Configure OpenAPI endpoint using the static YAML file
    routing {
        // Serve the OpenAPI specification from a file
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml") {
            // Additional configuration can be added here if needed
        }
    }
}

/**
 * Configuration for Swagger UI.
 *
 * Provides an interactive web interface for exploring and testing the API.
 */
fun Application.configureSwagger() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
            version = "4.15.5"
        }
    }
}
