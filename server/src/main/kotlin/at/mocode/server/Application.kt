package at.mocode.server

import at.mocode.server.plugins.configureDatabase
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

/**
 * Main entry point for the application.
 * Uses Ktor's EngineMain to start the server with configuration from application.yaml
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Application module configuration.
 * This is where all server plugins and routes are configured.
 */
fun Application.module() {
    val log = LoggerFactory.getLogger("Application")

    log.info("Initializing application...")

    // Configure database
    configureDatabase()

    // Configure plugins
    configurePlugins()

    // Configure routing
    configureRouting()

    log.info("Application initialized successfully")
}

/**
 * Configures all Ktor plugins for the application
 */
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
            // Log the error but continue with default configuration
            this@configurePlugins.log.warn("Failed to configure CORS from config, using defaults: ${e.message}")
        }
    }

    // Configure status pages for error handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                text = "500: ${cause.message ?: "Internal Server Error"}",
                status = HttpStatusCode.InternalServerError
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondText(
                text = "404: Page Not Found",
                status = HttpStatusCode.NotFound
            )
        }
    }
}

/**
 * Configures all routes for the application
 */
private fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }

        // Root endpoint with basic information
        get("/") {
            // Read application info from config if available
            val appName = application.environment.config.propertyOrNull("application.name")?.getString() ?: "Meldestelle API Server"
            val appVersion = application.environment.config.propertyOrNull("application.version")?.getString() ?: "1.0.0"
            val appEnv = application.environment.config.propertyOrNull("application.environment")?.getString() ?: "development"

            call.respondText("$appName v$appVersion - Running in $appEnv mode")
        }

        // API routes can be organized in separate files and included here
        // Example: registerUserRoutes()
    }
}
