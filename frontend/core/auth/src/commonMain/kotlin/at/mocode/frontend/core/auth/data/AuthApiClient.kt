package at.mocode.frontend.core.auth.data

import at.mocode.shared.core.AppConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

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
  private val httpClient: HttpClient,
  // Keycloak Basis-URL (z. B. http://localhost:8180)
  private val keycloakBaseUrl: String = AppConstants.KEYCLOAK_URL,
  // Realm-Name in Keycloak
  private val realm: String = AppConstants.KEYCLOAK_REALM,
  // Client-ID (Public Client empfohlen für Frontend-Flows)
  private val clientId: String = AppConstants.KEYCLOAK_CLIENT_ID,
  // Optional: Client-Secret (nur bei vertraulichen Clients erforderlich)
  private val clientSecret: String? = null
) {

  /**
   * Authenticate user with username and password
   */
  suspend fun login(username: String, password: String): LoginResponse {
    val tokenEndpoint = "$keycloakBaseUrl/realms/$realm/protocol/openid-connect/token"
    return try {
      val response = httpClient.submitForm(
        url = tokenEndpoint,
        formParameters = Parameters.build {
          append("grant_type", "password")
          append("client_id", clientId)

          // WICHTIG: Senden Sie client_secret nur, wenn es sich NICHT um einen öffentlichen Client (wie 'web-app') handelt.
          // Keycloak lehnt Anfragen von öffentlichen Clients ab, die client_secret enthalten.
          // Wir prüfen, ob die Client-ID auf einen öffentlichen Client hindeutet oder ob ein Secret explizit angegeben wurde.
          // Aktuell gehen wir davon aus, dass 'web-app' öffentlich ist und daher kein Secret gesendet werden sollte.

          // Logik: Wenn clientId 'web-app' ist, ignorieren wir das Geheimnis oder verlassen uns darauf, dass der Aufrufer null übergibt.
          // Da AppConstants möglicherweise noch das Geheimnis für 'postman-client' enthält, ist Vorsicht geboten.

          if (!clientSecret.isNullOrBlank() && clientId != "web-app") {
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
        val errorBody = response.bodyAsText()
        LoginResponse(
          success = false,
          message = "Login fehlgeschlagen: HTTP ${response.status.value} - $errorBody"
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
   * Refresh authentication token
   */
  suspend fun refreshToken(refreshToken: String): LoginResponse {
    val tokenEndpoint = "$keycloakBaseUrl/realms/$realm/protocol/openid-connect/token"
    return try {
      val response = httpClient.submitForm(
        url = tokenEndpoint,
        formParameters = Parameters.build {
          append("grant_type", "refresh_token")
          append("client_id", clientId)
          if (!clientSecret.isNullOrBlank() && clientId != "web-app") {
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
        val errorBody = response.bodyAsText()
        LoginResponse(
          success = false,
          message = "Token refresh fehlgeschlagen: HTTP ${response.status.value} - $errorBody"
        )
      }
    } catch (e: Exception) {
      LoginResponse(
        success = false,
        message = "Token refresh Fehler: ${e.message}"
      )
    }
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
