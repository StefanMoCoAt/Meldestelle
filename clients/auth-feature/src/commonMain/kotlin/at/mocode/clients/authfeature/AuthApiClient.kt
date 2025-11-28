package at.mocode.clients.authfeature

import at.mocode.clients.shared.AppConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.Serializable

/**
 * Data classes for authentication API communication
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null,
    val userId: String? = null,
    val username: String? = null
)

/**
 * HTTP client for authentication API calls
 */
class AuthApiClient(
    // Keycloak Basis-URL (z. B. http://localhost:8180)
  private val keycloakBaseUrl: String = AppConfig.KEYCLOAK_URL,
    // Realm-Name in Keycloak
  private val realm: String = AppConfig.KEYCLOAK_REALM,
    // Client-ID (Public Client empfohlen für Frontend-Flows)
  private val clientId: String = AppConfig.KEYCLOAK_CLIENT_ID,
    // Optional: Client-Secret (nur bei vertraulichen Clients erforderlich)
  private val clientSecret: String? = null
) {
    private val client = AuthenticatedHttpClient.createUnauthenticated()

    /**
     * Authenticate user with username and password
     */
    suspend fun login(username: String, password: String): LoginResponse {
        val tokenEndpoint = "$keycloakBaseUrl/realms/$realm/protocol/openid-connect/token"
        return try {
            val response = client.submitForm(
                url = tokenEndpoint,
                formParameters = Parameters.build {
                    append("grant_type", "password")
                    append("client_id", clientId)
                    if (!clientSecret.isNullOrBlank()) {
                        append("client_secret", clientSecret)
                    }
                    append("username", username)
                    append("password", password)
                }
            ) {
                // Explicit: URL-encoded Form
                contentType(ContentType.Application.FormUrlEncoded)
            }

            if (response.status.isSuccess()) {
                val kc = response.body<KeycloakTokenResponse>()
                LoginResponse(
                    success = true,
                    token = kc.access_token,
                    message = null,
                    userId = null,
                    username = username
                )
            } else {
                LoginResponse(
                    success = false,
                    message = "Login fehlgeschlagen: HTTP ${response.status.value}"
                )
            }
        } catch (e: Exception) {
            LoginResponse(
                success = false,
                message = "Verbindungsfehler: ${e.message}"
            )
        }
    }

    /**
     * Exchange an authorization code (PKCE) for tokens
     */
    suspend fun exchangeAuthorizationCode(code: String, codeVerifier: String, redirectUri: String): LoginResponse {
        val tokenEndpoint = "$keycloakBaseUrl/realms/$realm/protocol/openid-connect/token"
        return try {
            val response = client.submitForm(
                url = tokenEndpoint,
                formParameters = Parameters.build {
                    append("grant_type", "authorization_code")
                    append("client_id", clientId)
                    if (!clientSecret.isNullOrBlank()) {
                        append("client_secret", clientSecret)
                    }
                    append("code", code)
                    append("code_verifier", codeVerifier)
                    append("redirect_uri", redirectUri)
                }
            ) {
                contentType(ContentType.Application.FormUrlEncoded)
            }

            if (response.status.isSuccess()) {
                val kc = response.body<KeycloakTokenResponse>()
                LoginResponse(
                    success = true,
                    token = kc.access_token,
                    message = null
                )
            } else {
                LoginResponse(
                    success = false,
                    message = "Code-Exchange fehlgeschlagen: HTTP ${'$'}{response.status.value}"
                )
            }
        } catch (e: Exception) {
            LoginResponse(
                success = false,
                message = "Code-Exchange Fehler: ${'$'}{e.message}"
            )
        }
    }

    /**
     * Refresh authentication token
     */
    suspend fun refreshToken(refreshToken: String): LoginResponse {
        val tokenEndpoint = "$keycloakBaseUrl/realms/$realm/protocol/openid-connect/token"
        return try {
            val response = client.submitForm(
                url = tokenEndpoint,
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("client_id", clientId)
                    if (!clientSecret.isNullOrBlank()) {
                        append("client_secret", clientSecret)
                    }
                    append("refresh_token", refreshToken)
                }
            ) {
                contentType(ContentType.Application.FormUrlEncoded)
            }

            if (response.status.isSuccess()) {
                val kc = response.body<KeycloakTokenResponse>()
                LoginResponse(
                    success = true,
                    token = kc.access_token,
                    message = null
                )
            } else {
                LoginResponse(
                    success = false,
                    message = "Token refresh fehlgeschlagen: HTTP ${response.status.value}"
                )
            }
        } catch (e: Exception) {
            LoginResponse(
                success = false,
                message = "Token refresh Fehler: ${e.message}"
            )
        }
    }

    /**
     * Logout and invalidate token
     */
    suspend fun logout(token: String): Boolean {
        // Empfehlung: Frontend-seitig Token lokal verwerfen.
        // Optional könnten hier Keycloak-Endpoints für Token-Revocation aufgerufen werden.
        return true
    }

    @Serializable
    private data class KeycloakTokenResponse(
        val access_token: String,
        val expires_in: Long? = null,
        val refresh_expires_in: Long? = null,
        val refresh_token: String? = null,
        val token_type: String? = null,
        val not_before_policy: Long? = null,
        val session_state: String? = null,
        val scope: String? = null
    )
}
