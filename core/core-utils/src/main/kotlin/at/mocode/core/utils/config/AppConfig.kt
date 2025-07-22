package at.mocode.core.utils.config

import at.mocode.core.utils.database.DatabaseConfig
import java.io.File
import java.util.Properties

/**
 * Zentrale Konfigurationsverwaltung für die Anwendung.
 * Lädt Konfigurationen aus verschiedenen Quellen (Umgebungsvariablen, Property-Dateien).
 */
object AppConfig {
    // Aktuelle Umgebung
    val environment: AppEnvironment = AppEnvironment.current()

    // Anwendungs-Informationen
    val appInfo = AppInfoConfig()

    // Server-Konfiguration
    val server = ServerConfig()

    // Sicherheits-Konfiguration
    val security = SecurityConfig()

    // Logging-Konfiguration
    val logging = LoggingConfig()

    // Rate Limiting-Konfiguration
    val rateLimit = RateLimitConfig()

    // Service Discovery-Konfiguration
    val serviceDiscovery = ServiceDiscoveryConfig()

    // Datenbank-Konfiguration (wird nach dem Laden der Properties initialisiert)
    val database: DatabaseConfig

    init {
        // Lade Umgebungsspezifische Properties
        val props = loadProperties()

        // Konfiguriere Komponenten mit Properties
        appInfo.configure(props)
        server.configure(props)
        security.configure(props)
        logging.configure(props)
        rateLimit.configure(props)
        serviceDiscovery.configure(props)

        // Datenbank-Konfiguration mit Properties initialisieren
        database = DatabaseConfig.fromEnv(props)

        // Log Konfigurationsinformationen
        if (!AppEnvironment.isProduction()) {
            println("=== Anwendungskonfiguration ===")
            println("Umgebung: $environment")
            println("App: ${appInfo.name} v${appInfo.version}")
            println("Server: Port ${server.port}, ${server.workers} Worker")
            println("Datenbank: ${database.jdbcUrl}")
            println("===============================\n")
        }
    }

    /**
     * Lädt die Properties für die aktuelle Umgebung.
     */
    private fun loadProperties(): Properties {
        val props = Properties()

        // Lade Basis-Properties
        loadPropertiesFile("application.properties", props)

        // Lade umgebungsspezifische Properties
        val envFile = when (environment) {
            AppEnvironment.DEVELOPMENT -> "application-dev.properties"
            AppEnvironment.TEST -> "application-test.properties"
            AppEnvironment.STAGING -> "application-staging.properties"
            AppEnvironment.PRODUCTION -> "application-prod.properties"
        }

        loadPropertiesFile(envFile, props)

        return props
    }

    /**
     * Lädt eine Property-Datei, wenn sie existiert.
     */
    private fun loadPropertiesFile(filename: String, props: Properties) {
        val resourceStream = javaClass.classLoader.getResourceAsStream(filename)
        if (resourceStream != null) {
            props.load(resourceStream)
            resourceStream.close()
        } else {
            // Versuche aus dem Dateisystem zu laden
            val file = File("config/$filename")
            if (file.exists()) {
                file.inputStream().use { props.load(it) }
            }
        }
    }

    /**
     * Gibt den Wert einer Property zurück, wobei die Priorität wie folgt ist:
     * 1. Umgebungsvariable
     * 2. Property aus Datei
     * 3. Standardwert
     */
    fun getProperty(key: String, defaultValue: String? = null): String? {
        val envKey = key.replace('.', '_').uppercase()
        return System.getenv(envKey) ?: defaultValue
    }
}

/**
 * Konfiguration für Anwendungsinformationen.
 */
class AppInfoConfig {
    var name: String = "Meldestelle"
    var version: String = "1.0.0"
    var description: String = "Pferdesport Meldestelle System"

    fun configure(props: Properties) {
        name = props.getProperty("app.name", name)
        version = props.getProperty("app.version", version)
        description = props.getProperty("app.description", description)
    }
}

/**
 * Konfiguration für den Server.
 */
class ServerConfig {
    var port: Int = System.getenv("API_PORT")?.toIntOrNull() ?: 8081
    var host: String = System.getenv("API_HOST") ?: "0.0.0.0"
    var workers: Int = Runtime.getRuntime().availableProcessors()
    var cors: CorsConfig = CorsConfig()

    fun configure(props: Properties) {
        port = props.getProperty("server.port")?.toIntOrNull() ?: port
        host = props.getProperty("server.host") ?: host
        workers = props.getProperty("server.workers")?.toIntOrNull() ?: workers

        // CORS Konfiguration
        cors.enabled = props.getProperty("server.cors.enabled")?.toBoolean() ?: cors.enabled
        props.getProperty("server.cors.allowedOrigins")?.split(",")?.map { it.trim() }?.let {
            cors.allowedOrigins = it
        }
    }

