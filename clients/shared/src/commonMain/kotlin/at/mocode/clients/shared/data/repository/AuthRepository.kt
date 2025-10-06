package at.mocode.clients.shared.data.repository

import at.mocode.clients.shared.domain.models.User
import at.mocode.clients.shared.domain.models.AuthToken
import at.mocode.clients.shared.domain.models.ApiResponse
import at.mocode.clients.shared.network.HttpClientConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Authentication repository handling all authentication-related operations
 * with Keycloak integration.
 */
class AuthRepository(
    private val baseUrl: String = "http://localhost:8080",
    private val keycloakUrl: String = "http://localhost:8180",
    private val realm: String = "meldestelle",
    private val clientId: String = "meldestelle-client"
) : Repository {

    private val httpClient: HttpClient = HttpClientConfig.createClient(baseUrl)

    @Serializable
    data class LoginRequest(
        val username: String,
        val password: String
    )

    @Serializable
    data class KeycloakTokenResponse(
        val access_token: String,
        val refresh_token: String,
        val expires_in: Long,
        val token_type: String = "Bearer"
    )

    /**
     * Authenticate user with username and password via Keycloak
     */
    suspend fun login(username: String, password: String): RepositoryResult<AuthToken> {
        return try {
            val response = httpClient.submitForm(
                url = "$keycloakUrl/realms/$realm/protocol/openid-connect/token",
                formParameters = Parameters.build {
                    append("grant_type", "password")
                    append("client_id", clientId)
                    append("username", username)
                    append("password", password)
                }
            ).body<KeycloakTokenResponse>()

            val authToken = AuthToken(
                accessToken = response.access_token,
                refreshToken = response.refresh_token,
                expiresIn = response.expires_in,
                tokenType = response.token_type
            )

            RepositoryResult.Success(authToken)
        } catch (e: Exception) {
            RepositoryResult.Error(
                at.mocode.clients.shared.domain.models.ApiError(
                    code = "LOGIN_FAILED",
                    message = "Login failed: ${e.message}"
                )
            )
        }
    }

    /**
     * Refresh authentication token
     */
    suspend fun refreshToken(refreshToken: String): RepositoryResult<AuthToken> {
        return try {
            val response = httpClient.submitForm(
                url = "$keycloakUrl/realms/$realm/protocol/openid-connect/token",
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("client_id", clientId)
                    append("refresh_token", refreshToken)
                }
            ).body<KeycloakTokenResponse>()

            val authToken = AuthToken(
                accessToken = response.access_token,
                refreshToken = response.refresh_token,
                expiresIn = response.expires_in,
                tokenType = response.token_type
            )

            RepositoryResult.Success(authToken)
        } catch (e: Exception) {
            RepositoryResult.Error(
                at.mocode.clients.shared.domain.models.ApiError(
                    code = "TOKEN_REFRESH_FAILED",
                    message = "Token refresh failed: ${e.message}"
                )
            )
        }
    }

    /**
     * Get current user information using access token
     */
    suspend fun getCurrentUser(accessToken: String): RepositoryResult<User> {
        return try {
            val response = httpClient.get("$baseUrl/api/auth/me") {
                header("Authorization", "Bearer $accessToken")
            }.body<ApiResponse<User>>()

            response.toRepositoryResult()
        } catch (e: Exception) {
            RepositoryResult.Error(
                at.mocode.clients.shared.domain.models.ApiError(
                    code = "USER_INFO_FAILED",
                    message = "Failed to get user info: ${e.message}"
                )
            )
        }
    }

    /**
     * Logout user by invalidating tokens
     */
    suspend fun logout(refreshToken: String): RepositoryResult<Unit> {
        return try {
            httpClient.submitForm(
                url = "$keycloakUrl/realms/$realm/protocol/openid-connect/logout",
                formParameters = Parameters.build {
                    append("client_id", clientId)
                    append("refresh_token", refreshToken)
                }
            )

            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error(
                at.mocode.clients.shared.domain.models.ApiError(
                    code = "LOGOUT_FAILED",
                    message = "Logout failed: ${e.message}"
                )
            )
        }
    }

    /**
     * Check if token is still valid
     */
    suspend fun validateToken(accessToken: String): RepositoryResult<Boolean> {
        return try {
            val response = httpClient.get("$baseUrl/api/auth/validate") {
                header("Authorization", "Bearer $accessToken")
            }.body<ApiResponse<Boolean>>()

            response.toRepositoryResult()
        } catch (e: Exception) {
            RepositoryResult.Success(false) // Token is invalid
        }
    }

    /**
     * Cleanup resources
     */
    fun close() {
        httpClient.close()
    }
}
