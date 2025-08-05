package at.mocode.core.utils.database

import at.mocode.core.utils.config.DatabaseConfig
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// 1. Aktiviert die Testcontainers-Unterstützung für diese Klasse
@Testcontainers
class DatabaseFactoryTest {

    // 2. Definiert einen PostgreSQL-Container, der vor den Tests gestartet wird
    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("test-db")
            withUsername("test-user")
            withPassword("test-password")
        }
    }

    private lateinit var databaseFactory: DatabaseFactory
    private lateinit var dbConfig: DatabaseConfig

    // 3. Diese Methode wird VOR jedem Test ausgeführt
    @BeforeEach
    fun setup() {
        // Erstelle eine DB-Konfiguration mit den dynamischen Daten des gestarteten Containers
        dbConfig = DatabaseConfig(
            host = postgresContainer.host,
            port = postgresContainer.firstMappedPort,
            name = postgresContainer.databaseName,
            jdbcUrl = postgresContainer.jdbcUrl,
            username = postgresContainer.username,
            password = postgresContainer.password,
            driverClassName = "org.postgresql.Driver",
            maxPoolSize = 2,
            minPoolSize = 1,
            autoMigrate = false // Wir steuern Migrationen im Test manuell
        )
        // Erstelle eine neue Factory-Instanz und verbinde sie mit der Test-DB
        databaseFactory = DatabaseFactory(dbConfig)
        databaseFactory.connect()
    }

    // 4. Diese Methode wird NACH jedem Test ausgeführt
    @AfterEach
    fun tearDown() {
        databaseFactory.close()
    }

    // Ein einfaches Test-Tabellen-Objekt für Exposed
    private object Users : Table("test_users") {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 50)
        override val primaryKey = PrimaryKey(id)
    }

    @Test
    fun `dbQuery should connect and execute a transaction against a real PostgreSQL container`() {
        // Act & Assert
        // runBlocking wird verwendet, da dbQuery eine suspend-Funktion ist
        runBlocking {
            val resultName = databaseFactory.dbQuery {
                // Führe Operationen in einer Transaktion aus
                SchemaUtils.create(Users)
                Users.insert {
                    it[name] = "Stefan"
                }
                // Lese den gerade eingefügten Wert
                Users.selectAll().first()[Users.name]
            }

            // Überprüfe das Ergebnis
            assertNotNull(resultName)
            assertEquals("Stefan", resultName)
        }
    }
}
