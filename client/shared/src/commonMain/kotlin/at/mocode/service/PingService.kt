package at.mocode.service

import at.mocode.model.EnhancedPingResponse
import at.mocode.model.HealthResponse
import at.mocode.model.PingResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class PingService {

    private val client = HttpClient {
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

    private val baseUrl = getBaseUrl()

    suspend fun ping(): Result<PingResponse> = runCatching {
        client.get("$baseUrl/ping").body<PingResponse>()
    }

    suspend fun enhancedPing(simulate: Boolean = false): Result<EnhancedPingResponse> = runCatching {
        // Fallback: Use simple ping and enhance response locally
        val response = client.get("$baseUrl/ping").body<PingResponse>()
        EnhancedPingResponse(
            status = response.status,
            timestamp = response.timestamp,
            service = response.service,
            circuitBreakerState = if (simulate) "OPEN" else "CLOSED",
            responseTime = 100L
        )
    }

    suspend fun health(): Result<HealthResponse> = runCatching {
        // Fallback: Use simple ping to determine health
        val response = client.get("$baseUrl/ping").body<PingResponse>()
        HealthResponse(
            status = response.status,
            timestamp = response.timestamp,
            service = response.service,
            healthy = response.status == "pong"
        )
    }

    suspend fun testFailure(): Result<EnhancedPingResponse> = runCatching {
        // Simulate failure for testing
        throw RuntimeException("Simulated failure for testing")
    }
}

// Platform-specific base URL
expect fun getBaseUrl(): String
