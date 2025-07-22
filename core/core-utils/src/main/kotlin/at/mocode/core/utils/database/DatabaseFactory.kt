package at.mocode.core.utils.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Factory-Klasse für die Datenbankverbindung.
 * Stellt eine Verbindung zur Datenbank her und konfiguriert den Connection Pool.
 */
object DatabaseFactory {
    private var dataSource: HikariDataSource? = null

    /**
     * Initialisiert die Datenbankverbindung mit der angegebenen Konfiguration.
     * @param config Die Datenbankkonfiguration
     */
    fun init(config: DatabaseConfig) {
        if (dataSource != null) {
            close()
        }

        val hikariConfig = HikariConfig().apply {
            driverClassName = config.driverClassName
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            maximumPoolSize = config.maxPoolSize
            minimumIdle = config.minPoolSize // Use the minPoolSize from config
            isAutoCommit = false

            // Use READ_COMMITTED for better performance while maintaining data integrity
            // REPEATABLE_READ is more strict and can lead to more contention
            transactionIsolation = "TRANSACTION_READ_COMMITTED"

            // Connection validation
            connectionTestQuery = "SELECT 1"
            validationTimeout = 5000 // 5 seconds

            // Connection timeouts
            connectionTimeout = 30000 // 30 seconds
            idleTimeout = 600000 // 10 minutes
            maxLifetime = 1800000 // 30 minutes

            // Leak detection
            leakDetectionThreshold = 60000 // 1 minute

            // Statement cache for better performance
            dataSourceProperties["cachePrepStmts"] = "true"
            dataSourceProperties["prepStmtCacheSize"] = "250"
            dataSourceProperties["prepStmtCacheSqlLimit"] = "2048"
            dataSourceProperties["useServerPrepStmts"] = "true"

            // Connection initialization - run a simple query to warm up connections
            connectionInitSql = "SELECT 1"

            // Pool name for better identification in metrics
            poolName = "MeldestelleDbPool"

            validate()
        }

        dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource!!)
    }

    /**
     * Führt eine Datenbankoperation in einer Transaktion aus.
     * @param block Der Code, der in der Transaktion ausgeführt werden soll
     * @return Das Ergebnis der Transaktion
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * Schließt die Datenbankverbindung.
     */
    fun close() {
        dataSource?.close()
        dataSource = null
    }

    /**
     * Gets the number of active connections in the pool.
     * @return The number of active connections, or 0 if the pool is not initialized
     */
    fun getActiveConnections(): Int {
        return dataSource?.hikariPoolMXBean?.activeConnections ?: 0
    }

    /**
     * Gets the number of idle connections in the pool.
     * @return The number of idle connections, or 0 if the pool is not initialized
     */
    fun getIdleConnections(): Int {
        return dataSource?.hikariPoolMXBean?.idleConnections ?: 0
    }

    /**
     * Gets the total number of connections in the pool.
     * @return The total number of connections, or 0 if the pool is not initialized
     */
    fun getTotalConnections(): Int {
        return dataSource?.hikariPoolMXBean?.totalConnections ?: 0
    }
}
