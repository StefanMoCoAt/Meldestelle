package at.mocode.config

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

/**
 * Configuration for database connection.
 * Loads configuration from application.yaml.
 */
object DatabaseConfig {
    private var initialized = false

    /**
     * Initializes the database connection using configuration from application.yaml.
     * @param application The Ktor application instance
     */
    fun init(application: Application) {
        if (initialized) return

        val config = application.environment.config

        val driver = config.property("database.driver").getString()
        val url = config.property("database.url").getString()

        // Configure connection pool
        val hikariConfig = HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = url
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        application.log.info("Database initialized with driver: $driver, url: $url")
        initialized = true
    }
}