    class CorsConfig {
        var enabled: Boolean = true
        var allowedOrigins: List<String> = listOf("*")
    }
}

/**
 * Konfiguration für die Sicherheit.
 */
class SecurityConfig {
    var jwt = JwtConfig()
    var apiKey: String? = null

    fun configure(props: Properties) {
        // JWT Konfiguration
        jwt.secret = System.getenv("JWT_SECRET") ?: props.getProperty("security.jwt.secret") ?: jwt.secret
        jwt.issuer = System.getenv("JWT_ISSUER") ?: props.getProperty("security.jwt.issuer") ?: jwt.issuer
        jwt.audience = System.getenv("JWT_AUDIENCE") ?: props.getProperty("security.jwt.audience") ?: jwt.audience
        jwt.realm = System.getenv("JWT_REALM") ?: props.getProperty("security.jwt.realm") ?: jwt.realm

        props.getProperty("security.jwt.expirationInMinutes")?.toLongOrNull()?.let {
            jwt.expirationInMinutes = it
        }

        // API Key Konfiguration
        apiKey = System.getenv("API_KEY") ?: props.getProperty("security.apiKey")
    }

    class JwtConfig {
        var secret: String = "default-jwt-secret-key-please-change-in-production"
        var issuer: String = "meldestelle-api"
        var audience: String = "meldestelle-clients"
        var realm: String = "meldestelle"
        var expirationInMinutes: Long = 60 * 24 // 24 Stunden
    }
}

/**
 * Konfiguration für das Logging.
 */
class LoggingConfig {
    // Allgemeine Logging-Einstellungen
    var level: String = if (AppEnvironment.isProduction()) "INFO" else "DEBUG"
    var logRequests: Boolean = true
    var logResponses: Boolean = !AppEnvironment.isProduction()

    // Erweiterte Request-Logging-Einstellungen
    var logRequestHeaders: Boolean = !AppEnvironment.isProduction()
    var logRequestBody: Boolean = !AppEnvironment.isProduction()
    var logRequestParameters: Boolean = true

    // Erweiterte Response-Logging-Einstellungen
    var logResponseHeaders: Boolean = !AppEnvironment.isProduction()
    var logResponseBody: Boolean = !AppEnvironment.isProduction()
    var logResponseTime: Boolean = true

    // Filter für Logging
    var excludePaths: List<String> = listOf("/health", "/metrics", "/favicon.ico")
    var maxBodyLogSize: Int = 1000  // Maximale Größe des Body-Logs in Zeichen

    // Strukturiertes Logging
    var useStructuredLogging: Boolean = true
    var includeCorrelationId: Boolean = true

    // Log Sampling für hohe Traffic-Volumen
    var enableLogSampling: Boolean = AppEnvironment.isProduction() // In Produktion standardmäßig aktiviert
    var samplingRate: Int = 10 // Nur 10% der Anfragen in High-Traffic-Endpunkten loggen
    var highTrafficThreshold: Int = 100 // Schwellenwert für Anfragen pro Minute
    var alwaysLogPaths: List<String> = listOf("/api/v1/auth", "/api/v1/admin") // Diese Pfade immer vollständig loggen
    var alwaysLogErrors: Boolean = true // Fehler immer loggen, unabhängig vom Sampling

    // Cross-Service Tracing
    var requestIdHeader: String = "X-Request-ID"
    var propagateRequestId: Boolean = true
    var generateRequestIdIfMissing: Boolean = true

