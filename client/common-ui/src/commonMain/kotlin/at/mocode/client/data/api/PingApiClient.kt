package at.mocode.client.data.api

import at.mocode.client.data.model.PingResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class PingApiClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun ping(): String {
        return try {
            // HINWEIS: Wir rufen hier Port 8081 an, den Port unseres Gateways.
            val response = httpClient.get("http://localhost:8081/ping").body<PingResponse>()
            response.status
        } catch (e: Exception) {
            "Fehler: ${e.message}"
        }
    }
}
