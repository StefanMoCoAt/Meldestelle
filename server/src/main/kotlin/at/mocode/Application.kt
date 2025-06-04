package at.mocode

import at.mocode.config.DatabaseConfig
import at.mocode.config.EmailConfig
import at.mocode.database.DatabaseFactory
import at.mocode.database.TurnierRepository
import at.mocode.email.EmailService
import at.mocode.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

/**
 * Application entry point.
 * Starts the Ktor server using the Netty engine.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Application module configuration.
 * This function is called by Ktor during server startup to configure all components.
 */
fun Application.module() {
    // Configure JSON serialization for request/response handling
    configureSerialization()

    // Configure Cross-Origin Resource Sharing for frontend-backend communication
    configureCORS()

    // Initialize database configuration and connection factory
    DatabaseConfig.init(this)
    DatabaseFactory.init(this)

    // Initialize the tournament repository and add sample data if the database is empty
    val turnierRepository = TurnierRepository()
    turnierRepository.addSampleDataIfEmpty()

    // Initialize EmailService if the configuration is valid
    val emailService = if (EmailConfig.isValid()) {
        EmailService(
            smtpHost = EmailConfig.smtpHost,
            smtpPort = EmailConfig.smtpPort,
            smtpUsername = EmailConfig.smtpUsername,
            smtpPassword = EmailConfig.smtpPassword,
            recipientEmail = EmailConfig.recipientEmail,
            senderEmail = EmailConfig.senderEmail
        )
    } else {
        log.warn("Email configuration is invalid: ${EmailConfig.getValidationErrors()}")
        null
    }

    // Configure routing for different parts of the application
    configureStaticRouting()     // Serves static files
    configureHealthRouting()     // Health check endpoints
    configureApiRouting(emailService, turnierRepository)  // API endpoints
}
