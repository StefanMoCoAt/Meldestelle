package at.mocode.shared.di

import at.mocode.shared.core.AppConfig
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
  // 1. JSON Konfiguration (Global verfügbar)
  single {
    Json {
      ignoreUnknownKeys = true
      prettyPrint = true
      isLenient = true
    }
  }

  // 2. HttpClient (Singleton)
  single {
    val config = get<AppConfig>()
    val jsonConfig = get<Json>()

    HttpClient {
      // Standard-URL setzen
      defaultRequest {
        url(config.gatewayUrl)
        contentType(ContentType.Application.Json)
      }

      install(ContentNegotiation) {
        json(jsonConfig)
      }

      install(Logging) {
        level = if (config.isDebug) LogLevel.INFO else LogLevel.NONE
        logger = Logger.DEFAULT
      }

      install(HttpTimeout) {
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 10000
      }
    }
  }
}
