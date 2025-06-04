package at.mocode.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import at.mocode.PlatformInfo
import at.mocode.SERVER_PORT
import io.ktor.client.request.request

/**
 * Shared HTTP client instance for making API requests across the application.
 *
 * Features:
 * - Content negotiation with JSON serialization/deserialization
 * - Platform-specific host configuration through PlatformInfo
 * - Consistent port configuration using SERVER_PORT constant
 *
 * This client is used by all screens that need to communicate with the backend API.
 */
val httpClient = HttpClient {
    // Install content negotiation plugin to handle JSON serialization/deserialization
    install(ContentNegotiation) {
        json() // Uses kotlinx.serialization for JSON processing
    }

    // Configure the HTTP engine with platform-specific settings
    engine {
        request {
            // Configure default URL components for all requests
            url {
                // Use HTTP protocol for all requests
                protocol = URLProtocol.HTTP

                // Use platform-specific host:
                // - In JVM (desktop): Uses API_HOST constant ("backend")
                // - In WASM/JS (browser): Uses "localhost" in development, API_HOST in production
                host = PlatformInfo.apiHost

                // Use the standard server port defined in Constants.kt
                port = SERVER_PORT
            }
        }
    }
}
