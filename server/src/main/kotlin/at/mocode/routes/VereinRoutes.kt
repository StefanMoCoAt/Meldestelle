package at.mocode.routes

import at.mocode.repositories.PostgresVereinRepository
import at.mocode.repositories.VereinRepository
import at.mocode.stammdaten.Verein
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.vereinRoutes() {
    val vereinRepository: VereinRepository = PostgresVereinRepository()

    route("/api/vereine") {
        // GET /api/vereine - Get all clubs
        get {
            try {
                val vereine = vereinRepository.findAll()
                call.respond(HttpStatusCode.OK, vereine)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/vereine/{id} - Get club by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing verein ID")
                )
                val uuid = uuidFrom(id)
                val verein = vereinRepository.findById(uuid)
                if (verein != null) {
                    call.respond(HttpStatusCode.OK, verein)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Verein not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/vereine/oeps/{oepsVereinsNr} - Get club by OEPS number
        get("/oeps/{oepsVereinsNr}") {
            try {
                val oepsVereinsNr = call.parameters["oepsVereinsNr"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing OEPS Vereins number")
                )
                val verein = vereinRepository.findByOepsVereinsNr(oepsVereinsNr)
                if (verein != null) {
                    call.respond(HttpStatusCode.OK, verein)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Verein not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/vereine/search?q={query} - Search clubs
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val vereine = vereinRepository.search(query)
                call.respond(HttpStatusCode.OK, vereine)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/vereine/bundesland/{bundesland} - Get clubs by state
        get("/bundesland/{bundesland}") {
            try {
                val bundesland = call.parameters["bundesland"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing bundesland")
                )
                val vereine = vereinRepository.findByBundesland(bundesland)
                call.respond(HttpStatusCode.OK, vereine)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/vereine - Create new club
        post {
            try {
                val verein = call.receive<Verein>()
                val createdVerein = vereinRepository.create(verein)
                call.respond(HttpStatusCode.Created, createdVerein)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/vereine/{id} - Update club
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing verein ID")
                )
                val uuid = uuidFrom(id)
                val verein = call.receive<Verein>()
                val updatedVerein = vereinRepository.update(uuid, verein)
                if (updatedVerein != null) {
                    call.respond(HttpStatusCode.OK, updatedVerein)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Verein not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/vereine/{id} - Delete club
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing verein ID")
                )
                val uuid = uuidFrom(id)
                val deleted = vereinRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Verein not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
