package at.mocode.gateway.auth

import at.mocode.shared.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*

/**
 * Konfiguriert die API-Key-Authentifizierung für die Anwendung.
 * Diese einfache Authentifizierung kann für externe Systeme verwendet werden,
 * die keinen JWT-basierten Zugriff benötigen.
 */
fun Application.configureApiKeyAuth() {
    val apiKey = AppConfig.security.apiKey ?: "api-key-not-configured"

    install(Authentication) {
        register(object : AuthenticationProvider(object : AuthenticationProvider.Config("api-key") {}) {
            override suspend fun onAuthenticate(context: AuthenticationContext) {
                val call = context.call

                val requestApiKey = call.request.header("X-API-Key")
                    ?: call.request.queryParameters["api_key"]

                if (requestApiKey == apiKey) {
                    context.principal(ApiKeyPrincipal(apiKey))
                } else {
                    context.challenge("ApiKeyAuth", AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
                        call.respond(HttpStatusCode.Unauthorized, "Ungültiger API-Key")
                        challenge.complete()
                    }
                }
            }
        })
    }
}

/**
 * Principal für die API-Key-Authentifizierung.
 */
class ApiKeyPrincipal(val apiKey: String) : Principal
