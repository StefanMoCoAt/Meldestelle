package at.mocode.shared.data.repository

import at.mocode.shared.domain.model.PingData
import at.mocode.shared.domain.model.Resource
import at.mocode.shared.domain.repository.PingRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class PingRepositoryImpl(
  private val httpClient: HttpClient
) : PingRepository {

  override suspend fun checkSystemStatus(): Resource<PingData> {
    return try {
      // Der HttpClient hat die BaseURL schon konfiguriert (siehe NetworkModule)
      val response = httpClient.get("/api/ping/simple").body<PingData>()
      Resource.Success(response)
    } catch (e: Exception) {
      // Hier fangen wir Netzwerkfehler ab und machen sie "hübsch" für die UI
      Resource.Error(
        message = "Verbindung fehlgeschlagen: ${e.message ?: "Unbekannter Fehler"}",
        code = "NETWORK_ERROR"
      )
    }
  }
}
