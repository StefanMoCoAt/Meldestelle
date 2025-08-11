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

data class AppInfoConfig(
    val name: ApplicationName,
    val version: ApplicationVersion,
    val description: String
)

data class ServerConfig(
    val port: Port,
    val host: Host,
    val advertisedHost: Host,
    val workers: WorkerCount,
    val cors: CorsConfig
) {
    data class CorsConfig(val enabled: Boolean, val allowedOrigins: List<String>)
}

data class DatabaseConfig(
    val host: Host,
    val port: Port,
    val name: DatabaseName,
    val jdbcUrl: JdbcUrl,
    val username: DatabaseUsername,
    val password: DatabasePassword,
    val driverClassName: String,
    val maxPoolSize: PoolSize,
    val minPoolSize: PoolSize,
    val autoMigrate: Boolean
)

data class ServiceDiscoveryConfig(
    val enabled: Boolean,
    val consulHost: Host,
    val consulPort: Port
)

data class SecurityConfig(val jwt: JwtConfig, val apiKey: ApiKey?) {
    data class JwtConfig(
        val secret: JwtSecret,
        val issuer: JwtIssuer,
        val audience: JwtAudience,
        val realm: JwtRealm,
        val expirationInMinutes: Long
    )
}

data class LoggingConfig(val level: String, val logRequests: Boolean, val logResponses: Boolean)

data class RateLimitConfig(
    val enabled: Boolean,
    val globalLimit: RateLimit,
    val globalPeriodMinutes: PeriodMinutes
)
