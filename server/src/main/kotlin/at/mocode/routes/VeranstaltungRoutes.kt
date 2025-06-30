package at.mocode.routes

import at.mocode.model.Veranstaltung
import at.mocode.repositories.VeranstaltungRepository
import at.mocode.services.ServiceLocator
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.veranstaltungRoutes() {
    val veranstaltungRepository: VeranstaltungRepository = ServiceLocator.veranstaltungRepository

    route("/veranstaltungen") {
        // GET /api/veranstaltungen - Get all veranstaltungen
        get {
            try {
                val veranstaltungen = veranstaltungRepository.findAll()
                call.respond(HttpStatusCode.OK, veranstaltungen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/veranstaltungen/{id} - Get veranstaltung by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing veranstaltung ID")
                )
                val uuid = uuidFrom(id)
                val veranstaltung = veranstaltungRepository.findById(uuid)
                if (veranstaltung != null) {
                    call.respond(HttpStatusCode.OK, veranstaltung)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Veranstaltung not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/veranstaltungen/search?q={query} - Search veranstaltungen
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val veranstaltungen = veranstaltungRepository.search(query)
                call.respond(HttpStatusCode.OK, veranstaltungen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/veranstaltungen - Create new veranstaltung
        post {
            try {
                val veranstaltung = call.receive<Veranstaltung>()
                val createdVeranstaltung = veranstaltungRepository.create(veranstaltung)
                call.respond(HttpStatusCode.Created, createdVeranstaltung)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/veranstaltungen/{id} - Update veranstaltung
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing veranstaltung ID")
                )
                val uuid = uuidFrom(id)
                val veranstaltung = call.receive<Veranstaltung>()
                val updatedVeranstaltung = veranstaltungRepository.update(uuid, veranstaltung)
                if (updatedVeranstaltung != null) {
                    call.respond(HttpStatusCode.OK, updatedVeranstaltung)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Veranstaltung not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/veranstaltungen/{id} - Delete veranstaltung
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing veranstaltung ID")
                )
                val uuid = uuidFrom(id)
                val deleted = veranstaltungRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Veranstaltung not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
