package at.mocode.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import at.mocode.PlatformInfo
import at.mocode.SERVER_PORT
import io.ktor.client.request.request

/**
 * Ktor HTTP Client Instance for making API requests
 * Uses platform-specific configuration through PlatformInfo
 */
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json() // Uses kotlinx.serialization
    }

    // Configure the engine for absolute URLs
    engine {
        // For WASM/JS client
        request {
            // Set the base URL for all requests
            url {
                protocol = URLProtocol.HTTP
                // Use the host from the platform-specific implementation
                // PlatformInfo.apiHost returns "backend" for JVM and "localhost" for WASM
                host = PlatformInfo.apiHost
                port = SERVER_PORT
            }
        }
    }
}
