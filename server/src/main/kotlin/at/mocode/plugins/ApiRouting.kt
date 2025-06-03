package at.mocode.plugins

import at.mocode.email.EmailService
import at.mocode.model.ApiResponse
import at.mocode.model.Bewerb
import at.mocode.model.Nennung
import at.mocode.model.Turnier
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

            // Group all tournament endpoints
            route("/turniere") {
                // Get all tournaments
                get {
                    try {
                        // For now, return sample data
                        // In a real application, this would come from a database
                        val turniere = listOf(
                            Turnier(
                                name = "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ",
                                datum = "7.JUNI 2025",
                                number = 25319,
                                bewerbe = listOf(
                                    Bewerb(1, "Pony Stilspringprüfung", "60 cm", null),
                                    Bewerb(2, "Stilspringprüfung", "60 cm", null),
                                    Bewerb(3, "Pony Stilspringprüfung", "70 cm", null),
                                    Bewerb(4, "Stilspringprüfung", "80 cm", null),
                                    Bewerb(5, "Pony Stilspringprüfung", "95 cm", null),
                                    Bewerb(6, "Stilspringprüfung", "95 cm", null),
                                    Bewerb(7, "Einlaufspringprüfung", "95cm", null),
                                    Bewerb(8, "Springpferdeprüfung", "105 cm", null),
                                    Bewerb(9, "Stilspringprüfung", "105 cm", null),
                                    Bewerb(10, "Standardspringprüfung", "105cm", null)
                                )
                            ),
                            Turnier(
                                name = "CSN-B LAMBACH, OÖ",
                                datum = "14.JUNI 2025",
                                number = 25320,
                                bewerbe = listOf(
                                    Bewerb(1, "Stilspringprüfung", "80 cm", null),
                                    Bewerb(2, "Stilspringprüfung", "95 cm", null),
                                    Bewerb(3, "Standardspringprüfung", "105 cm", null)
                                )
                            )
                        )

                        call.respond(HttpStatusCode.OK, turniere)
                    } catch (e: Exception) {
                        application.log.error("Error getting tournaments", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse(
                                success = false,
                                message = "Fehler beim Laden der Turniere. Bitte versuchen Sie es später erneut."
                            )
                        )
                    }
                }

                // Create a new tournament
                post {
                    try {
                        val turnier = call.receive<Turnier>()

                        // In a real application, this would be saved to a database
                        // For now, just log it and return success
                        application.log.info("Created tournament: $turnier")

                        call.respond(
                            HttpStatusCode.Created,
                            ApiResponse(
                                success = true,
                                message = "Turnier erfolgreich erstellt"
                            )
                        )
                    } catch (e: Exception) {
                        application.log.error("Error creating tournament", e)
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ApiResponse(
                                success = false,
                                message = "Fehler beim Erstellen des Turniers. Bitte versuchen Sie es später erneut."
                            )
                        )
                    }
                }

                // Update or delete a specific tournament
                route("/{number}") {
                    // Update a tournament
                    put {
                        try {
                            val number = call.parameters["number"]?.toIntOrNull()
                            if (number == null) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ApiResponse(
                                        success = false,
                                        message = "Ungültige Turnier-Nummer"
                                    )
                                )
                                return@put
                            }

                            val turnier = call.receive<Turnier>()

                            // In a real application, this would update the tournament in a database
                            // For now, just log it and return success
                            application.log.info("Updated tournament: $turnier")

                            call.respond(
                                HttpStatusCode.OK,
                                ApiResponse(
                                    success = true,
                                    message = "Turnier erfolgreich aktualisiert"
                                )
                            )
                        } catch (e: Exception) {
                            application.log.error("Error updating tournament", e)
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ApiResponse(
                                    success = false,
                                    message = "Fehler beim Aktualisieren des Turniers. Bitte versuchen Sie es später erneut."
                                )
                            )
                        }
                    }

                    // Delete a tournament
                    delete {
                        try {
                            val number = call.parameters["number"]?.toIntOrNull()
                            if (number == null) {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    ApiResponse(
                                        success = false,
                                        message = "Ungültige Turnier-Nummer"
                                    )
                                )
                                return@delete
                            }

                            // In a real application, this would delete the tournament from a database
                            // For now, just log it and return success
                            application.log.info("Deleted tournament with number: $number")

                            call.respond(
                                HttpStatusCode.OK,
                                ApiResponse(
                                    success = true,
                                    message = "Turnier erfolgreich gelöscht"
                                )
                            )
                        } catch (e: Exception) {
                            application.log.error("Error deleting tournament", e)
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ApiResponse(
                                    success = false,
                                    message = "Fehler beim Löschen des Turniers. Bitte versuchen Sie es später erneut."
                                )
                            )
                        }
                    }
                }
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
