package at.mocode.shared.config

import at.mocode.shared.database.DatabaseConfig
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
    var level: String = if (AppEnvironment.isProduction()) "INFO" else "DEBUG"
    var logRequests: Boolean = true
    var logResponses: Boolean = !AppEnvironment.isProduction()

    fun configure(props: Properties) {
        level = props.getProperty("logging.level") ?: level
        logRequests = props.getProperty("logging.requests")?.toBoolean() ?: logRequests
        logResponses = props.getProperty("logging.responses")?.toBoolean() ?: logResponses
    }
}
