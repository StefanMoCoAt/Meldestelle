package at.mocode.routes

import at.mocode.config.DependencyInjection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

/**
 * Configures routes for tournament registration forms.
 */
fun Route.nennungRoutes() {
    val log = LoggerFactory.getLogger("NennungRoutes")
    val turnierRepository = DependencyInjection.turnierRepository
    val nennungRepository = DependencyInjection.nennungRepository
    val nennungView = DependencyInjection.nennungView
    val emailService = DependencyInjection.emailService

    // Route to display the registration form for a specific tournament
    get("/nennung/{number}") {
        val turnierNumber = call.parameters["number"]?.toIntOrNull()
        if (turnierNumber == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid tournament number")
            return@get
        }

        log.info("Displaying registration form for tournament $turnierNumber")

        // Get the tournament
        val turnier = turnierRepository.getTurnierByNumber(turnierNumber)
        if (turnier == null) {
            call.respond(HttpStatusCode.NotFound, "Tournament not found")
            return@get
        }

        // Render the registration form
        nennungView.renderNennungForm(call, turnier)
    }

    // Route to handle form submissions
    post("/nennung/{number}/submit") {
        val turnierNumber = call.parameters["number"]?.toIntOrNull()
        if (turnierNumber == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid tournament number")
            return@post
        }

        log.info("Processing registration form submission for tournament $turnierNumber")

        // Get the tournament
        val turnier = turnierRepository.getTurnierByNumber(turnierNumber)
        if (turnier == null) {
            call.respond(HttpStatusCode.NotFound, "Tournament not found")
            return@post
        }

        // Parse form parameters
        val formParameters = call.receiveParameters()
        val riderName = formParameters["riderName"] ?: ""
        val horseName = formParameters["horseName"] ?: ""
        val email = formParameters["email"] ?: ""
        val phone = formParameters["phone"] ?: ""
        val comments = formParameters["comments"] ?: ""
        val selectedEvents = formParameters.getAll("selectedEvents") ?: emptyList()

        // Validate required fields
        if (riderName.isBlank() || horseName.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Rider name and horse name are required")
            return@post
        }

        // Validate that at least one contact method is provided
        if (email.isBlank() && phone.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Either email or phone number is required")
            return@post
        }

        // Create Nennung object using repository
        val nennung = nennungRepository.createNennung(
            riderName = riderName,
            horseName = horseName,
            email = email,
            phone = phone,
            selectedEvents = selectedEvents,
            comments = comments,
            turnierNumber = turnierNumber
        )

        if (nennung == null) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to create registration")
            return@post
        }

        // Save the registration to the database
        val nennungId = nennungRepository.saveNennung(nennung)
        log.info("Registration saved with ID: $nennungId")

        // Send email notification
        val emailSent = emailService.sendNennungEmail(nennung)
        if (!emailSent) {
            log.error("Failed to send email notification for registration: $riderName with $horseName")
            // Continue anyway, we don't want to block the user if email fails
        }

        // Render confirmation page
        nennungView.renderConfirmationPage(call, turnier, riderName, horseName)
    }
}

/**
 * Extension function to register nennung routes.
 */
fun Application.configureNennungRoutes() {
    routing {
        nennungRoutes()
    }
}
