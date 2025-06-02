package at.mocode

import at.mocode.config.EmailConfig
import at.mocode.email.EmailService
import at.mocode.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // Configure serialization
    configureSerialization()

    // Configure CORS
    configureCORS()

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

    // Configure routing
    configureStaticRouting()
    configureHealthRouting()
    configureApiRouting(emailService)
}
