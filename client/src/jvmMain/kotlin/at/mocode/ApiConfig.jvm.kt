package at.mocode

actual object ApiConfig {
    actual val baseUrl: String = System.getenv("API_BASE_URL") ?: "http://localhost:8081"
    actual val pingEndpoint: String = "$baseUrl/api/ping"
}
