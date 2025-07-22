package at.mocode.infrastructure.gateway.routing

import at.mocode.core.domain.model.ApiResponse
import at.mocode.infrastructure.auth.client.AuthenticationService
import at.mocode.infrastructure.auth.client.JwtService
import at.mocode.core.utils.validation.ApiValidationUtils
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Konfiguriert die Authentifizierungs-Routen.
 */
fun Routing.authRoutes(
    authenticationService: AuthenticationService,
    jwtService: JwtService
) {
    route("/auth") {
        // Login-Route
        post("/login") {
            try {
                // Request-Daten lesen
                val request = call.receive<LoginRequest>()

                // Validierung
                val validationErrors = ApiValidationUtils.validateLoginRequest(request.username, request.password)
                if (!ApiValidationUtils.isValid(validationErrors)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<LoginResponse>(ApiValidationUtils.createErrorMessage(validationErrors))
                    )
                    return@post
                }

                // Authentifizierung durchführen
                val authResult = authenticationService.authenticate(request.username, request.password)

                // Antwort basierend auf dem Ergebnis senden
                when (authResult) {
                    is AuthenticationService.AuthResult.Success -> {
                        call.respond(
                            HttpStatusCode.OK,
                            ApiResponse.success(
                                LoginResponse(
                                    token = authResult.token,
                                    userId = authResult.user.userId.toString(),
                                    personId = authResult.user.personId.toString(),
                                    username = authResult.user.username,
                                    email = authResult.user.email
                                )
                            )
                        )
                    }

                    is AuthenticationService.AuthResult.Failure -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<LoginResponse>(authResult.reason)
                        )
                    }

                    is AuthenticationService.AuthResult.Locked -> {
                        call.respond(
                            HttpStatusCode.Locked,
                            ApiResponse.error<LoginResponse>(
                                "Account gesperrt bis ${authResult.lockedUntil}"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse.error<LoginResponse>("Fehler bei der Anmeldung: ${e.message}")
                )
            }
        }

        // Registrierung (Beispiel, sollte an die Anforderungen angepasst werden)
        post("/register") {
            // Würde hier Registrierung implementieren
            call.respond(
                HttpStatusCode.NotImplemented,
                ApiResponse.error<Any>("Registrierung noch nicht implementiert")
            )
        }

        // Passwort ändern (geschützte Route)
        authenticate("jwt") {
            post("/change-password") {
                try {
                    // Request-Daten lesen
                    val request = call.receive<ChangePasswordRequest>()

                    // Validierung
                    val validationErrors = ApiValidationUtils.validateChangePasswordRequest(
                        request.currentPassword,
                        request.newPassword,
                        request.confirmPassword
                    )
                    if (!ApiValidationUtils.isValid(validationErrors)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Any>(ApiValidationUtils.createErrorMessage(validationErrors))
                        )
                        return@post
                    }

                    // Benutzer-ID aus dem Token extrahieren
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim("sub", String::class) ?: run {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Any>("Ungültiges Token")
                        )
                        return@post
                    }

                    // Passwort ändern
                    val result = authenticationService.changePassword(
                        com.benasher44.uuid.Uuid.fromString(userId),
                        request.currentPassword,
                        request.newPassword
                    )

                    // Antwort basierend auf dem Ergebnis senden
                    when (result) {
                        is AuthenticationService.PasswordChangeResult.Success -> {
                            call.respond(
                                HttpStatusCode.OK,
                                ApiResponse.success("Passwort erfolgreich geändert")
                            )
                        }

                        is AuthenticationService.PasswordChangeResult.Failure -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<Any>(result.reason)
                            )
                        }

                        is AuthenticationService.PasswordChangeResult.WeakPassword -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<Any>("Das neue Passwort ist zu schwach")
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Any>("Fehler bei der Passwortänderung: ${e.message}")
                    )
                }
            }

            // Benutzerinformationen abrufen
            get("/me") {
                try {
                    // Token validieren und Benutzerinformationen abrufen
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim("sub", String::class) ?: run {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiResponse.error<Any>("Ungültiges Token")
                        )
                        return@get
                    }

                    // Hier können zusätzliche Informationen aus dem Token oder der Datenbank abgerufen werden
                    val username = principal.getClaim("username", String::class) ?: ""
                    val personId = principal.getClaim("personId", String::class) ?: ""
                    val permissions = principal.getClaim("permissions", String::class)?.split(",") ?: listOf()

                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse.success(
                            UserInfoResponse(
                                userId = userId,
                                personId = personId,
                                username = username,
                                permissions = permissions
                            )
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse.error<Any>("Fehler beim Abrufen der Benutzerinformationen: ${e.message}")
                    )
                }
            }
        }
    }
}


/**
 * Request-Modell für die Anmeldung.
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Response-Modell für eine erfolgreiche Anmeldung.
 */
@Serializable
data class LoginResponse(
    val token: String,
    val userId: String,
    val personId: String,
    val username: String,
    val email: String
)

/**
 * Request-Modell für die Passwortänderung.
 */
@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

/**
 * Response-Modell für Benutzerinformationen.
 */
@Serializable
data class UserInfoResponse(
    val userId: String,
    val personId: String,
    val username: String,
    val permissions: List<String>
)
