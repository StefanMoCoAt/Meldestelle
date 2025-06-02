package at.mocode.plugins

import at.mocode.email.EmailService
import at.mocode.model.ApiResponse
import at.mocode.model.Nennung
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configures API routing for the application
 */
fun Application.configureApiRouting(emailService: EmailService?) {
    routing {
        // Group all API endpoints under /api
        route("/api") {
            // Debug endpoint to test if the server is responding to requests
            get("/debug") {
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Debug endpoint is working"
                    )
                )
            }

            // Group all nennung endpoints
            route("/nennung") {
                // Debug endpoint for the nennung endpoint (GET method)
                get {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "GET /api/nennung endpoint is working"
                        )
                    )
                }

                // Endpoint for form submissions
                post {
                    try {
                        // Parse the request body as Nennung
                        val nennung = call.receive<Nennung>()

                        // Log the received data
                        application.log.info("Received nennung: $nennung")

                        // Send email notification if email service is available
                        val emailSent = if (emailService != null) {
                            val result = emailService.sendNennungEmail(nennung)
                            if (result) {
                                application.log.info("Email notification sent successfully")
                            } else {
                                application.log.error("Failed to send email notification")
                            }
                            result
                        } else {
                            application.log.warn("Email service not available, skipping notification")
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
                        application.log.error("Error processing nennung", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse(
                                success = false,
                                message = "Fehler bei der Verarbeitung der Nennung. Bitte versuchen Sie es später erneut."
                            )
                        )
                    }
                }
            }
        }
    }
}
