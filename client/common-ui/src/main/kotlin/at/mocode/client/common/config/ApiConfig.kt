package at.mocode.client.common.config

/**
 * Configuration class for API client settings.
 * Allows for environment-specific configuration.
 */
data class ApiConfig(
    val baseUrl: String = System.getProperty("api.base.url") ?: System.getenv("API_BASE_URL") ?: "http://localhost:8080",
    val requestTimeoutMs: Long = System.getProperty("api.timeout")?.toLongOrNull() ?: 30_000L,
    val cacheTtlMs: Long = System.getProperty("api.cache.ttl")?.toLongOrNull() ?: 30_000L,
    val maxCacheSize: Int = System.getProperty("api.cache.max.size")?.toIntOrNull() ?: 1000,
    val enableLogging: Boolean = System.getProperty("api.logging.enabled")?.toBoolean() ?: false
)
