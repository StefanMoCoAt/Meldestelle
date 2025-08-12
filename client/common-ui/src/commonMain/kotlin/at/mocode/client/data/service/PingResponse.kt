package at.mocode.client.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

@Serializable
data class PingResponse(val status: String)

class PingService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun ping(): Result<PingResponse> = try {
        val response = client.get("http://localhost:8082/ping").body<PingResponse>()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun pingFlow(): Flow<Result<PingResponse>> = flow {
        emit(ping())
    }
}
