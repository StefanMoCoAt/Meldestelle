package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.config.*
import at.mocode.infrastructure.gateway.config.configurePrometheusMetrics
import at.mocode.infrastructure.gateway.config.configureCustomMetrics
import at.mocode.infrastructure.gateway.plugins.configureHttpCaching
import at.mocode.infrastructure.gateway.routing.docRoutes
import at.mocode.infrastructure.gateway.routing.serviceRoutes
import at.mocode.infrastructure.gateway.routing.ApiGatewayInfo
import at.mocode.infrastructure.gateway.routing.HealthStatus
import at.mocode.core.utils.config.AppConfig
import at.mocode.core.domain.model.ApiResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

fun Application.module() {
    val config = AppConfig

    // ContentNegotiation installieren
    install(ContentNegotiation) {
        json()
    }

    // CORS installieren, wenn aktiviert
    if (config.server.cors.enabled) {
        install(CORS) {
            if (config.server.cors.allowedOrigins.contains("*")) {
                anyHost()
            } else {
                config.server.cors.allowedOrigins.forEach { allowHost(it, schemes = listOf("http", "https")) }
            }
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            // Add request ID header to allowed headers
            allowHeader(config.logging.requestIdHeader)
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
        }
    }

    // Authentication installieren (für Metrics-Endpoint)
    install(Authentication) {
        basic("metrics-auth") {
            realm = "Metrics Access"
            validate { credentials ->
                // Simple validation for metrics endpoint
                if (credentials.name == "admin" && credentials.password == "metrics") {
                    UserIdPrincipal(credentials.name)
                } else null
            }
        }
    }

    // Erweiterte Monitoring- und Logging-Konfiguration
    configureMonitoring()

    // Prometheus Metrics konfigurieren
    configurePrometheusMetrics()

    // Custom application metrics konfigurieren
    configureCustomMetrics()

    // Request Tracing für Cross-Service Tracing konfigurieren
    configureRequestTracing()

    // Enhanced Rate Limiting konfigurieren
    configureRateLimiting()

    // OpenAPI und Swagger UI konfigurieren
    configureOpenApi()
    configureSwagger()

    // HTTP Caching konfigurieren
    configureHttpCaching()

    routing {
        // Hauptrouten
        get("/") {
            val gatewayInfo = ApiGatewayInfo(
                name = "Meldestelle API Gateway",
                version = "1.0.0",
                description = "API Gateway for Meldestelle Self-Contained Systems",
                availableContexts = listOf("authentication", "master-data", "horse-registry"),
                endpoints = mapOf(
                    "health" to "/health",
                    "metrics" to "/metrics",
                    "docs" to "/docs",
                    "api" to "/api",
                    "swagger" to "/swagger"
                )
            )
            call.respond(ApiResponse.success(gatewayInfo, "API Gateway information retrieved successfully"))
        }

        // Health check endpoint
        get("/health") {
            val healthStatus = HealthStatus(
                status = "UP",
                contexts = mapOf(
                    "authentication" to "UP",
                    "master-data" to "UP",
                    "horse-registry" to "UP"
                )
            )
            call.respond(ApiResponse.success(healthStatus, "Health check completed successfully"))
        }

        // Static resources for documentation
        staticResources("/docs", "static/docs") {
            default("index.html")
        }

        // API Documentation routes
        docRoutes()

        // Service discovery routes
        serviceRoutes()
    }
}
