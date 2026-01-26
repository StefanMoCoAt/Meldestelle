package at.mocode.frontend.core.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Simple token provider interface so the core network module does not depend on auth-feature.
 */
interface TokenProvider {
  fun getAccessToken(): String?
}

/**
 * Koin module providing HttpClients.
 */
val networkModule = module {

  // 1. Base Client (Raw, for Auth/Keycloak)
  single(named("baseHttpClient")) {
    HttpClient {
      install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true })
      }
      install(Logging) {
        logger = object : Logger {
          override fun log(message: String) {
            println("[baseClient] $message")
          }
        }
        level = LogLevel.INFO
      }
    }
  }

  // 2. API Client (Configured for Gateway & Auth Header)
  single(named("apiClient")) {
    // Resolve TokenProvider lazily to avoid circular dependency issues during init
    val tokenProvider: TokenProvider? = try {
      get<TokenProvider>()
    } catch (_: Throwable) {
      println("[apiClient] Warning: No TokenProvider found in Koin")
      null
    }

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
          val s = response.status.value
          s == 0 || s >= 500
        }
        exponentialDelay()
      }

      // Base URL configuration
      defaultRequest {
        val base = NetworkConfig.baseUrl.trimEnd('/')
        url(base)
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
    }.also { client ->
      // Dynamic Auth Header Injection via HttpSend plugin
      // This ensures we get the CURRENT token for each request
      if (tokenProvider != null) {
        client.plugin(HttpSend).intercept { request ->
          try {
            val token = tokenProvider.getAccessToken()
            if (token != null) {
              request.header("Authorization", "Bearer $token")
            }
          } catch (e: Exception) {
            println("[apiClient] Error getting access token: $e")
          }
          execute(request)
        }
      }
    }
  }
}
