package at.mocode.core.utils.config

import java.io.File
import java.net.InetAddress
import java.util.Properties

/**
 * Zentrale, unveränderliche Konfigurations-Klasse für die Anwendung.
 * Hält alle Konfigurationswerte, die beim Start eines Service geladen werden.
 */
class AppConfig(
    val environment: AppEnvironment,
    val appInfo: AppInfoConfig,
    val server: ServerConfig,
    val database: DatabaseConfig,
    val serviceDiscovery: ServiceDiscoveryConfig,
    val security: SecurityConfig,
    val logging: LoggingConfig,
    val rateLimit: RateLimitConfig
) {
    companion object {
        fun load(): AppConfig {
            val environment = AppEnvironment.current()
            val props = loadProperties(environment)

            return AppConfig(
                environment = environment,
                appInfo = AppInfoConfig.fromProperties(props),
                server = ServerConfig.fromProperties(props),
                database = DatabaseConfig.fromProperties(props),
                serviceDiscovery = ServiceDiscoveryConfig.fromProperties(props),
                security = SecurityConfig.fromProperties(props),
                logging = LoggingConfig.fromProperties(props, environment),
                rateLimit = RateLimitConfig.fromProperties(props)
            )
        }

        private fun loadProperties(environment: AppEnvironment): Properties {
            val props = Properties()
            loadPropertiesFile("application.properties", props)
            val envFile = "application-${environment.name.lowercase()}.properties"
            loadPropertiesFile(envFile, props)
            return props
        }

        private fun loadPropertiesFile(filename: String, props: Properties) {
            val resourceStream = AppConfig::class.java.classLoader.getResourceAsStream(filename)
            if (resourceStream != null) {
                resourceStream.use { props.load(it) }
                return
            }
            val file = File("config/$filename")
            if (file.exists()) {
                file.inputStream().use { props.load(it) }
            }
        }
    }
}

data class AppInfoConfig(val name: String, val version: String, val description: String) {
    companion object {
        fun fromProperties(props: Properties) = AppInfoConfig(
            name = props.getProperty("app.name", "Meldestelle"),
            version = props.getProperty("app.version", "1.0.0"),
            description = props.getProperty("app.description", "Pferdesport Meldestelle System")
        )
    }
}

data class ServerConfig(
    val port: Int,
    val host: String,
    val advertisedHost: String,
    val workers: Int,
    val cors: CorsConfig
) {
    companion object {
        fun fromProperties(props: Properties): ServerConfig {
            val defaultHost = try { InetAddress.getLocalHost().hostAddress } catch (_: Exception) { "127.0.0.1" }
            return ServerConfig(
                port = props.getIntProperty("server.port", "API_PORT", 8081),
                host = props.getStringProperty("server.host", "API_HOST", "0.0.0.0"),
                advertisedHost = props.getStringProperty("server.advertisedHost", "API_HOST_ADVERTISED", defaultHost),
                workers = props.getIntProperty("server.workers", "API_WORKERS", Runtime.getRuntime().availableProcessors()),
                cors = CorsConfig.fromProperties(props)
            )
        }
    }
    data class CorsConfig(val enabled: Boolean, val allowedOrigins: List<String>) {
        companion object {
            fun fromProperties(props: Properties) = CorsConfig(
                enabled = props.getBooleanProperty("server.cors.enabled", "API_CORS_ENABLED", true),
                allowedOrigins = props.getProperty("server.cors.allowedOrigins")?.split(",")?.map { it.trim() } ?: listOf("*")
            )
        }
    }
}

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driverClassName: String,
    val maxPoolSize: Int,
    val minPoolSize: Int,
    val autoMigrate: Boolean
) {
    companion object {
        fun fromProperties(props: Properties): DatabaseConfig {
            val host = props.getStringProperty("database.host", "DB_HOST", "localhost")
            val port = props.getIntProperty("database.port", "DB_PORT", 5432)
            val name = props.getStringProperty("database.name", "DB_NAME", "meldestelle_db")
            return DatabaseConfig(
                jdbcUrl = "jdbc:postgresql://$host:$port/$name",
                username = props.getStringProperty("database.username", "DB_USER", "meldestelle_user"),
                password = props.getStringProperty("database.password", "DB_PASSWORD", "secure_password_change_me"),
                driverClassName = "org.postgresql.Driver",
                maxPoolSize = props.getIntProperty("database.maxPoolSize", "DB_MAX_POOL_SIZE", 10),
                minPoolSize = props.getIntProperty("database.minPoolSize", "DB_MIN_POOL_SIZE", 5),
                autoMigrate = props.getBooleanProperty("database.autoMigrate", "DB_AUTO_MIGRATE", true)
            )
        }
    }
}

data class ServiceDiscoveryConfig(val enabled: Boolean, val consulHost: String, val consulPort: Int) {
    companion object {
        fun fromProperties(props: Properties) = ServiceDiscoveryConfig(
            enabled = props.getBooleanProperty("service-discovery.enabled", "CONSUL_ENABLED", true),
            consulHost = props.getStringProperty("service-discovery.consul.host", "CONSUL_HOST", "consul"),
            consulPort = props.getIntProperty("service-discovery.consul.port", "CONSUL_PORT", 8500)
        )
    }
}

data class SecurityConfig(val jwt: JwtConfig, val apiKey: String?) {
    companion object {
        fun fromProperties(props: Properties) = SecurityConfig(
            jwt = JwtConfig.fromProperties(props),
            apiKey = props.getStringProperty("security.apiKey", "API_KEY", "").ifEmpty { null }
        )
    }
    data class JwtConfig(val secret: String, val issuer: String, val audience: String, val realm: String, val expirationInMinutes: Long) {
        companion object {
            fun fromProperties(props: Properties) = JwtConfig(
                secret = props.getStringProperty("security.jwt.secret", "JWT_SECRET", "default-secret-please-change-in-production"),
                issuer = props.getStringProperty("security.jwt.issuer", "JWT_ISSUER", "meldestelle-api"),
                audience = props.getStringProperty("security.jwt.audience", "JWT_AUDIENCE", "meldestelle-clients"),
                realm = props.getStringProperty("security.jwt.realm", "JWT_REALM", "meldestelle"),
                expirationInMinutes = props.getLongProperty("security.jwt.expirationInMinutes", "JWT_EXPIRATION_MINUTES", 60 * 24)
            )
        }
    }
}

data class LoggingConfig(val level: String, val logRequests: Boolean, val logResponses: Boolean) {
    companion object {
        fun fromProperties(props: Properties, env: AppEnvironment) = LoggingConfig(
            level = props.getStringProperty("logging.level", "LOG_LEVEL", if (env.isProduction()) "INFO" else "DEBUG"),
            logRequests = props.getBooleanProperty("logging.requests", "LOG_REQUESTS", true),
            logResponses = props.getBooleanProperty("logging.responses", "LOG_RESPONSES", !env.isProduction())
        )
    }
}

data class RateLimitConfig(val enabled: Boolean, val globalLimit: Int, val globalPeriodMinutes: Int) {
    companion object {
        fun fromProperties(props: Properties) = RateLimitConfig(
            enabled = props.getBooleanProperty("ratelimit.enabled", "RATE_LIMIT_ENABLED", true),
            globalLimit = props.getIntProperty("ratelimit.global.limit", "RATE_LIMIT_GLOBAL_LIMIT", 100),
            globalPeriodMinutes = props.getIntProperty("ratelimit.global.periodMinutes", "RATE_LIMIT_GLOBAL_PERIOD", 1)
        )
    }
}
