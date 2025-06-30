package at.mocode

import at.mocode.plugins.configureDatabase
import at.mocode.plugins.configureRouting
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
