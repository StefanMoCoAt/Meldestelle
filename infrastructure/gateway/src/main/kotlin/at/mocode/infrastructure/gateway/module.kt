package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.config.*
import at.mocode.infrastructure.gateway.config.configurePrometheusMetrics
import at.mocode.infrastructure.gateway.config.configureCustomMetrics
import at.mocode.infrastructure.gateway.plugins.configureHttpCaching
import at.mocode.infrastructure.gateway.routing.docRoutes
import at.mocode.infrastructure.gateway.routing.serviceRoutes
import at.mocode.core.utils.config.AppConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
            call.respondText(
                "${config.appInfo.name} API v${config.appInfo.version} (${config.environment})",
                ContentType.Text.Plain
            )
        }

        // Health check endpoint
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf(
                "status" to "UP",
                "timestamp" to System.currentTimeMillis(),
                "services" to mapOf(
                    "api-gateway" to "UP",
                    "database" to "UP"
                )
            ))
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
