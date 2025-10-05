package at.mocode.clients.authfeature

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Singleton object for managing authenticated HTTP client configuration.
 * Provides methods to create HTTP clients and add authentication headers manually.
 */
object AuthenticatedHttpClient {

    private val authTokenManager = AuthTokenManager()

    /**
     * Create a basic HTTP client with JSON support
     */
    fun create(baseUrl: String = "http://localhost:8081"): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    /**
     * Add an authentication header to an HTTP request builder if a token is available
     */
    fun HttpRequestBuilder.addAuthHeader() {
        authTokenManager.getBearerToken()?.let { bearerToken ->
            header(HttpHeaders.Authorization, bearerToken)
        }
    }

    /**
     * Get the shared AuthTokenManager instance
     */
    fun getAuthTokenManager(): AuthTokenManager = authTokenManager

    /**
     * Create an HTTP client without authentication (for login/public endpoints)
     */
    fun createUnauthenticated(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
