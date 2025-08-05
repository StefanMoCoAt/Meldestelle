package at.mocode.core.utils.config

import java.io.File
import java.net.InetAddress
import java.util.Properties

/**
 * Verantwortlich für das Laden der Anwendungskonfiguration aus verschiedenen Quellen.
 * Diese Klasse kapselt die "unreine" Logik des Datei- und Systemzugriffs.
 */
class ConfigLoader(private val configPath: String = "config") {

    fun load(environment: AppEnvironment = AppEnvironment.current()): AppConfig {
        //val environment = AppEnvironment.current()
        val props = loadProperties(environment)

        return AppConfig(
            environment = environment,
            appInfo = createAppInfoConfig(props),
            server = createServerConfig(props),
            database = createDatabaseConfig(props),
            serviceDiscovery = createServiceDiscoveryConfig(props),
            security = createSecurityConfig(props),
            logging = createLoggingConfig(props, environment),
            rateLimit = createRateLimitConfig(props)
        )
    }

    private fun loadProperties(environment: AppEnvironment): Properties {
        val props = Properties()
        // Lade zuerst die Basis-Properties
        loadPropertiesFile("application.properties", props)
        // Überschreibe mit umgebungsspezifischen Properties, falls vorhanden
        val envFile = "application-${environment.name.lowercase()}.properties"
        loadPropertiesFile(envFile, props)
        return props
    }

    private fun loadPropertiesFile(filename: String, props: Properties) {
        // Versuche, aus den Ressourcen (im JAR) zu laden
        val resourceStream = this::class.java.classLoader.getResourceAsStream(filename)
        if (resourceStream != null) {
            resourceStream.use { props.load(it) }
            return
        }
        // Fallback für lokale Entwicklung: Lade aus einem 'config'-Ordner
        // HIER WIRD DER PARAMETER VERWENDET
        val file = File("$configPath/$filename")
        if (file.exists()) {
            file.inputStream().use { props.load(it) }
        }
    }

    // Die Konfigurations-Erstellungslogik ist hierher verschoben
    private fun createAppInfoConfig(props: Properties) = AppInfoConfig(
        name = props.getProperty("app.name", "Meldestelle"),
        version = props.getProperty("app.version", "1.0.0"),
        description = props.getProperty("app.description", "Pferdesport Meldestelle System")
    )

    private fun createServerConfig(props: Properties): ServerConfig {
        val defaultHost = try {
            InetAddress.getLocalHost().hostAddress
        } catch (_: Exception) {
            "127.0.0.1"
        }
        return ServerConfig(
            port = props.getIntProperty("server.port", "API_PORT", 8081),
            host = props.getStringProperty("server.host", "API_HOST", "0.0.0.0"),
            advertisedHost = props.getStringProperty("server.advertisedHost", "API_HOST_ADVERTISED", defaultHost),
            workers = props.getIntProperty("server.workers", "API_WORKERS", Runtime.getRuntime().availableProcessors()),
            cors = ServerConfig.CorsConfig(
                enabled = props.getBooleanProperty("server.cors.enabled", "API_CORS_ENABLED", true),
                allowedOrigins = props.getProperty("server.cors.allowedOrigins")?.split(",")?.map { it.trim() }
                    ?: listOf("*")
            )
        )
    }

    private fun createDatabaseConfig(props: Properties): DatabaseConfig {
        val host = props.getStringProperty("database.host", "DB_HOST", "localhost")
        val port = props.getIntProperty("database.port", "DB_PORT", 5432)
        val name = props.getStringProperty("database.name", "DB_NAME", "meldestelle_db")
        return DatabaseConfig(
            host = host,
            port = port,
            name = name,
            jdbcUrl = "jdbc:postgresql://$host:$port/$name",
            username = props.getStringProperty("database.username", "DB_USER", "meldestelle_user"),
            password = props.getStringProperty("database.password", "DB_PASSWORD", "secure_password_change_me"),
            driverClassName = "org.postgresql.Driver",
            maxPoolSize = props.getIntProperty("database.maxPoolSize", "DB_MAX_POOL_SIZE", 10),
            minPoolSize = props.getIntProperty("database.minPoolSize", "DB_MIN_POOL_SIZE", 5),
            autoMigrate = props.getBooleanProperty("database.autoMigrate", "DB_AUTO_MIGRATE", true)
        )
    }

    // ... Fügen Sie hier die verbleibenden 'create...Config' Methoden ein,
    // analog zu den 'fromProperties' Methoden aus der alten AppConfig.
    private fun createServiceDiscoveryConfig(props: Properties) = ServiceDiscoveryConfig(
        enabled = props.getBooleanProperty("service-discovery.enabled", "CONSUL_ENABLED", true),
        consulHost = props.getStringProperty("service-discovery.consul.host", "CONSUL_HOST", "consul"),
        consulPort = props.getIntProperty("service-discovery.consul.port", "CONSUL_PORT", 8500)
    )

    private fun createSecurityConfig(props: Properties) = SecurityConfig(
        jwt = SecurityConfig.JwtConfig(
            secret = props.getStringProperty(
                "security.jwt.secret",
                "JWT_SECRET",
                "default-secret-please-change-in-production"
            ),
            issuer = props.getStringProperty("security.jwt.issuer", "JWT_ISSUER", "meldestelle-api"),
            audience = props.getStringProperty("security.jwt.audience", "JWT_AUDIENCE", "meldestelle-clients"),
            realm = props.getStringProperty("security.jwt.realm", "JWT_REALM", "meldestelle"),
            expirationInMinutes = props.getLongProperty(
                "security.jwt.expirationInMinutes",
                "JWT_EXPIRATION_MINUTES",
                60 * 24
            )
        ),
        apiKey = props.getStringProperty("security.apiKey", "API_KEY", "").ifEmpty { null }
    )

    private fun createLoggingConfig(props: Properties, env: AppEnvironment) = LoggingConfig(
        level = props.getStringProperty("logging.level", "LOG_LEVEL", if (env.isProduction()) "INFO" else "DEBUG"),
        logRequests = props.getBooleanProperty("logging.requests", "LOG_REQUESTS", true),
        logResponses = props.getBooleanProperty("logging.responses", "LOG_RESPONSES", !env.isProduction())
    )

    private fun createRateLimitConfig(props: Properties) = RateLimitConfig(
        enabled = props.getBooleanProperty("ratelimit.enabled", "RATE_LIMIT_ENABLED", true),
        globalLimit = props.getIntProperty("ratelimit.global.limit", "RATE_LIMIT_GLOBAL_LIMIT", 100),
        globalPeriodMinutes = props.getIntProperty("ratelimit.global.periodMinutes", "RATE_LIMIT_GLOBAL_PERIOD", 1)
    )
}
