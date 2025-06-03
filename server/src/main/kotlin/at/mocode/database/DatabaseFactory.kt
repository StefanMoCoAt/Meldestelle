package at.mocode.database

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Factory for database operations
 */
object DatabaseFactory {
    /**
     * Initializes the database schema
     * @param application The Ktor application instance
     */
    fun init(application: Application) {
        // Create tables if they don't exist
        transaction {
            SchemaUtils.create(Turniere, Bewerbe)
            application.log.info("Database schema initialized")
        }
    }
}
