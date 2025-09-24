package at.mocode.service

import at.mocode.model.EnhancedPingResponse
import at.mocode.model.HealthResponse
import at.mocode.model.PingResponse
import at.mocode.ping.client.PingApiClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Deprecated("Use PingApiClient directly for new code")
class PingService(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 5000
        }
    }
) {
    private val api = PingApiClient(client)

    suspend fun ping(): Result<PingResponse> = runCatching { api.simplePing() }

    suspend fun enhancedPing(simulate: Boolean = false): Result<EnhancedPingResponse> =
        runCatching { api.enhancedPing(simulate) }

    suspend fun health(): Result<HealthResponse> = runCatching { api.healthCheck() }

    suspend fun testFailure(): Result<EnhancedPingResponse> = runCatching {
        throw RuntimeException("Simulated failure for testing")
    }
}

// Platform-specific base URL required by PingApiClient via getBaseUrl()
expect fun getBaseUrl(): String
