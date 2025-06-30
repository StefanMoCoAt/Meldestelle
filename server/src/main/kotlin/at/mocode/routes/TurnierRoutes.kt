package at.mocode.routes

import at.mocode.model.Turnier
import at.mocode.repositories.TurnierRepository
import at.mocode.repositories.PostgresTurnierRepository
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.turnierRoutes() {
    val turnierRepository: TurnierRepository = PostgresTurnierRepository()

    route("/turniere") {
        // GET /api/turniere - Get all turniere
        get {
            try {
                val turniere = turnierRepository.findAll()
                call.respond(HttpStatusCode.OK, turniere)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/turniere/{id} - Get turnier by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing turnier ID")
                )
                val uuid = uuidFrom(id)
                val turnier = turnierRepository.findById(uuid)
                if (turnier != null) {
                    call.respond(HttpStatusCode.OK, turnier)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Turnier not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/turniere/search?q={query} - Search turniere
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val turniere = turnierRepository.search(query)
                call.respond(HttpStatusCode.OK, turniere)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/turniere/veranstaltung/{veranstaltungId} - Get turniere by veranstaltung ID
        get("/veranstaltung/{veranstaltungId}") {
            try {
                val veranstaltungId = call.parameters["veranstaltungId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing veranstaltung ID")
                )
                val uuid = uuidFrom(veranstaltungId)
                val turniere = turnierRepository.findByVeranstaltungId(uuid)
                call.respond(HttpStatusCode.OK, turniere)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/turniere - Create new turnier
        post {
            try {
                val turnier = call.receive<Turnier>()
                val createdTurnier = turnierRepository.create(turnier)
                call.respond(HttpStatusCode.Created, createdTurnier)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/turniere/{id} - Update turnier
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing turnier ID")
                )
                val uuid = uuidFrom(id)
                val turnier = call.receive<Turnier>()
                val updatedTurnier = turnierRepository.update(uuid, turnier)
                if (updatedTurnier != null) {
                    call.respond(HttpStatusCode.OK, updatedTurnier)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Turnier not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/turniere/{id} - Delete turnier
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing turnier ID")
                )
                val uuid = uuidFrom(id)
                val deleted = turnierRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Turnier not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
