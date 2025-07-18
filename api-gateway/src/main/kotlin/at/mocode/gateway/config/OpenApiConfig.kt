package at.mocode.gateway.config

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

/**
 * Configuration for OpenAPI/Swagger documentation.
 *
 * This module configures the OpenAPI specification generation and Swagger UI
 * for the API Gateway, providing comprehensive API documentation.
 */
fun Application.configureOpenApi() {
    install(OpenAPI) {
        codegen = org.openapitools.codegen.CodegenType.CLIENT
        info {
            title = "Meldestelle Self-Contained Systems API"
            version = "1.0.0"
            description = "Unified API Gateway for Austrian Equestrian Federation bounded contexts"
            contact {
                name = "API Support"
                email = "support@mocode.at"
            }
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        server("http://localhost:8080") {
            description = "Development server"
        }
        server("https://api.meldestelle.at") {
            description = "Production server"
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
