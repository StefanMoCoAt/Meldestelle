package at.mocode.infrastructure.gateway.config

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Database configuration for the API Gateway.
 *
 * Sets up database connections and schema initialization for all bounded contexts.
 */
fun Application.configureDatabase() {
    val log = LoggerFactory.getLogger("DatabaseConfig")
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
                at.mocode.masterdata.infrastructure.persistence.LandTable
            )

            // Member Management Context tables
            // TODO: Uncomment once the members module is fully migrated
            // SchemaUtils.createMissingTablesAndColumns(
            //     at.mocode.members.infrastructure.persistence.PersonTable,
            //     at.mocode.members.infrastructure.persistence.VereinTable
            // )

            // Horse Registry Context tables
            SchemaUtils.createMissingTablesAndColumns(
                at.mocode.horses.infrastructure.persistence.HorseTable
            )

            // Event Management Context tables
            SchemaUtils.createMissingTablesAndColumns(
                at.mocode.events.infrastructure.persistence.VeranstaltungTable
            )

            log.info("Database schemas initialized successfully")
        } catch (e: Exception) {
            log.error("Failed to initialize database schemas: ${e.message}")
            // In production, you might want to fail fast here
        }
    }
}
