package at.mocode.plugins

import at.mocode.database.TurnierRepository
import at.mocode.email.EmailService
import at.mocode.model.ApiResponse
import at.mocode.model.Nennung
import at.mocode.model.Turnier
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configures API routing for the application.
 *
 * This function sets up all REST API endpoints for the application, organized into logical groups:
 * - /api/debug - Debug endpoint to verify API functionality
 * - /api/turniere - CRUD operations for tournaments
 * - /api/nennung - Endpoints for handling tournament registrations
 *
 * @param emailService Optional email service for sending notifications
 * @param turnierRepository Repository for tournament data access
 */
fun Application.configureApiRouting(emailService: EmailService?, turnierRepository: TurnierRepository) {
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

            // Tournament endpoints (/api/turniere)
            // Provides CRUD operations for tournament management
            route("/turniere") {
                // GET /api/turniere - Retrieve all tournaments
                get {
                    try {
                        // Get tournaments from the database
                        val turniere = turnierRepository.getAllTurniere()
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

                // POST /api/turniere - Create a new tournament
                post {
                    try {
                        val turnier = call.receive<Turnier>()

                        // Save the tournament to the database
                        val createdTurnier = turnierRepository.createTurnier(turnier)
                        application.log.info("Created tournament: $createdTurnier")

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

                // Tournament operations by ID (/api/turniere/{number})
                // Provides operations for a specific tournament identified by its number
                route("/{number}") {
                    // PUT /api/turniere/{number} - Update an existing tournament
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

                            // Update the tournament in the database
                            val updatedTurnier = turnierRepository.updateTurnier(number, turnier)
                            if (updatedTurnier == null) {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ApiResponse(
                                        success = false,
                                        message = "Turnier mit Nummer $number nicht gefunden"
                                    )
                                )
                                return@put
                            }

                            application.log.info("Updated tournament: $updatedTurnier")

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

                    // DELETE /api/turniere/{number} - Delete an existing tournament
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

                            // Delete the tournament from the database
                            val deleted = turnierRepository.deleteTurnier(number)
                            if (!deleted) {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ApiResponse(
                                        success = false,
                                        message = "Turnier mit Nummer $number nicht gefunden"
                                    )
                                )
                                return@delete
                            }

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

            // Registration endpoints (/api/nennung)
            // Handles tournament registration submissions and related operations
            route("/nennung") {
                // GET /api/nennung - Debug endpoint to verify registration API functionality
                get {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "GET /api/nennung endpoint is working"
                        )
                    )
                }

                // POST /api/nennung - Submit a new tournament registration
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
