package at.mocode

import at.mocode.API_HOST

/**
 * JavaScript interop function to determine if the application is running in a production environment.
 * Returns true if the hostname is not 'localhost'.
 */
@JsName("checkProductionEnvironment")
private fun checkProductionEnvironment(): Boolean = js("typeof window !== 'undefined' && window.location && window.location.hostname !== 'localhost'")

/**
 * WebAssembly JavaScript implementation of PlatformInfo.
 * Provides platform-specific information for the WebAssembly JavaScript target.
 */
actual object PlatformInfo {
    /**
     * Returns the appropriate API host based on the environment:
     * - In production: Uses the API_HOST constant (backend service name from docker-compose.yml)
     * - In development: Uses 'localhost'
     */
    actual val apiHost: String
        get() {
            return if (checkProductionEnvironment() as Boolean) {
                API_HOST
            } else {
                "localhost"
            }
        }
}
