package at.mocode.gateway

import at.mocode.shared.config.AppConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
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

    routing {
        // Hauptrouten
        get("/") {
            call.respondText(
                "${config.appInfo.name} API v${config.appInfo.version} (${config.environment})",
                ContentType.Text.Plain
            )
        }
    }
}
