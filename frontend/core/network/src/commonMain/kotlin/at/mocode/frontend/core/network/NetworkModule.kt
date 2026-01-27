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
    // We use a provider lambda to get the TokenProvider instance when needed
    // This avoids resolving it immediately during module definition

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
      client.plugin(HttpSend).intercept { request ->
        try {
          // Resolve TokenProvider dynamically from Koin scope
          // This assumes Koin is initialized and accessible
          // Since we are inside a Koin component, we should be able to get it?
          // No, 'this' here is HttpSendScope.

          // We need to capture the Koin scope or use GlobalContext if necessary,
          // BUT better: we inject the TokenProvider into the module definition lambda
          // and use it here.

          // However, `get<TokenProvider>()` might fail if not yet registered.
          // Let's try to resolve it safely.

          // The issue with the previous code was likely that `get<TokenProvider>()` was called
          // during module definition time (or bean creation time), and if it wasn't ready or
          // if it was null (due to try-catch), the interceptor logic was skipped or broken.

          // Let's try to get it from the Koin instance that created this client.
          // But we are inside `single { ... }`.

          // We can capture the `Scope` from the `single` block.
          // val scope = this // Koin Scope

          // But we can't easily pass `scope` into `intercept`.

          // Let's try to resolve TokenProvider lazily using a lazy delegate or similar.
          // Or just resolve it inside the interceptor if we can access Koin.

          // Since we are in `single`, we can get the provider.
          // The previous error `TypeError: this.getToken_wiq2bn_k$ is not a function`
          // was in AuthModule, which we fixed.

          // The current error `Error_0: Fail to fetch` is a CORS error on the network level,
          // NOT a JS runtime error in the interceptor (unless the interceptor causes it).

          // Wait, the logs show:
          // [baseClient] REQUEST: .../token
          // Access to fetch at ... blocked by CORS policy

          // This confirms it is a CORS issue on the Keycloak server side, or the browser side.
          // The JS error `TypeError` is GONE in the latest log!

          // So the interceptor logic in NetworkModule might be fine, or at least not the cause of the CORS error.
          // But let's make it robust anyway.

          // We will use a safe lazy resolution pattern.
        } catch (_: Exception) {
           // ignore
        }
        execute(request)
      }

      // Re-applying the logic with proper Koin resolution
      val koinScope = this@single

      client.plugin(HttpSend).intercept { request ->
        try {
            // Attempt to resolve TokenProvider from the capturing scope
            val tokenProvider = try {
                koinScope.get<TokenProvider>()
            } catch (_: Exception) {
                null
            }

            val token = tokenProvider?.getAccessToken()
            if (token != null) {
                request.header("Authorization", "Bearer $token")
            }
        } catch (e: Exception) {
            println("[apiClient] Error injecting auth header: $e")
        }
        execute(request)
      }
    }
  }
}
