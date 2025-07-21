package at.mocode.gateway

import at.mocode.gateway.config.configureOpenApi
import at.mocode.gateway.config.configureSwagger
import at.mocode.gateway.routing.docRoutes
import at.mocode.shared.config.AppConfig
import at.mocode.shared.config.RateLimitConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.http.content.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.minutes

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
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
        }
    }

    // Call-Logging installieren
    if (config.logging.logRequests) {
        install(CallLogging)
    }

    // OpenAPI und Swagger UI konfigurieren
    configureOpenApi()
    configureSwagger()

    // Rate Limiting konfigurieren
    if (config.rateLimit.enabled) {
        install(RateLimit) {
            // Globale Rate Limiting Konfiguration
            global {
                // Limit basierend auf Konfiguration
                rateLimiter(
                    limit = config.rateLimit.globalLimit,
                    refillPeriod = config.rateLimit.globalPeriodMinutes.minutes
                )
                // Request-Key basierend auf IP-Adresse
                requestKey { call -> call.request.local.remoteHost }
            }

            // Konfiguriere Rate Limiting für spezifische Routen
            // Wir verwenden hier einen Interceptor, um die Response-Header hinzuzufügen
            if (config.rateLimit.includeHeaders) {
                this@module.intercept(ApplicationCallPipeline.Plugins) {
                    call.response.header("X-RateLimit-Enabled", "true")
                    call.response.header("X-RateLimit-Limit", config.rateLimit.globalLimit.toString())
                }
            }
        }
    }

    routing {
        // Hauptrouten
        get("/") {
            call.respondText(
                "${config.appInfo.name} API v${config.appInfo.version} (${config.environment})",
                ContentType.Text.Plain
            )
        }

        // Static resources for documentation
        staticResources("/docs", "static/docs") {
            default("index.html")
        }

        // API Documentation routes
        docRoutes()
    }
}
