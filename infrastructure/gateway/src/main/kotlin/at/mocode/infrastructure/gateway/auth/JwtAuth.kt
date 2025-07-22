package at.mocode.infrastructure.gateway.auth

import at.mocode.core.domain.model.BerechtigungE
import at.mocode.infrastructure.auth.client.JwtService
import at.mocode.core.utils.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

/**
 * Konfiguriert die JWT-Authentifizierung für die Anwendung.
 */
fun Application.configureJwtAuth(jwtService: JwtService) {
    val jwtConfig = AppConfig.security.jwt

    install(Authentication) {
        jwt("jwt") {
            realm = jwtConfig.realm
            verifier {
                com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC512(jwtConfig.secret))
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            }
            validate { credential ->
                // Token is already validated by the verifier above
                // Just check if required claims are present
                val subject = credential.payload.subject
                val permissions = credential.payload.getClaim("permissions")

                if (subject != null && permissions != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token ungültig oder abgelaufen")
            }
        }
    }
}

/**
 * Prüft, ob der aktuelle Benutzer die angegebene Berechtigung hat.
 * Muss innerhalb eines authenticate("jwt")-Block verwendet werden.
 *
 * @param permission Die erforderliche Berechtigung
 * @param onFailure Funktion, die bei fehlender Berechtigung aufgerufen wird
 * @param onSuccess Funktion, die bei vorhandener Berechtigung aufgerufen wird
 */
suspend fun ApplicationCall.requirePermission(
    permission: BerechtigungE,
    onFailure: suspend () -> Unit = { respond(HttpStatusCode.Forbidden, "Keine Berechtigung") },
    onSuccess: suspend () -> Unit
) {
    val principal = principal<JWTPrincipal>()
    if (principal == null) {
        respond(HttpStatusCode.Unauthorized, "Nicht authentifiziert")
        return
    }

    val permissions = principal.getClaim("permissions", Array<String>::class)?.mapNotNull {
        try {
            BerechtigungE.valueOf(it)
        } catch (e: Exception) {
            null
        }
    } ?: emptyList()

    if (permissions.contains(permission) || permissions.contains(BerechtigungE.SYSTEM_ADMIN)) {
        onSuccess()
    } else {
        onFailure()
    }
}

/**
 * Prüft, ob der aktuelle Benutzer eine der angegebenen Berechtigungen hat.
 * Muss innerhalb eines authenticate("jwt")-Block verwendet werden.
 *
 * @param permissions Die erforderlichen Berechtigungen (eine davon ist ausreichend)
 * @param onFailure Funktion, die bei fehlender Berechtigung aufgerufen wird
 * @param onSuccess Funktion, die bei vorhandener Berechtigung aufgerufen wird
 */
suspend fun ApplicationCall.requireAnyPermission(
    vararg permissions: BerechtigungE,
    onFailure: suspend () -> Unit = { respond(HttpStatusCode.Forbidden, "Keine Berechtigung") },
    onSuccess: suspend () -> Unit
) {
    val principal = principal<JWTPrincipal>()
    if (principal == null) {
        respond(HttpStatusCode.Unauthorized, "Nicht authentifiziert")
        return
    }

    val userPermissions = principal.getClaim("permissions", Array<String>::class)?.mapNotNull {
        try {
            BerechtigungE.valueOf(it)
        } catch (_: Exception) {
            null
        }
    } ?: emptyList()

    if (userPermissions.contains(BerechtigungE.SYSTEM_ADMIN) ||
        permissions.any { userPermissions.contains(it) }) {
        onSuccess()
    } else {
        onFailure()
    }
}
