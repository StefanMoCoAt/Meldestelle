package at.mocode.clients.pingfeature

import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class PingApiClient(
    private val baseUrl: String = "http://localhost:8081"
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
}
