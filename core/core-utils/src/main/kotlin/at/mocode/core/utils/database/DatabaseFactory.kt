package at.mocode.core.utils.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Factory-Klasse für die Datenbankverbindung.
 * Erstellt und konfiguriert eine Datenbankverbindung inklusive Connection Pool
 * und führt bei der Initialisierung die notwendigen Migrationen aus.
 *
 * @property config Die Datenbankkonfiguration, die für diese Instanz verwendet werden soll.
 */
class DatabaseFactory(private val config: DatabaseConfig) {

    private var dataSource: HikariDataSource? = null
    private var database: Database? = null

    /**
     * Initialisiert die Datenbankverbindung. Muss vor der ersten Verwendung aufgerufen werden.
     * Konfiguriert den Connection Pool und führt Flyway-Migrationen aus.
     */
    fun connect() {
        if (dataSource != null) {
            close()
        }

        val hikariConfig = createHikariConfig()
        val ds = HikariDataSource(hikariConfig)
        dataSource = ds
        database = Database.connect(ds)

        if (config.autoMigrate) {
            runFlyway(ds)
        }
    }

    /**
     * Schließt die Datenbankverbindung und den Connection Pool.
     */
    fun close() {
        dataSource?.close()
        dataSource = null
        database = null
    }

    /**
     * Führt eine Datenbankoperation in einer neuen, suspendierenden Transaktion aus.
     * Dies ist die primäre Methode, um mit der Datenbank zu interagieren.
     *
     * @param block Der Code, der in der Transaktion ausgeführt werden soll.
     * @return Das Ergebnis der Transaktion.
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T {
        // Wir stellen sicher, dass die dbQuery-Funktion nur auf einer verbundenen Datenbank läuft.
        if (database == null) {
            throw IllegalStateException("Database has not been connected. Call connect() first.")
        }
        return newSuspendedTransaction(Dispatchers.IO, db = database) {
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
            connectionTestQuery = "SELECT 1"
            validationTimeout = 5000 // 5 seconds
            connectionTimeout = 30000 // 30 seconds
            idleTimeout = 600000 // 10 minutes
            maxLifetime = 1800000 // 30 minutes
            leakDetectionThreshold = 60000 // 1 minute
            poolName = "MeldestelleDbPool-${config.jdbcUrl.substringAfterLast('/')}" // Eindeutiger Pool-Name
        }
    }

    private fun runFlyway(dataSource: HikariDataSource) {
        println("Starte Flyway-Migrationen für Schema: ${dataSource.jdbcUrl}")
        try {
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate()
            println("Flyway-Migrationen erfolgreich abgeschlossen.")
        } catch (e: Exception) {
            println("FEHLER: Flyway-Migration fehlgeschlagen! Details: ${e.message}")
            // Wir werfen den Fehler weiter, damit die Anwendung beim Start fehlschlägt.
            // Das ist wichtig, um Inkonsistenzen zu vermeiden.
            throw IllegalStateException("Flyway migration failed", e)
        }
    }
}
