package at.mocode.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import at.mocode.tables.TurniereTable
import at.mocode.tables.BewerbeTable
import at.mocode.tables.NennungenTable
import at.mocode.tables.NennungEventsTable
import java.io.File

/**
 * Configures the database connection based on the environment.
 * Supports three environments:
 * - Test: Uses in-memory SQLite
 * - Development (IDEA): Uses file-based SQLite
 * - Production/Docker: Uses PostgreSQL with connection pooling
 */
fun configureDatabase() {
    val log = LoggerFactory.getLogger("DatabaseInitialization")
    var connectionSuccessful = false // Flag: Was any connection established?

    // Check if we're in a test environment (e.g. via System Property)
    val isTestEnvironment = System.getProperty("isTestEnvironment")?.toBoolean() ?: false

    if (isTestEnvironment) {
        log.info("Test environment detected, using SQLite in-memory database (test)...")
        try {
            Database.connect("jdbc:sqlite::memory:", driver = "org.sqlite.JDBC")
            log.info("Connected to SQLite in-memory (test) successfully.")
            connectionSuccessful = true
        } catch (e: Exception) {
            log.error("Failed to connect to SQLite (test)!", e)
            throw e // Rethrow error, test should fail
        }
    } else {
        // Check if we should use SQLite (either in IDEA or in Docker with SQLite)
        // First check for an explicit SQLite flag
        val useSqlite = System.getenv("USE_SQLITE")?.toBoolean() ?: false

        // Then check if we're in IDEA (no Docker environment variables set)
        val dbHostFromEnv = System.getenv("DB_HOST")
        val isIdeaEnvironment = (dbHostFromEnv == null)

        if (useSqlite || isIdeaEnvironment) {
            // Ensure the data directory exists
            val dataDir = File("data")
            if (!dataDir.exists()) {
                dataDir.mkdir()
            }

            log.info("IDEA environment detected (missing DB_HOST), using SQLite file database (dev)...")
            try {
                Database.connect("jdbc:sqlite:data/meldestelle.db", driver = "org.sqlite.JDBC")
                log.info("Connected to SQLite file database (dev) successfully.")
                connectionSuccessful = true
            } catch (e: Exception) {
                log.error("Failed to connect to SQLite (dev)!", e)
                // Maybe don't throw here so the app starts in IDE anyway? Currently, it throws.
                throw e
            }
        } else {
            // Normal Docker/Production environment -> use PostgreSQL
            log.info("Production/Docker environment detected, connecting to PostgreSQL...")
            try {
                // Read configuration directly from environment variables
                val dbHost = dbHostFromEnv // Safe fallback
                val dbPort = System.getenv("DB_PORT") ?: "5432"
                val dbName = System.getenv("DB_NAME") ?: error("DB_NAME not set in environment")
                val dbUser = System.getenv("DB_USER") ?: error("DB_USER not set in environment")
                val dbPassword = System.getenv("DB_PASSWORD") ?: error("DB_PASSWORD not set in environment")
                val driverClassName = "org.postgresql.Driver"
                val maxPoolSize = System.getenv("DB_POOL_SIZE")?.toIntOrNull() ?: 10
                val jdbcURL = "jdbc:postgresql://$dbHost:$dbPort/$dbName"

                log.info("Attempting to connect to PostgresQL at URL: {}", jdbcURL)

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
                log.info("PostgresQL connection pool initialized successfully!")
                connectionSuccessful = true
            } catch (e: Exception) {
                log.error("Failed to initialize PostgreSQL connection pool!", e)
                throw e // Rethrow error, app should not start without DB in prod
            }
        }
    }

    // --- Schema Initialization (NOW CENTRALIZED) ---
    // Only execute this if *any* DB connection was successful
    transaction { // Execute schema operations in a transaction
        log.info("Initializing/Verifying database schema...")
        try {
            // Create the table(s) if they don't exist yet
            SchemaUtils.create(TurniereTable, BewerbeTable, NennungenTable, NennungEventsTable)

            log.info("Database schema initialized successfully (tables created/verified).")
        } catch (e: Exception) {
            log.error("Failed to initialize database schema!", e)
            // Here you could decide if a schema error is critical
            // throw e // Commented out: App might start anyway, even if the schema is missing/wrong
        }
    }
}