    fun configure(props: Properties) {
        // Allgemeine Einstellungen
        level = props.getProperty("logging.level") ?: level
        logRequests = props.getProperty("logging.requests")?.toBoolean() ?: logRequests
        logResponses = props.getProperty("logging.responses")?.toBoolean() ?: logResponses

        // Request-Logging-Einstellungen
        logRequestHeaders = props.getProperty("logging.request.headers")?.toBoolean() ?: logRequestHeaders
        logRequestBody = props.getProperty("logging.request.body")?.toBoolean() ?: logRequestBody
        logRequestParameters = props.getProperty("logging.request.parameters")?.toBoolean() ?: logRequestParameters

        // Response-Logging-Einstellungen
        logResponseHeaders = props.getProperty("logging.response.headers")?.toBoolean() ?: logResponseHeaders
        logResponseBody = props.getProperty("logging.response.body")?.toBoolean() ?: logResponseBody
        logResponseTime = props.getProperty("logging.response.time")?.toBoolean() ?: logResponseTime

        // Filter-Einstellungen
        props.getProperty("logging.exclude.paths")?.split(",")?.map { it.trim() }?.let {
            excludePaths = it
        }
        maxBodyLogSize = props.getProperty("logging.maxBodyLogSize")?.toIntOrNull() ?: maxBodyLogSize

        // Strukturiertes Logging
        useStructuredLogging = props.getProperty("logging.structured")?.toBoolean() ?: useStructuredLogging
        includeCorrelationId = props.getProperty("logging.correlationId")?.toBoolean() ?: includeCorrelationId

        // Log Sampling Konfiguration
        enableLogSampling = props.getProperty("logging.sampling.enabled")?.toBoolean() ?: enableLogSampling
        samplingRate = props.getProperty("logging.sampling.rate")?.toIntOrNull() ?: samplingRate
        highTrafficThreshold = props.getProperty("logging.sampling.highTrafficThreshold")?.toIntOrNull() ?: highTrafficThreshold
        alwaysLogErrors = props.getProperty("logging.sampling.alwaysLogErrors")?.toBoolean() ?: alwaysLogErrors

        // Pfade, die immer geloggt werden sollen
        props.getProperty("logging.sampling.alwaysLogPaths")?.split(",")?.map { it.trim() }?.let {
            alwaysLogPaths = it
        }

        // Cross-Service Tracing
        requestIdHeader = props.getProperty("logging.requestIdHeader") ?: requestIdHeader
        propagateRequestId = props.getProperty("logging.propagateRequestId")?.toBoolean() ?: propagateRequestId
        generateRequestIdIfMissing = props.getProperty("logging.generateRequestIdIfMissing")?.toBoolean() ?: generateRequestIdIfMissing
    }
}

/**
 * Konfiguration für Rate Limiting.
 */
class RateLimitConfig {
    // Globale Rate Limiting Konfiguration
    var enabled: Boolean = true
    var globalLimit: Int = 100
    var globalPeriodMinutes: Int = 1
    var includeHeaders: Boolean = true

    // Spezifische Rate Limits für verschiedene Endpunkte oder Benutzertypen
    var endpointLimits: Map<String, EndpointLimit> = mapOf(
        "api/v1/events" to EndpointLimit(200, 1),
        "api/v1/auth" to EndpointLimit(20, 1)
    )

    // Rate Limits für verschiedene Benutzertypen
    var userTypeLimits: Map<String, EndpointLimit> = mapOf(
        "anonymous" to EndpointLimit(50, 1),
        "authenticated" to EndpointLimit(200, 1),
        "admin" to EndpointLimit(500, 1)
    )

    fun configure(props: Properties) {
        enabled = props.getProperty("ratelimit.enabled")?.toBoolean() ?: enabled
        globalLimit = props.getProperty("ratelimit.global.limit")?.toIntOrNull() ?: globalLimit
        globalPeriodMinutes = props.getProperty("ratelimit.global.periodMinutes")?.toIntOrNull() ?: globalPeriodMinutes
        includeHeaders = props.getProperty("ratelimit.includeHeaders")?.toBoolean() ?: includeHeaders

        // Endpunkt-spezifische Limits können in der Konfiguration überschrieben werden
        // Format: ratelimit.endpoint.api/v1/events.limit=200
        // Format: ratelimit.endpoint.api/v1/events.periodMinutes=1
    }

    /**
     * Repräsentiert ein Rate Limit für einen spezifischen Endpunkt oder Benutzertyp.
     */
    data class EndpointLimit(
        val limit: Int,
        val periodMinutes: Int
    )
    }

    /**
     * Konfiguration für Service Discovery.
     */
    class ServiceDiscoveryConfig {
        // Consul Konfiguration
        var enabled: Boolean = true
        var consulHost: String = System.getenv("CONSUL_HOST") ?: "consul"
        var consulPort: Int = System.getenv("CONSUL_PORT")?.toIntOrNull() ?: 8500

        // Service Registration Konfiguration
        var registerServices: Boolean = true
        var healthCheckPath: String = "/health"
        var healthCheckInterval: Int = 10 // Sekunden

        fun configure(props: Properties) {
            enabled = props.getProperty("service-discovery.enabled")?.toBoolean() ?: enabled
            consulHost = props.getProperty("service-discovery.consul.host") ?: consulHost
            consulPort = props.getProperty("service-discovery.consul.port")?.toIntOrNull() ?: consulPort

            registerServices = props.getProperty("service-discovery.register-services")?.toBoolean() ?: registerServices
            healthCheckPath = props.getProperty("service-discovery.health-check.path") ?: healthCheckPath
            healthCheckInterval = props.getProperty("service-discovery.health-check.interval")?.toIntOrNull() ?: healthCheckInterval
        }
    }
