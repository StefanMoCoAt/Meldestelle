package at.mocode.clients.pingfeature

import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingEvent
import at.mocode.shared.core.AppConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Legacy PingApiClient - deprecated in favor of PingApiKoinClient which uses the shared authenticated HttpClient.
 * Kept for backward compatibility or standalone testing if needed.
 */
// @Deprecated("Use PingApiKoinClient with DI instead") // Deprecation removed for cleaner build logs during transition
class PingApiClient(
  private val baseUrl: String = AppConstants.GATEWAY_URL
) : PingApi {

  private val client = HttpClient {
    install(ContentNegotiation) {
      json(Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
      })
    }
  }

  override suspend fun simplePing(): PingResponse {
    return client.get("$baseUrl/api/ping/simple").body()
  }

  override suspend fun enhancedPing(simulate: Boolean): EnhancedPingResponse {
    return client.get("$baseUrl/api/ping/enhanced") {
      parameter("simulate", simulate)
    }.body()
  }

  override suspend fun healthCheck(): HealthResponse {
    return client.get("$baseUrl/api/ping/health").body()
  }

  override suspend fun publicPing(): PingResponse {
    return client.get("$baseUrl/api/ping/public").body()
  }

  override suspend fun securePing(): PingResponse {
    return client.get("$baseUrl/api/ping/secure").body()
  }

  override suspend fun syncPings(lastSyncTimestamp: Long): List<PingEvent> {
    return client.get("$baseUrl/api/ping/sync") {
      parameter("lastSyncTimestamp", lastSyncTimestamp)
    }.body()
  }
}
