package at.mocode.routes

import at.mocode.model.Abteilung
import at.mocode.repositories.AbteilungRepository
import at.mocode.repositories.PostgresAbteilungRepository
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.abteilungRoutes() {
    val abteilungRepository: AbteilungRepository = PostgresAbteilungRepository()

    route("/api/abteilungen") {
        // GET /api/abteilungen - Get all abteilungen
        get {
            try {
                val abteilungen = abteilungRepository.findAll()
                call.respond(HttpStatusCode.OK, abteilungen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/abteilungen/{id} - Get abteilung by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing abteilung ID")
                )
                val uuid = uuidFrom(id)
                val abteilung = abteilungRepository.findById(uuid)
                if (abteilung != null) {
                    call.respond(HttpStatusCode.OK, abteilung)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Abteilung not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/abteilungen/search?q={query} - Search abteilungen
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val abteilungen = abteilungRepository.search(query)
                call.respond(HttpStatusCode.OK, abteilungen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/abteilungen/bewerb/{bewerbId} - Get abteilungen by bewerb ID
        get("/bewerb/{bewerbId}") {
            try {
                val bewerbId = call.parameters["bewerbId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing bewerb ID")
                )
                val uuid = uuidFrom(bewerbId)
                val abteilungen = abteilungRepository.findByBewerbId(uuid)
                call.respond(HttpStatusCode.OK, abteilungen)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/abteilungen/aktiv/{istAktiv} - Get abteilungen by active status
        get("/aktiv/{istAktiv}") {
            try {
                val istAktiv = call.parameters["istAktiv"]?.toBoolean() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing or invalid aktiv parameter")
                )
                val abteilungen = abteilungRepository.findByAktiv(istAktiv)
                call.respond(HttpStatusCode.OK, abteilungen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/abteilungen - Create new abteilung
        post {
            try {
                val abteilung = call.receive<Abteilung>()
                val createdAbteilung = abteilungRepository.create(abteilung)
                call.respond(HttpStatusCode.Created, createdAbteilung)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/abteilungen/{id} - Update abteilung
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing abteilung ID")
                )
                val uuid = uuidFrom(id)
                val abteilung = call.receive<Abteilung>()
                val updatedAbteilung = abteilungRepository.update(uuid, abteilung)
                if (updatedAbteilung != null) {
                    call.respond(HttpStatusCode.OK, updatedAbteilung)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Abteilung not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/abteilungen/{id} - Delete abteilung
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing abteilung ID")
                )
                val uuid = uuidFrom(id)
                val deleted = abteilungRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Abteilung not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
