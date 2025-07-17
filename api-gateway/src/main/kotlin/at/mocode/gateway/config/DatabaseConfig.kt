package at.mocode.gateway.config

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Database configuration for the API Gateway.
 *
 * Sets up database connections and schema initialization for all bounded contexts.
 */
fun Application.configureDatabase() {
    val databaseUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:postgresql://localhost:5432/meldestelle"
    val databaseUser = environment.config.propertyOrNull("database.user")?.getString()
        ?: "meldestelle_user"
    val databasePassword = environment.config.propertyOrNull("database.password")?.getString()
        ?: "meldestelle_password"

    // Initialize database connection
    Database.connect(
        url = databaseUrl,
        driver = "org.postgresql.Driver",
        user = databaseUser,
        password = databasePassword
    )

    // Initialize database schemas for all contexts
    transaction {
        // Import table definitions from all contexts
        try {
            // Master Data Context tables
            SchemaUtils.createMissingTablesAndColumns(
                at.mocode.masterdata.infrastructure.repository.LandTable
            )

            // Member Management Context tables
            SchemaUtils.createMissingTablesAndColumns(
                at.mocode.members.infrastructure.repository.PersonTable,
                at.mocode.members.infrastructure.repository.VereinTable
            )

            // Horse Registry Context tables
            SchemaUtils.createMissingTablesAndColumns(
                at.mocode.horses.infrastructure.repository.HorseTable
            )

            log.info("Database schemas initialized successfully")
        } catch (e: Exception) {
            log.error("Failed to initialize database schemas: ${e.message}")
            // In production, you might want to fail fast here
        }
    }
}
