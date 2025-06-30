package at.mocode.routes

import at.mocode.services.ServiceLocator
import at.mocode.stammdaten.Verein
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.vereinRoutes() {
    val vereinService = ServiceLocator.vereinService

    route("/vereine") {
        // GET /api/vereine - Get all clubs
        get {
            try {
                val vereine = vereinService.getAllVereine()
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
                val verein = vereinService.getVereinById(uuid)
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
                val verein = vereinService.getVereinByOepsNr(oepsVereinsNr)
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
                val vereine = vereinService.searchVereine(query)
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
                val vereine = vereinService.getVereineByBundesland(bundesland)
                call.respond(HttpStatusCode.OK, vereine)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/vereine - Create new club
        post {
            try {
                val verein = call.receive<Verein>()
                val createdVerein = vereinService.createVerein(verein)
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
                val updatedVerein = vereinService.updateVerein(uuid, verein)
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
                val deleted = vereinService.deleteVerein(uuid)
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
