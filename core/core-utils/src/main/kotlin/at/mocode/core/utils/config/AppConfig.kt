package at.mocode.core.utils.config

/**
 * Eine reine, unveränderliche Datenhalte-Klasse für die gesamte Anwendungskonfiguration.
 * Wird vom ConfigLoader instanziiert.
 */
data class AppConfig(
    val environment: AppEnvironment,
    val appInfo: AppInfoConfig,
    val server: ServerConfig,
    val database: DatabaseConfig,
    val serviceDiscovery: ServiceDiscoveryConfig,
    val security: SecurityConfig,
    val logging: LoggingConfig,
    val rateLimit: RateLimitConfig
)

data class AppInfoConfig(val name: String, val version: String, val description: String)

data class ServerConfig(
    val port: Int,
    val host: String,
    val advertisedHost: String,
    val workers: Int,
    val cors: CorsConfig
) {
    data class CorsConfig(val enabled: Boolean, val allowedOrigins: List<String>)
}

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val name: String,
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String,
    val maxPoolSize: Int,
    val minPoolSize: Int,
    val autoMigrate: Boolean
)

data class ServiceDiscoveryConfig(val enabled: Boolean, val consulHost: String, val consulPort: Int)

data class SecurityConfig(val jwt: JwtConfig, val apiKey: String?) {
    data class JwtConfig(
        val secret: String,
        val issuer: String,
        val audience: String,
        val realm: String,
        val expirationInMinutes: Long
    )
}

data class LoggingConfig(val level: String, val logRequests: Boolean, val logResponses: Boolean)

data class RateLimitConfig(val enabled: Boolean, val globalLimit: Int, val globalPeriodMinutes: Int)
