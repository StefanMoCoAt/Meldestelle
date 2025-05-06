package at.mocode.plugins

import at.mocode.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun configureDatabase() {
    val log = LoggerFactory.getLogger("DatabaseInitialization")
    var connectionSuccessful = false // Flag: Wurde irgendeine Verbindung hergestellt?

    // Prüfen, ob wir in einer Testumgebung sind (z.B. über System Property)
    val isTestEnvironment = System.getProperty("isTestEnvironment")?.toBoolean() ?: false

    if (isTestEnvironment) {
        log.info("Test environment detected, using in-memory H2 database (test)...")
        try {
            // H2 im PostgreSQL-Kompatibilitätsmodus starten, kann helfen
            Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", driver = "org.h2.Driver")
            log.info("Connected to H2 (test) successfully.")
            connectionSuccessful = true
        } catch (e: Exception) {
            log.error("Failed to connect to H2 (test)!", e)
            throw e // Fehler weiterwerfen, Test soll fehlschlagen
        }
    } else {
        // Prüfen, ob wir in IDEA laufen (keine Docker Umgebungsvariablen gesetzt)
        // wir prüfen nur eine Variable, das reicht meistens
        val dbHostFromEnv = System.getenv("DB_HOST")
        val isIdeaEnvironment = (dbHostFromEnv == null)

        if (isIdeaEnvironment) {
            log.info("IDEA environment detected (missing DB_HOST), using in-memory H2 database (dev)...")
            try {
                Database.connect("jdbc:h2:mem:dev;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", driver = "org.h2.Driver")
                log.info("Connected to H2 (dev) successfully.")
                connectionSuccessful = true
            } catch (e: Exception) {
                log.error("Failed to connect to H2 (dev)!", e)
                // Hier vielleicht nicht werfen, damit App in IDE trotzdem startet? Oder doch? → Aktuell wirft es.
                throw e
            }
        } else {
            // Normale Docker/Produktionsumgebung -> PostgreSQL verwenden
            log.info("Production/Docker environment detected, connecting to PostgreSQL...")
            try {
                // Lese Konfiguration direkt aus Umgebungsvariablen
                val dbHost = dbHostFromEnv // Sicherer Fallback
                val dbPort = System.getenv("DB_PORT") ?: "5432"
                val dbName = System.getenv("DB_NAME") ?: error("DB_NAME not set in environment")
                val dbUser = System.getenv("DB_USER") ?: error("DB_USER not set in environment")
                val dbPassword = System.getenv("DB_PASSWORD") ?: error("DB_PASSWORD not set in environment")
                val driverClassName = "org.postgresql.Driver"
                val maxPoolSize = System.getenv("DB_POOL_SIZE")?.toIntOrNull() ?: 10
                val jdbcURL = "jdbc:postgresql://$dbHost:$dbPort/$dbName"

                log.info("Attempting to connect to PostgreSQL at URL: {}", jdbcURL)

                val hikariConfig = HikariConfig().apply {
                    this.driverClassName = driverClassName
                    this.jdbcUrl = jdbcURL
                    this.username = dbUser
                    this.password = dbPassword
                    this.maximumPoolSize = maxPoolSize
                    this.validate()
                }
                val dataSource = HikariDataSource(hikariConfig)
                Database.connect(dataSource)
                log.info("PostgreSQL connection pool initialized successfully!")
                connectionSuccessful = true
            } catch (e: Exception) {
                log.error("Failed to initialize PostgreSQL connection pool!", e)
                throw e // Fehler weiterwerfen, App soll nicht starten ohne DB in Prod
            }
        }
    }

    // --- Schema Initialisierung (JETZT ZENTRALISIERT) ---
    // Führe dies nur aus, wenn *irgendeine* DB-Verbindung erfolgreich war
    transaction { // Führe Schema-Operationen in einer Transaktion aus
        log.info("Initializing/Verifying database schema...")
        try {
            // Erstellt die Tabelle(n), falls sie noch nicht existieren
            SchemaUtils.create(TurniereTable)
            // Füge hier später weitere Tabellen hinzu:
            // SchemaUtils.create(TurniereTable, NennungenTable, ...)

            log.info("Database schema initialized successfully (tables created/verified).")
        } catch (e: Exception) {
            log.error("Failed to initialize database schema!", e)
            // Hier könntest du entscheiden, ob ein Fehler beim Schema kritisch ist
            // throw e // Auskommentiert: App startet evtl. trotzdem, auch wenn Schema fehlt/falsch ist
        }
    }

    // --- TODO für den NÄCHSTEN Schritt ---
    // Hier kommt später die Logik zum Erstellen der Tabellen hin,
    // z.B. innerhalb einer Transaktion:
    transaction {
        SchemaUtils.create(
            VereineTable,
            PersonenTable,
            PferdeTable,
            VeranstaltungenTable, // NEU
            TurniereTable,
            ArtikelTable,
            PlaetzeTable // NEU
            // ... weitere Tabellen ...
        )
    }
    // ------------------------------------
}
