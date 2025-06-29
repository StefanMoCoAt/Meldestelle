package at.mocode.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * Configures the database connection based on the environment.
 * Supports three environments:
 * 1. Test environment - Uses in-memory H2 database
 * 2. Development environment - Uses in-memory H2 database
 * 3. Production environment - Uses PostgreSQL database
 *
 * @param application The Ktor application instance to read configuration from
 */
fun Application.configureDatabase() {
    val log = LoggerFactory.getLogger("DatabaseInitialization")
    var connectionSuccessful = false

    // Environment detection
    val isTestEnvironment = System.getProperty("isTestEnvironment")?.toBoolean() ?: false
    val dbHostFromEnv = System.getenv("DB_HOST")
    val isIdeaEnvironment = (dbHostFromEnv == null)

    // Get database configuration from application.yaml if available
    val dbConfig = try {
        environment.config.config("database")
    } catch (e: ApplicationConfigurationException) {
        log.warn("No database configuration found in application.yaml, using environment variables")
        null
    }

    when {
        isTestEnvironment -> {
            configureTestDatabase(log)
            connectionSuccessful = true
        }
        isIdeaEnvironment -> {
            configureDevelopmentDatabase(log)
            connectionSuccessful = true
        }
        else -> {
            connectionSuccessful = configureProductionDatabase(log, dbConfig)
        }
    }

    // Initialize schema if connection was successful
    if (connectionSuccessful) {
        initializeSchema(log, isTestEnvironment, isIdeaEnvironment)
    } else {
        log.error("No database connection established. Schema initialization skipped.")
    }
}

/**
 * Configures an in-memory H2 database for testing
 */
private fun configureTestDatabase(log: Logger): Boolean {
    log.info("Test environment detected, using in-memory H2 database (test)...")
    return try {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )
        log.info("Connected to H2 (test) successfully.")
        true
    } catch (e: Exception) {
        log.error("Failed to connect to H2 (test)!", e)
        throw e // Rethrow to fail the test
    }
}

/**
 * Configures an in-memory H2 database for development
 */
private fun configureDevelopmentDatabase(log: Logger): Boolean {
    log.info("Development environment detected, using in-memory H2 database (dev)...")
    return try {
        Database.connect(
            url = "jdbc:h2:mem:dev;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )
        log.info("Connected to H2 (dev) successfully.")
        true
    } catch (e: Exception) {
        log.error("Failed to connect to H2 (dev)!", e)
        throw e
    }
}

/**
 * Configures a PostgreSQL database for production
 */
private fun configureProductionDatabase(log: Logger, dbConfig: ApplicationConfig?): Boolean {
    log.info("Production environment detected, connecting to PostgreSQL...")

    // Get database configuration from application.yaml or environment variables
    val dbHost = dbConfig?.propertyOrNull("host")?.getString() ?: System.getenv("DB_HOST")
        ?: error("Database host not configured")
    val dbPort = dbConfig?.propertyOrNull("port")?.getString() ?: System.getenv("DB_PORT") ?: "5432"
    val dbName = dbConfig?.propertyOrNull("name")?.getString() ?: System.getenv("DB_NAME")
        ?: error("Database name not configured")
    val dbUser = dbConfig?.propertyOrNull("user")?.getString() ?: System.getenv("DB_USER")
        ?: error("Database user not configured")
    val dbPassword = dbConfig?.propertyOrNull("password")?.getString() ?: System.getenv("DB_PASSWORD")
        ?: error("Database password not configured")

    // Connection pool configuration
    val maxPoolSize = dbConfig?.propertyOrNull("pool.maxSize")?.getString()?.toIntOrNull()
        ?: System.getenv("DB_POOL_SIZE")?.toIntOrNull() ?: 10
    val minIdle = dbConfig?.propertyOrNull("pool.minIdle")?.getString()?.toIntOrNull() ?: 2
    val idleTimeout = dbConfig?.propertyOrNull("pool.idleTimeout")?.getString()?.toLongOrNull() ?: 10000L
    val connectionTimeout = dbConfig?.propertyOrNull("pool.connectionTimeout")?.getString()?.toLongOrNull() ?: 5000L
    val maxLifetime = dbConfig?.propertyOrNull("pool.maxLifetime")?.getString()?.toLongOrNull() ?: 1800000L

    val jdbcURL = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    log.info("Attempting to connect to PostgreSQL at URL: {}", jdbcURL)

    return try {
        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = jdbcURL
            username = dbUser
            password = dbPassword
            maximumPoolSize = maxPoolSize
            minimumIdle = minIdle
            this.idleTimeout = idleTimeout
            this.connectionTimeout = connectionTimeout
            this.maxLifetime = maxLifetime

            // Additional security and performance settings
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")

            // Connection validation
            connectionTestQuery = "SELECT 1"
            validationTimeout = TimeUnit.SECONDS.toMillis(5)

            // Leak detection
            leakDetectionThreshold = TimeUnit.SECONDS.toMillis(60)

            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
        log.info("PostgreSQL connection pool initialized successfully!")
        true
    } catch (e: Exception) {
        log.error("Failed to initialize PostgreSQL connection pool!", e)
        throw e // Rethrow in production
    }
}

/**
 * Initializes the database schema
 */
private fun initializeSchema(log: Logger, isTestEnvironment: Boolean, isIdeaEnvironment: Boolean) {
    transaction {
        log.info("Initializing/Verifying database schema...")
        try {
            // Create all tables if they don't exist
            SchemaUtils.create(
                _root_ide_package_.at.mocode.tables.VereineTable,
                _root_ide_package_.at.mocode.tables.PersonenTable,
                _root_ide_package_.at.mocode.tables.PferdeTable,
                _root_ide_package_.at.mocode.tables.VeranstaltungenTable,
                _root_ide_package_.at.mocode.tables.TurniereTable,
                _root_ide_package_.at.mocode.tables.ArtikelTable,
                _root_ide_package_.at.mocode.tables.PlaetzeTable,
                _root_ide_package_.at.mocode.tables.LizenzenTable
                // Add more tables here if needed
            )
            log.info("Database schema initialized successfully.")
        } catch (e: Exception) {
            log.error("Failed to initialize database schema!", e)
            // In production, a schema initialization failure is critical
            if (!isTestEnvironment && !isIdeaEnvironment) {
                throw e
            }
            // In test/development, just log the error
        }
    }
}
