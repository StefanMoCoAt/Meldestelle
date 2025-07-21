package at.mocode.gateway.config

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.response.*
import at.mocode.dto.base.ApiResponse
import org.slf4j.event.Level

/**
 * Monitoring and logging configuration for the API Gateway.
 *
 * Configures request logging, error handling, and status pages.
 */
fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val userAgent = call.request.headers["User-Agent"]
            "$status: $httpMethod ${call.request.path()} - $userAgent"
        }
    }

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
