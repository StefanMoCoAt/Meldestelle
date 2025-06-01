package at.mocode

import at.mocode.config.EmailConfig
import at.mocode.email.EmailService
import at.mocode.model.Nennung
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
// Install ContentNegotiation to handle JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    // Initialize EmailService if the configuration is valid
    val emailService = if (EmailConfig.isValid()) {
        EmailService(
            smtpHost = EmailConfig.smtpHost,
            smtpPort = EmailConfig.smtpPort,
            smtpUsername = EmailConfig.smtpUsername,
            smtpPassword = EmailConfig.smtpPassword,
            recipientEmail = EmailConfig.recipientEmail
        )
    } else {
        log.warn("Email configuration is invalid: ${EmailConfig.getValidationErrors()}")
        null
    }

    routing {
        get("/") {
            call.respondText("Ktor ist erreichbar!")
        }

        // Endpoint for form submissions
        post("/api/nennung") {
            try {
                // Parse the request body as Nennung
                val nennung = call.receive<Nennung>()

                // Log the received data
                log.info("Received nennung: $nennung")

                // Send email notification if email service is available
                val emailSent = if (emailService != null) {
                    val result = emailService.sendNennungEmail(nennung)
                    if (result) {
                        log.info("Email notification sent successfully")
                    } else {
                        log.error("Failed to send email notification")
                    }
                    result
                } else {
                    log.warn("Email service not available, skipping notification")
                    false
                }

                // Respond with success, even if email failed (we don't want to block the user)
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "success" to true,
                        "emailSent" to emailSent,
                        "message" to "Nennung erfolgreich empfangen"
                    )
                )
            } catch (e: Exception) {
                log.error("Error processing nennung", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf(
                        "success" to false,
                        "message" to "Fehler bei der Verarbeitung der Nennung: ${e.message}"
                    )
                )
            }
        }
    }
}
