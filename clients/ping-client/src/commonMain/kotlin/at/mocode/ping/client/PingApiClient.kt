package at.mocode.ping.client

import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import at.mocode.service.getBaseUrl

class PingApiClient(
    private val client: HttpClient,
    baseUrl: String = getBaseUrl()
) : PingApi {
    private val base = "$baseUrl/api/ping"

    override suspend fun simplePing(): PingResponse = client.get("$base/simple").body()

    override suspend fun enhancedPing(simulate: Boolean): EnhancedPingResponse =
        client.get("$base/enhanced") { parameter("simulate", simulate) }.body()

    override suspend fun healthCheck(): HealthResponse = client.get("$base/health").body()
}
