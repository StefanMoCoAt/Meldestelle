package at.mocode.clients.authfeature

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
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
    private val baseUrl: String = "http://localhost:8081"
) {
    private val client = AuthenticatedHttpClient.createUnauthenticated()

    /**
     * Authenticate user with username and password
     */
    suspend fun login(username: String, password: String): LoginResponse {
        return try {
            val response = client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username = username, password = password))
            }

            if (response.status.isSuccess()) {
                response.body<LoginResponse>()
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
     * Refresh authentication token
     */
    suspend fun refreshToken(token: String): LoginResponse {
        return try {
            val response = client.post("$baseUrl/api/auth/refresh") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            if (response.status.isSuccess()) {
                response.body<LoginResponse>()
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
        return try {
            val response = client.post("$baseUrl/api/auth/logout") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            response.status.isSuccess()
        } catch (_: Exception) {
            false // Logout failed, but we'll clear local token anyway
        }
    }
}
