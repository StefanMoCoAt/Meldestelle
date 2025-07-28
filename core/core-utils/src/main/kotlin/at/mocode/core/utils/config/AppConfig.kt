package at.mocode.core.utils.config

import at.mocode.core.utils.database.DatabaseConfig
import java.io.File
import java.util.Properties

/**
 * Zentrale Konfigurations-Klasse für die Anwendung.
 * Hält alle Konfigurationswerte, die beim Start des Service explizit geladen werden.
 */
class AppConfig(
    val environment: AppEnvironment,
    val appInfo: AppInfoConfig,
    val server: ServerConfig,
    val security: SecurityConfig,
    val logging: LoggingConfig,
    val rateLimit: RateLimitConfig,
    val serviceDiscovery: ServiceDiscoveryConfig,
    val database: DatabaseConfig
) {
    companion object {
        /**
         * Factory-Methode, die eine AppConfig-Instanz durch das Laden von
         * .properties-Dateien und Umgebungsvariablen erstellt.
         * Dies ist der zentrale Einstiegspunkt, um die Konfiguration zu laden.
         */
        fun load(): AppConfig {
            val environment = AppEnvironment.current()
            val props = loadProperties(environment)

            return AppConfig(
                environment = environment,
                appInfo = AppInfoConfig.fromProperties(props),
                server = ServerConfig.fromProperties(props),
                security = SecurityConfig.fromProperties(props),
                logging = LoggingConfig.fromProperties(props, environment),
                rateLimit = RateLimitConfig.fromProperties(props),
                serviceDiscovery = ServiceDiscoveryConfig.fromProperties(props),
                database = DatabaseConfig.fromProperties(props)
            )
        }

        private fun loadProperties(environment: AppEnvironment): Properties {
            val props = Properties()

            // Lade Basis-Properties
            loadPropertiesFile("application.properties", props)

            // Lade umgebungsspezifische Properties
            val envFile = "application-${environment.name.lowercase()}.properties"
            loadPropertiesFile(envFile, props)

            return props
        }

        private fun loadPropertiesFile(filename: String, props: Properties) {
            val resourceStream = javaClass.classLoader.getResourceAsStream(filename)
            if (resourceStream != null) {
                props.load(resourceStream)
                resourceStream.close()
            } else {
                val file = File("config/$filename")
                if (file.exists()) {
                    file.inputStream().use { props.load(it) }
                }
            }
        }
    }
}

/**
 * Konfiguration für Anwendungsinformationen.
 */
data class AppInfoConfig(
    val name: String,
    val version: String,
    val description: String
) {
    companion object {
        fun fromProperties(props: Properties): AppInfoConfig {
            return AppInfoConfig(
                name = props.getProperty("app.name", "Meldestelle"),
                version = props.getProperty("app.version", "1.0.0"),
                description = props.getProperty("app.description", "Pferdesport Meldestelle System")
            )
        }
    }
}

/**
 * Konfiguration für den Server.
 */
data class ServerConfig(
    val port: Int,
    val host: String,
    val workers: Int,
    val cors: CorsConfig
) {
    companion object {
        fun fromProperties(props: Properties): ServerConfig {
            val corsConfig = CorsConfig(
                enabled = props.getProperty("server.cors.enabled")?.toBoolean() ?: true,
                allowedOrigins = props.getProperty("server.cors.allowedOrigins")?.split(",")?.map { it.trim() }
                    ?: listOf("*")
            )
            return ServerConfig(
                port = System.getenv("API_PORT")?.toIntOrNull() ?: props.getProperty("server.port", "8081").toInt(),
                host = System.getenv("API_HOST") ?: props.getProperty("server.host", "0.0.0.0"),
                workers = props.getProperty("server.workers")?.toIntOrNull() ?: Runtime.getRuntime()
                    .availableProcessors(),
                cors = corsConfig
            )
        }
    }

    data class CorsConfig(
        val enabled: Boolean,
        val allowedOrigins: List<String>
    )
}

/**
 * Konfiguration für die Sicherheit.
 */
data class SecurityConfig(
    val jwt: JwtConfig,
    val apiKey: String?
) {
    companion object {
        fun fromProperties(props: Properties): SecurityConfig {
            val jwtConfig = JwtConfig(
                secret = System.getenv("JWT_SECRET") ?: props.getProperty(
                    "security.jwt.secret",
                    "default-jwt-secret-key-please-change-in-production"
                ),
                issuer = System.getenv("JWT_ISSUER") ?: props.getProperty("security.jwt.issuer", "meldestelle-api"),
                audience = System.getenv("JWT_AUDIENCE") ?: props.getProperty(
                    "security.jwt.audience",
                    "meldestelle-clients"
                ),
                realm = System.getenv("JWT_REALM") ?: props.getProperty("security.jwt.realm", "meldestelle"),
                expirationInMinutes = props.getProperty("security.jwt.expirationInMinutes")?.toLongOrNull() ?: (60 * 24)
            )
            return SecurityConfig(
                jwt = jwtConfig,
                apiKey = System.getenv("API_KEY") ?: props.getProperty("security.apiKey")
            )
        }
    }

    data class JwtConfig(
        val secret: String,
        val issuer: String,
        val audience: String,
        val realm: String,
        val expirationInMinutes: Long
    )
}

/**
 * Konfiguration für das Logging.
 */
data class LoggingConfig(
    val level: String,
    val logRequests: Boolean,
    val logResponses: Boolean
    // ... many more detailed properties from your original file
) {
    companion object {
        fun fromProperties(props: Properties, env: AppEnvironment): LoggingConfig {
            return LoggingConfig(
                level = props.getProperty("logging.level", if (env == AppEnvironment.PRODUCTION) "INFO" else "DEBUG"),
                logRequests = props.getProperty("logging.requests")?.toBoolean() ?: true,
                logResponses = props.getProperty("logging.responses")?.toBoolean() ?: (env != AppEnvironment.PRODUCTION)
                // ... load other properties here
            )
        }
    }
}

/**
 * Konfiguration für Rate Limiting.
 */
data class RateLimitConfig(
    val enabled: Boolean,
    val globalLimit: Int,
    val globalPeriodMinutes: Int
) {
    companion object {
        fun fromProperties(props: Properties): RateLimitConfig {
            return RateLimitConfig(
                enabled = props.getProperty("ratelimit.enabled")?.toBoolean() ?: true,
                globalLimit = props.getProperty("ratelimit.global.limit")?.toIntOrNull() ?: 100,
                globalPeriodMinutes = props.getProperty("ratelimit.global.periodMinutes")?.toIntOrNull() ?: 1
            )
        }
    }
}

/**
 * Konfiguration für Service Discovery.
 */
data class ServiceDiscoveryConfig(
    val enabled: Boolean,
    val consulHost: String,
    val consulPort: Int
) {
    companion object {
        fun fromProperties(props: Properties): ServiceDiscoveryConfig {
            return ServiceDiscoveryConfig(
                enabled = props.getProperty("service-discovery.enabled")?.toBoolean() ?: true,
                consulHost = System.getenv("CONSUL_HOST") ?: props.getProperty(
                    "service-discovery.consul.host",
                    "consul"
                ),
                consulPort = System.getenv("CONSUL_PORT")?.toIntOrNull()
                    ?: props.getProperty("service-discovery.consul.port", "8500").toInt()
            )
        }
    }
}

