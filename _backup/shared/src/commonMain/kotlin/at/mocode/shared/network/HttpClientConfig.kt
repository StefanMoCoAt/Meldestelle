package at.mocode.shared.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientConfig {

  fun createClient(
    baseUrl: String = "http://localhost:8080"
  ): HttpClient = HttpClient {

    // Content negotiation with JSON (based on PingApiClient pattern)
    install(ContentNegotiation) {
      json(Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
      })
    }
  }

  fun createClientWithBaseUrl(baseUrl: String): HttpClient {
    return createClient(baseUrl)
  }
}
