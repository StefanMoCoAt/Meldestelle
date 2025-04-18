package at.mocode.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

fun Application.configureDatabase() {
    val log = LoggerFactory.getLogger("DatabaseInitialization")
    log.info("Initializing database connection from environment variables...")

    // Lese Konfiguration direkt aus Umgebungsvariablen,
    // die von Docker Compose (aus .env) gesetzt werden.
    val dbHost = System.getenv("DB_HOST") ?: "db" // Fallback auf 'db', falls nicht gesetzt
    val dbPort = System.getenv("DB_PORT") ?: "5432"
    val dbName = System.getenv("DB_NAME")
        ?: error("Database name (DB_NAME) not set in environment") // Fehler, wenn nicht gesetzt
    val dbUser = System.getenv("DB_USER")
        ?: error("Database user (DB_USER) not set in environment") // Fehler, wenn nicht gesetzt
    val dbPassword = System.getenv("DB_PASSWORD")
        ?: error("Database password (DB_PASSWORD) not set in environment") // Fehler, wenn nicht gesetzt
    val driverClassName = "org.postgresql.Driver" // Ist für Postgres fix
    // Pool Size auch optional aus Env Var lesen
    val maxPoolSize = System.getenv("DB_POOL_SIZE")?.toIntOrNull() ?: 10

    // Baue die JDBC URL zusammen
    val jdbcURL = "jdbc:postgresql://$dbHost:$dbPort/$dbName"

    log.info("Attempting to connect to database at URL: {}", jdbcURL) // Logge die URL (ohne User/Passwort!)

    // Konfiguriere HikariCP mit den Werten aus der Umgebung
    val hikariConfig = HikariConfig().apply {
        this.driverClassName = driverClassName
        this.jdbcUrl = jdbcURL
        this.username = dbUser
        this.password = dbPassword
        this.maximumPoolSize = maxPoolSize
        // Hier könnten weitere HikariCP-Optimierungen hin
        try {
            this.validate() // Prüft die Konfiguration frühzeitig
        } catch (e: Exception) {
            log.error("HikariCP configuration validation failed!", e)
            throw e // Wirft den Fehler weiter, damit die App nicht startet
        }
    }

    // Erstelle DataSource und verbinde Exposed
    try {
        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
        log.info("Database connection pool initialized successfully!")
    } catch (e: Exception) {
        log.error("Failed to initialize database connection pool!", e)
        // Optional: Hier entscheiden, ob die App trotzdem starten soll oder nicht.
        // Aktuell würde sie bei Fehlern hier abstürzen (was oft gewünscht ist).
        throw e
    }


    // --- TODO für den NÄCHSTEN Schritt ---
    // Hier kommt später die Logik zum Erstellen der Tabellen hin,
    // z.B. innerhalb einer Transaktion:
    // transaction {
    //     SchemaUtils.create(TurniereTable) // Erstellt die Tabelle, wenn sie nicht existiert
    // }
    // ------------------------------------
}