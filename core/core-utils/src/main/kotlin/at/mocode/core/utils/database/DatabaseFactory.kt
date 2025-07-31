package at.mocode.core.utils.database

import at.mocode.core.utils.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

class DatabaseFactory(private val config: DatabaseConfig) {

    private companion object {
        private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)
    }

    private var dataSource: HikariDataSource? = null
    private var database: Database? = null

    fun connect() {
        if (dataSource != null) {
            logger.warn("Database already connected. Closing existing connection before creating a new one.")
            close()
        }
        logger.info("Initializing database connection to ${config.jdbcUrl}")
        val hikariConfig = createHikariConfig()
        val ds = HikariDataSource(hikariConfig)
        dataSource = ds
        database = Database.connect(ds)

        if (config.autoMigrate) {
            runFlyway(ds)
        }
    }

    fun close() {
        dataSource?.close()
        dataSource = null
        database = null
        logger.info("Database connection closed.")
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T {
        val db = database ?: throw IllegalStateException("Database has not been connected. Call connect() first.")
        return newSuspendedTransaction(Dispatchers.IO, db = db) {
            block()
        }
    }

    private fun createHikariConfig(): HikariConfig {
        return HikariConfig().apply {
            driverClassName = config.driverClassName
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = config.maxPoolSize
            minimumIdle = config.minPoolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            validationTimeout = 5000
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            leakDetectionThreshold = 60000
            poolName = "MeldestelleDbPool"
        }
    }

    private fun runFlyway(dataSource: HikariDataSource) {
        logger.info("Starting Flyway migrations...")
        try {
            val count = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate()
                .migrationsExecuted
            logger.info("Flyway migrations completed successfully. Applied $count migrations.")
        } catch (e: Exception) {
            logger.error("Flyway migration failed!", e)
            throw IllegalStateException("Flyway migration failed", e)
        }
    }
}
