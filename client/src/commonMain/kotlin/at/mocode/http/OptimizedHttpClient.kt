package at.mocode.http

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Optimized HTTP Client für minimale Bundle-Größe
 * Enthält nur die minimal notwendigen Features für Ping-Service
 */
object OptimizedHttpClient {

    /**
     * Erstellt einen minimalen HTTP Client mit nur den notwendigen Features
     * - ContentNegotiation für JSON
     * - Minimale JSON-Konfiguration
     * - Keine unnötigen Plugins oder Features
     */
    fun createMinimalClient(): HttpClient {
        return HttpClient {
            // Nur ContentNegotiation für JSON - keine anderen Plugins
            install(ContentNegotiation) {
                json(Json {
                    // Minimale JSON-Konfiguration für kleinste Bundle-Größe
                    ignoreUnknownKeys = true
                    isLenient = false
                    encodeDefaults = false
                    // Keine pretty printing für Production
                    prettyPrint = false
                    // Keine explicitNulls für kleinere Payloads
                    explicitNulls = false
                })
            }

            // Explizit keine anderen Features installieren:
            // - Kein Logging (spart Bundle-Größe)
            // - Kein DefaultRequest (nicht benötigt für einfachen Ping)
            // - Kein Timeout (Browser/Platform Default verwenden)
            // - Kein Auth (Ping-Service ist öffentlich)
            // - Keine Cookies (nicht benötigt)
            // - Keine Compression (nicht benötigt für kleine Payloads)
        }
    }

    /**
     * Platform-optimierter Client (vereinfacht für alle Platforms)
     * Verwendet minimale Konfiguration für alle Targets
     */
    fun createPlatformOptimizedClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(createMinimalJson())
            }
            // Einheitliche Optimierungen für alle Platforms
            expectSuccess = false // Keine Exception bei HTTP-Errors (spart Bundle-Größe)
        }
    }

    /**
     * Minimale JSON-Konfiguration für kleinste Serialization-Overhead
     */
    private fun createMinimalJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = false
            encodeDefaults = false
            prettyPrint = false
            explicitNulls = false
            // Klassennamen nicht einbetten (spart Bytes)
            classDiscriminator = ""
            // Keine Polymorphie für einfache DTOs
            useAlternativeNames = false
        }
    }

}

/**
 * Lazy HTTP Client Instance für optimale Performance
 * Erstellt den Client nur einmal bei erster Verwendung
 */
class LazyHttpClient {
    private var _client: HttpClient? = null

    val client: HttpClient
        get() {
            if (_client == null) {
                _client = OptimizedHttpClient.createPlatformOptimizedClient()
            }
            return _client!!
        }

    fun close() {
        _client?.close()
        _client = null
    }
}

/**
 * Globale Singleton-Instanz für den optimierten HTTP Client
 * Minimiert Memory-Overhead und Bundle-Größe
 */
object GlobalHttpClient {
    private val lazyClient = LazyHttpClient()

    /**
     * Zugriff auf den optimierten HTTP Client
     */
    val client: HttpClient
        get() = lazyClient.client

    /**
     * Client schließen bei App-Beendigung
     */
    fun cleanup() {
        lazyClient.close()
    }
}
