package at.mocode.ping.feature.data

import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingResponse
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PingApiKoinClientTest {

  // Helper to create a testable client using the new DI-friendly implementation
  private fun createTestClient(mockEngine: MockEngine): PingApiKoinClient {
    val client = HttpClient(mockEngine) {
      install(ContentNegotiation) {
        json(Json {
          prettyPrint = true
          isLenient = true
          ignoreUnknownKeys = true
        })
      }
    }
    return PingApiKoinClient(client)
  }

  @Test
  fun `simplePing should return correct response`() = runTest {
    // Given
    val expectedResponse = PingResponse(
      status = "OK",
      timestamp = "2025-09-27T21:27:00Z",
      service = "ping-service"
    )

    val mockEngine = MockEngine { request ->
      assertEquals("/api/ping/simple", request.url.encodedPath)
      assertEquals(HttpMethod.Get, request.method)

      respond(
        content = Json.encodeToString(PingResponse.serializer(), expectedResponse),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
      )
    }

    // When
    val apiClient = createTestClient(mockEngine)
    val response = apiClient.simplePing()

    // Then
    assertEquals(expectedResponse, response)
  }

  @Test
  fun `enhancedPing should include simulate parameter`() = runTest {
    // Given
    val expectedResponse = EnhancedPingResponse(
      status = "OK",
      timestamp = "2025-09-27T21:27:00Z",
      service = "ping-service",
      circuitBreakerState = "CLOSED",
      responseTime = 42L
    )

    val mockEngine = MockEngine { request ->
      assertEquals("/api/ping/enhanced", request.url.encodedPath)
      assertEquals("true", request.url.parameters["simulate"])
      assertEquals(HttpMethod.Get, request.method)

      respond(
        content = Json.encodeToString(EnhancedPingResponse.serializer(), expectedResponse),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
      )
    }

    // When
    val apiClient = createTestClient(mockEngine)
    val response = apiClient.enhancedPing(simulate = true)

    // Then
    assertEquals(expectedResponse, response)
  }

  @Test
  fun `healthCheck should return health response`() = runTest {
    // Given
    val expectedResponse = HealthResponse(
      status = "UP",
      timestamp = "2025-09-27T21:27:00Z",
      service = "ping-service",
      healthy = true
    )

    val mockEngine = MockEngine { request ->
      assertEquals("/api/ping/health", request.url.encodedPath)
      assertEquals(HttpMethod.Get, request.method)

      respond(
        content = Json.encodeToString(HealthResponse.serializer(), expectedResponse),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
      )
    }

    // When
    val apiClient = createTestClient(mockEngine)
    val response = apiClient.healthCheck()

    // Then
    assertEquals(expectedResponse, response)
  }

  @Test
  fun `JSON serialization should work correctly`() {
    // Given
    val pingResponse = PingResponse(
      status = "OK",
      timestamp = "2025-09-27T21:27:00Z",
      service = "test-service"
    )

    // When
    val json = Json.encodeToString(PingResponse.serializer(), pingResponse)
    val deserializedResponse = Json.decodeFromString(PingResponse.serializer(), json)

    // Then
    assertEquals(pingResponse, deserializedResponse)
  }

  @Test
  fun `Enhanced ping response serialization should work correctly`() {
    // Given
    val enhancedResponse = EnhancedPingResponse(
      status = "OK",
      timestamp = "2025-09-27T21:27:00Z",
      service = "test-service",
      circuitBreakerState = "CLOSED",
      responseTime = 123L
    )

    // When
    val json = Json.encodeToString(EnhancedPingResponse.serializer(), enhancedResponse)
    val deserializedResponse = Json.decodeFromString(EnhancedPingResponse.serializer(), json)

    // Then
    assertEquals(enhancedResponse, deserializedResponse)
  }

  @Test
  fun `Health response serialization should work correctly`() {
    // Given
    val healthResponse = HealthResponse(
      status = "UP",
      timestamp = "2025-09-27T21:27:00Z",
      service = "test-service",
      healthy = true
    )

    // When
    val json = Json.encodeToString(HealthResponse.serializer(), healthResponse)
    val deserializedResponse = Json.decodeFromString(HealthResponse.serializer(), json)

    // Then
    assertEquals(healthResponse, deserializedResponse)
  }
}
