package at.mocode.frontend.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module that provides a preconfigured Ktor HttpClient under the named qualifier "apiClient".
 * The client uses the environment-aware base URL from NetworkConfig.
 */
val networkModule = module {
    single(named("apiClient")) {
        HttpClient {
            // JSON (kotlinx) configuration
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    }
                )
            }

            // Request timeouts
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }

            // Automatic simple retry on network exceptions and 5xx
            install(HttpRequestRetry) {
                maxRetries = 3
                retryIf { _, response ->
                    val s = response?.status?.value ?: 0
                    s == 0 || s >= 500
                }
                exponentialDelay()
            }

            // Authentication plugin (Bearer refresh can be wired later)
            install(Auth) {
                // TODO: Wire token provider/refresh when auth is implemented
            }

            // Logging for development
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[apiClient] $message")
                    }
                }
                level = LogLevel.INFO
            }

            // Set base URL
            defaultRequest {
                // Set only the base URL; endpoints will append paths
                url(NetworkConfig.baseUrl)
            }
        }
    }
}
