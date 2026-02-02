package at.mocode.ping.feature.data

import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingEvent
import at.mocode.ping.api.PingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * PingApi-Implementierung, die einen bereitgestellten HttpClient verwendet (z. B. den per Dependency Injection
 * bereitgestellten "apiClient").
 */
class PingApiKoinClient(private val client: HttpClient) : PingApi {

  override suspend fun simplePing(): PingResponse {
    return client.get("/api/ping/simple").body()
  }

  override suspend fun enhancedPing(simulate: Boolean): EnhancedPingResponse {
    return client.get("/api/ping/enhanced") {
      url.parameters.append("simulate", simulate.toString())
    }.body()
  }

  override suspend fun healthCheck(): HealthResponse {
    return client.get("/api/ping/health").body()
  }

  override suspend fun publicPing(): PingResponse {
    return client.get("/api/ping/public").body()
  }

  override suspend fun securePing(): PingResponse {
    return client.get("/api/ping/secure").body()
  }

  override suspend fun syncPings(since: Long): List<PingEvent> {
    return client.get("/api/ping/sync") {
      url.parameters.append("lastSyncTimestamp", since.toString())
    }.body()
  }
}
