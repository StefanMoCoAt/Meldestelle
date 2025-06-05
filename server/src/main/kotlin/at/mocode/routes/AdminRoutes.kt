package at.mocode.routes

import at.mocode.model.Bewerb
import at.mocode.config.DependencyInjection
import at.mocode.repository.TurnierRepository
import at.mocode.views.AdminView
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

/**
 * Configures routes for tournament administration.
 */
fun Route.adminRoutes() {
    val log = LoggerFactory.getLogger("AdminRoutes")
    val turnierRepository = DependencyInjection.turnierRepository
    val adminView = AdminView()

    // Route to display the tournament management page
    get("/admin/tournaments") {
        log.info("Handling request to tournament management page")

        // Get all tournaments
        val turniere = turnierRepository.getAllTurniere()

        // Render the tournament management page
        adminView.renderTournamentManagementPage(call, turniere)
    }

    // Route to display the tournament edit page
    get("/admin/tournaments/edit/{number}") {
        val turnierNumber = call.parameters["number"]?.toIntOrNull()
        if (turnierNumber == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid tournament number")
            return@get
        }

        log.info("Displaying edit form for tournament $turnierNumber")

        // Get the tournament
        val turnier = turnierRepository.getTurnierByNumber(turnierNumber)
        if (turnier == null) {
            call.respond(HttpStatusCode.NotFound, "Tournament not found")
            return@get
        }

        // Render the edit form
        adminView.renderTournamentEditPage(call, turnier)
    }

    // Route to handle tournament creation
    post("/admin/tournaments/add") {
        log.info("Processing tournament creation")

        // Parse form parameters
        val formParameters = call.receiveParameters()
        val number = formParameters["number"]?.toIntOrNull()
        val name = formParameters["name"]
        val datum = formParameters["datum"]

        // Validate required fields
        if (number == null || name.isNullOrBlank() || datum.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "All fields are required")
            return@post
        }

        // Parse competitions
        val bewerbNummern = formParameters.getAll("bewerb-nummer[]")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        val bewerbTitel = formParameters.getAll("bewerb-titel[]") ?: emptyList()
        val bewerbKlasse = formParameters.getAll("bewerb-klasse[]") ?: emptyList()
        val bewerbTask = formParameters.getAll("bewerb-task[]") ?: emptyList()

        // Create list of competitions
        val bewerbe = mutableListOf<Bewerb>()
        for (i in bewerbNummern.indices) {
            if (i < bewerbTitel.size && i < bewerbKlasse.size && i < bewerbTask.size) {
                bewerbe.add(
                    Bewerb(
                        nummer = bewerbNummern[i],
                        titel = bewerbTitel[i],
                        klasse = bewerbKlasse[i],
                        task = if (bewerbTask[i].isNotBlank()) bewerbTask[i] else null
                    )
                )
            }
        }

        // Create the tournament
        val turnier = turnierRepository.createTurnier(number, name, datum, bewerbe)
        if (turnier == null) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to create tournament")
            return@post
        }

        // Redirect to tournament management page
        call.respondRedirect("/admin/tournaments")
    }

    // Route to handle tournament update
    post("/admin/tournaments/update/{number}") {
        val turnierNumber = call.parameters["number"]?.toIntOrNull()
        if (turnierNumber == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid tournament number")
            return@post
        }

        log.info("Processing tournament update for tournament $turnierNumber")

        // Parse form parameters
        val formParameters = call.receiveParameters()
        val name = formParameters["name"]
        val datum = formParameters["datum"]

        // Validate required fields
        if (name.isNullOrBlank() || datum.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "All fields are required")
            return@post
        }

        // Parse competitions
        val bewerbNummern = formParameters.getAll("bewerb-nummer[]")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        val bewerbTitel = formParameters.getAll("bewerb-titel[]") ?: emptyList()
        val bewerbKlasse = formParameters.getAll("bewerb-klasse[]") ?: emptyList()
        val bewerbTask = formParameters.getAll("bewerb-task[]") ?: emptyList()

        // Create list of competitions
        val bewerbe = mutableListOf<Bewerb>()
        for (i in bewerbNummern.indices) {
            if (i < bewerbTitel.size && i < bewerbKlasse.size && i < bewerbTask.size) {
                bewerbe.add(
                    Bewerb(
                        nummer = bewerbNummern[i],
                        titel = bewerbTitel[i],
                        klasse = bewerbKlasse[i],
                        task = if (bewerbTask[i].isNotBlank()) bewerbTask[i] else null
                    )
                )
            }
        }

        // Update the tournament
        val turnier = turnierRepository.updateTurnier(turnierNumber, name, datum, bewerbe)
        if (turnier == null) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to update tournament")
            return@post
        }

        // Redirect to tournament management page
        call.respondRedirect("/admin/tournaments")
    }
}

/**
 * Extension function to register admin routes.
 */
fun Application.configureAdminRoutes() {
    routing {
        adminRoutes()
    }
}
