package at.mocode.ping.feature.integration

import at.mocode.frontend.core.sync.SyncManager
import at.mocode.ping.feature.domain.PingSyncServiceImpl
import at.mocode.ping.feature.test.FakePingEventRepository
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PingSyncIntegrationTest {

  @Test
  fun `syncPings should fetch data from API and store in repository`() = runTest {
    // Given
    val fakeRepo = FakePingEventRepository()

    // Mock API Response
    val mockEngine = MockEngine { request ->
      assertEquals("/api/ping/sync", request.url.encodedPath)

      val responseBody = """
        [
          {
            "id": "event-1",
            "message": "Ping 1",
            "lastModified": 1000
          },
          {
            "id": "event-2",
            "message": "Ping 2",
            "lastModified": 2000
          }
        ]
      """.trimIndent()

      respond(
        content = responseBody,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
      )
    }

    val httpClient = HttpClient(mockEngine) {
      install(ContentNegotiation) {
        json(Json {
          ignoreUnknownKeys = true
          isLenient = true
        })
      }
    }

    val syncManager = SyncManager(httpClient)
    val syncService = PingSyncServiceImpl(syncManager, fakeRepo)

    // When
    syncService.syncPings()

    // Then
    assertEquals(2, fakeRepo.storedEvents.size)
    assertTrue(fakeRepo.storedEvents.any { it.id == "event-1" && it.message == "Ping 1" })
    assertTrue(fakeRepo.storedEvents.any { it.id == "event-2" && it.message == "Ping 2" })
  }
}
