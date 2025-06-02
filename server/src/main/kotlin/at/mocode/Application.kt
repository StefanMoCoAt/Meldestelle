package at.mocode

import at.mocode.config.EmailConfig
import at.mocode.email.EmailService
import at.mocode.model.ApiResponse
import at.mocode.model.Nennung
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
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

    // Install CORS to allow cross-origin requests
    install(CORS) {
        // Allow requests from the frontend origin
        allowHost("localhost:8080", schemes = listOf("http", "https"))

        // Allow requests with credentials (cookies, authorization headers)
        allowCredentials = true

        // Allow common HTTP methods
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        // Allow common headers
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        // Maximum age (in seconds) the browser should cache CORS information
        maxAgeInSeconds = 3600
    }

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

    routing {
        // Serve static files from resources/static directory
        static("/static") {
            resources("static")
        }

        get("/") {
            call.respondText("Ktor ist erreichbar! <br><a href='/static/test.html'>Go to API Test Page</a>", contentType = io.ktor.http.ContentType.Text.Html)
        }

        // Debug endpoint to test if the server is responding to requests
        get("/api/debug") {
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    message = "Debug endpoint is working"
                )
            )
        }

        // Debug endpoint without leading slash
        get("api/debug") {
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    message = "Debug endpoint without leading slash is working"
                )
            )
        }

        // Debug endpoint for the nennung endpoint (GET method)
        get("api/nennung") {
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    message = "GET api/nennung endpoint is working"
                )
            )
        }

        // Debug endpoint for the nennung endpoint with leading slash (GET method)
        get("/api/nennung") {
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    message = "GET /api/nennung endpoint is working"
                )
            )
        }

        // Endpoint for form submissions with leading slash
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
                    ApiResponse(
                        success = true,
                        emailSent = emailSent,
                        message = "Nennung erfolgreich empfangen"
                    )
                )
            } catch (e: Exception) {
                log.error("Error processing nennung", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Fehler bei der Verarbeitung der Nennung: ${e.message}"
                    )
                )
            }
        }

        // Endpoint for form submissions without leading slash (to handle frontend requests)
        post("api/nennung") {
            try {
                // Parse the request body as Nennung
                val nennung = call.receive<Nennung>()

                // Log the received data
                log.info("Received nennung (no leading slash): $nennung")

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
                    ApiResponse(
                        success = true,
                        emailSent = emailSent,
                        message = "Nennung erfolgreich empfangen"
                    )
                )
            } catch (e: Exception) {
                log.error("Error processing nennung (no leading slash)", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Fehler bei der Verarbeitung der Nennung: ${e.message}"
                    )
                )
            }
        }
    }
}
