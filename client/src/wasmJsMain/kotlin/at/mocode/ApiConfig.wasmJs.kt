package at.mocode

actual object ApiConfig {
    actual val baseUrl: String = "" // Same-origin für Nginx-Proxy
    actual val pingEndpoint: String = "/api/ping"
}
