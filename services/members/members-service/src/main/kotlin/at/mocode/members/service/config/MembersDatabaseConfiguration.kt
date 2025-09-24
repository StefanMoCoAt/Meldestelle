package at.mocode.members.service.config

import at.mocode.core.utils.database.DatabaseConfig
import at.mocode.core.utils.database.DatabaseFactory
import at.mocode.members.infrastructure.persistence.MemberTable
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Database configuration for the Members Service.
 *
 * This configuration ensures that Database.connect() is called properly
 * before any Exposed operations are performed.
 */
@Configuration
@Profile("!test")
class MembersDatabaseConfiguration {

    private val log = LoggerFactory.getLogger(MembersDatabaseConfiguration::class.java)

    @PostConstruct
    fun initializeDatabase() {
        log.info("Initializing database schema for Members Service...")

        try {
            // Database connection is already initialized by the gateway
            // Only initialize the schema for this service
            transaction {
                SchemaUtils.createMissingTablesAndColumns(MemberTable)
                log.info("Members database schema initialized successfully")
            }
        } catch (e: Exception) {
            log.error("Failed to initialize database schema", e)
            throw e
        }
    }

    @PreDestroy
    fun closeDatabase() {
        log.info("Closing database connection for Members Service...")
        try {
            DatabaseFactory.close()
            log.info("Database connection closed successfully")
        } catch (e: Exception) {
            log.error("Error closing database connection", e)
        }
    }
}

/**
 * Test-specific database configuration.
 */
@Configuration
@Profile("test")
class MembersTestDatabaseConfiguration {

    private val log = LoggerFactory.getLogger(MembersTestDatabaseConfiguration::class.java)

    @PostConstruct
    fun initializeTestDatabase() {
        log.info("Initializing test database connection for Members Service...")

        try {
            // Use H2 in-memory database for tests
            val testConfig = DatabaseConfig(
                jdbcUrl = "jdbc:h2:mem:members_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                username = "sa",
                password = "",
                driverClassName = "org.h2.Driver",
                maxPoolSize = 5,
                minPoolSize = 1,
                autoMigrate = true
            )

            DatabaseFactory.init(testConfig)
            log.info("Test database connection initialized successfully")

            // Initialize database schema for tests
            transaction {
                SchemaUtils.createMissingTablesAndColumns(MemberTable)
                log.info("Test members database schema initialized successfully")
            }
        } catch (e: Exception) {
            log.error("Failed to initialize test database connection", e)
            throw e
        }
    }

    @PreDestroy
    fun closeTestDatabase() {
        log.info("Closing test database connection for Members Service...")
        try {
            DatabaseFactory.close()
            log.info("Test database connection closed successfully")
        } catch (e: Exception) {
            log.error("Error closing test database connection", e)
        }
    }
}
