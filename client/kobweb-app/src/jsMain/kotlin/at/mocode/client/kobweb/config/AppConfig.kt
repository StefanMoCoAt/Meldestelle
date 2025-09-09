package at.mocode.client.kobweb.config

/**
 * Application configuration for the Kobweb client.
 * Provides centralized configuration management to avoid hardcoded values.
 */
object AppConfig {
    /**
     * Base URL for the backend services.
     * Can be overridden via environment variables or build configuration.
     */
    val baseUrl: String = getBaseUrl()

    /**
     * Application title
     */
    const val APP_TITLE = "Meldestelle Kobweb Application"

    /**
     * Default timeout for network requests in milliseconds
     */
    const val DEFAULT_TIMEOUT = 10_000L

    /**
     * Gets the base URL from various sources with fallback hierarchy:
     * 1. Runtime environment variable
     * 2. Build-time configuration
     * 3. Default localhost for development
     */
    private fun getBaseUrl(): String {
        // Check for runtime configuration (if available in browser environment)
        val runtimeUrl = js("typeof window !== 'undefined' ? window.location.origin : null") as? String

        // For development, use localhost backend
        // In production, this should be configured during build or deployment
        return when {
            !runtimeUrl.isNullOrBlank() && runtimeUrl != "null" -> {
                // In production, backend might be on same origin or configured path
                if (runtimeUrl.contains("localhost") || runtimeUrl.contains("127.0.0.1")) {
                    "http://localhost:8081" // Development backend
                } else {
                    "$runtimeUrl/api" // Production backend on same origin
                }
            }
            else -> "http://localhost:8081" // Fallback for development
        }
    }
}
