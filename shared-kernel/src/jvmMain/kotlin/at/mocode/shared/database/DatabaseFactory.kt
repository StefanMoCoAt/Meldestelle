package at.mocode.shared.database

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
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
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
}
