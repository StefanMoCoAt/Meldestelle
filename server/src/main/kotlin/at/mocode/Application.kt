package at.mocode

import at.mocode.config.ServiceConfiguration
import at.mocode.plugins.configureDatabase
import at.mocode.plugins.configureRouting
import at.mocode.utils.ApiResponse
import at.mocode.validation.ValidationException
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val log = LoggerFactory.getLogger("Application")
    log.info("Initializing application...")

    // Configure dependency injection
    ServiceConfiguration.configureServices()
    log.info("Services configured")

    configureDatabase()
    configurePlugins()
    configureRouting()
    log.info("Application initialized successfully")
}

private fun Application.configurePlugins() {
    val log = LoggerFactory.getLogger("ApplicationPlugins")
    // Add default headers to all responses
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        header("X-Content-Type-Options", "nosniff")
    }

    // Configure call logging
    install(CallLogging) {
        level = Level.INFO
    }

    // Configure content negotiation with JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // Configure CORS
    install(CORS) {
        // Default CORS configuration
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        // Try to read from config to override defaults
        try {
            val appEnv = this@configurePlugins.environment.config
            if (appEnv.propertyOrNull("cors") != null) {
                val corsConfig = appEnv.config("cors")

                // Clear default anyHost if we have specific hosts
                if (corsConfig.propertyOrNull("allowedHosts") != null) {
                    val hosts = corsConfig.property("allowedHosts").getList()
                    if (hosts.isNotEmpty()) {
                        hosts.forEach { host ->
                            allowHost(host)
                        }
                    }
                }

                // Allow credentials if configured
                if (corsConfig.propertyOrNull("allowCredentials") != null) {
                    allowCredentials = corsConfig.property("allowCredentials").getString().toBoolean()
                }
            }
        } catch (e: Exception) {
            // Log the error but continue with the default configuration
            this@configurePlugins.log.warn("Failed to configure CORS from config, using defaults: ${e.message}")
        }
    }

    // Configure status pages for error handling
    install(StatusPages) {
        // Handle validation exceptions with detailed error information
        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(
                    success = false,
                    error = "VALIDATION_ERROR",
                    message = "Validation failed: ${cause.validationResult.errors.joinToString(", ") { "${it.field}: ${it.message}" }}"
                )
            )
        }

        // Handle illegal argument exceptions (typically validation-related)
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Nothing>(
                    success = false,
                    error = "INVALID_INPUT",
                    message = cause.message ?: "Invalid input provided"
                )
            )
        }

        // Handle not found exceptions
        exception<NoSuchElementException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Nothing>(
                    success = false,
                    error = "NOT_FOUND",
                    message = cause.message ?: "Resource not found"
                )
            )
        }

        // Handle all other exceptions
        exception<Throwable> { call, cause ->
            this@configurePlugins.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Nothing>(
                    success = false,
                    error = "INTERNAL_ERROR",
                    message = "An internal server error occurred"
                )
            )
        }

        // Handle 404 status
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondText(
                "404: Page Not Found",
                ContentType.Text.Plain,
                HttpStatusCode.NotFound
            )
        }
    }
}
