package at.mocode

import at.mocode.API_HOST

// Top-level function for JavaScript interop
@JsName("checkProductionEnvironment")
private fun checkProductionEnvironment(): Boolean = js("typeof window !== 'undefined' && window.location && window.location.hostname !== 'localhost'")

actual object PlatformInfo {
    actual val apiHost: String
        get() {
            // In Docker, we need to use the service name "backend" instead of "localhost"
            // Check if we're running in a production environment (Docker)
            return if (checkProductionEnvironment() as Boolean) {
                // Use the service name from docker-compose.yml
                API_HOST // "backend"
            } else {
                // Use localhost for development
                "localhost"
            }
        }
}
