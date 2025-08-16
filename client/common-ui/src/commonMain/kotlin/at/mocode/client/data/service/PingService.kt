package at.mocode.client.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

class PingService(
    private val baseUrl: String = "http://localhost:8080",
    private val httpClient: HttpClient = createDefaultHttpClient()
) {
    suspend fun ping(): Result<PingResponse> = try {
        val response = httpClient.get("$baseUrl/api/ping/ping").body<PingResponse>()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun close() {
        httpClient.close()
    }

    companion object {
        fun createDefaultHttpClient(): HttpClient = HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }
}
