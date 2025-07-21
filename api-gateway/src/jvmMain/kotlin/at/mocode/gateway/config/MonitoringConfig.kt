package at.mocode.gateway.config

import at.mocode.dto.base.ApiResponse
import at.mocode.shared.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.event.Level
import java.util.*

/**
 * Monitoring and logging configuration for the API Gateway.
 *
 * Configures request logging, error handling, and status pages.
 */
fun Application.configureMonitoring() {
    val loggingConfig = AppConfig.logging

    // Erweiterte Call-Logging-Konfiguration
    install(CallLogging) {
        level = when (loggingConfig.level.uppercase()) {
            "DEBUG" -> Level.DEBUG
            "TRACE" -> Level.TRACE
            "WARN" -> Level.WARN
            "ERROR" -> Level.ERROR
            else -> Level.INFO
        }

        // Filtere Pfade, die vom Logging ausgeschlossen werden sollen
        filter { call ->
            val path = call.request.path()
            !loggingConfig.excludePaths.any { path.startsWith(it) }
        }

        // Formatiere Log-Einträge mit erweitertem Format
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            val userAgent = call.request.headers["User-Agent"]
            val clientIp = call.request.local.remoteHost

            // Generiere eine Correlation-ID für das Request-Tracking
            val correlationId = UUID.randomUUID().toString()

            // Füge Correlation-ID als Response-Header hinzu
            if (loggingConfig.includeCorrelationId) {
                call.response.header("X-Correlation-ID", correlationId)
            }

            if (loggingConfig.useStructuredLogging) {
                // Strukturiertes Logging-Format
                buildString {
                    append("method=$httpMethod ")
                    append("path=$path ")
                    append("status=$status ")
                    append("client=$clientIp ")

                    // Log Headers wenn konfiguriert
                    if (loggingConfig.logRequestHeaders) {
                        val authHeader = call.request.headers["Authorization"]
                        if (authHeader != null) {
                            append("auth=true ")
                        }

                        val contentType = call.request.headers["Content-Type"]
                        if (contentType != null) {
                            append("contentType=$contentType ")
                        }
                    }

                    // Log Query-Parameter wenn konfiguriert
                    if (loggingConfig.logRequestParameters && call.request.queryParameters.entries().isNotEmpty()) {
                        append("params={")
                        call.request.queryParameters.entries().joinTo(this, ", ") { "${it.key}=${it.value.joinToString(",")}" }
                        append("} ")
                    }

                    if (userAgent != null) {
                        append("userAgent=\"${userAgent.replace("\"", "\\\"")}\" ")
                    }

                    // Füge Correlation-ID hinzu, wenn konfiguriert
                    if (loggingConfig.includeCorrelationId) {
                        append("correlationId=$correlationId ")
                    }
                }
            } else {
                // Einfaches Logging-Format
                "$status: $httpMethod $path - $clientIp - $userAgent"
            }
        }
    }

    // Erweiterte Logging-Konfiguration für den API-Gateway
    log.info("API Gateway konfiguriert mit erweitertem Logging")
    log.info("Logging-Konfiguration: level=${loggingConfig.level}, " +
            "logRequests=${loggingConfig.logRequests}, " +
            "logResponses=${loggingConfig.logResponses}, " +
            "logRequestHeaders=${loggingConfig.logRequestHeaders}, " +
            "logRequestParameters=${loggingConfig.logRequestParameters}")

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error<Any>("Internal server error: ${cause.message}")
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiResponse.error<Any>("Endpoint not found: ${call.request.path()}")
            )
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(
                status,
                ApiResponse.error<Any>("Authentication required")
            )
        }

        status(HttpStatusCode.Forbidden) { call, status ->
            call.respond(
                status,
                ApiResponse.error<Any>("Access forbidden")
            )
        }
    }
}
