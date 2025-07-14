package at.mocode.routes

import at.mocode.model.Platz
import at.mocode.enums.PlatzTypE
import at.mocode.services.ServiceLocator
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.platzRoutes() {
    val platzService = ServiceLocator.platzService

    route("/plaetze") {
        // GET /api/plaetze - Get all places
        get {
            try {
                val plaetze = platzService.getAllPlaetze()
                call.respond(HttpStatusCode.OK, plaetze)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/plaetze/{id} - Get place by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing place ID")
                )
                val uuid = uuidFrom(id)
                val platz = platzService.getPlatzById(uuid)
                if (platz != null) {
                    call.respond(HttpStatusCode.OK, platz)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Place not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/plaetze/search?q={query} - Search places
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val plaetze = platzService.searchPlaetze(query)
                call.respond(HttpStatusCode.OK, plaetze)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/plaetze/turnier/{turnierId} - Get places by tournament ID
        get("/turnier/{turnierId}") {
            try {
                val turnierId = call.parameters["turnierId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing tournament ID")
                )
                val uuid = uuidFrom(turnierId)
                val plaetze = platzService.getPlaetzeByTurnierId(uuid)
                call.respond(HttpStatusCode.OK, plaetze)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/plaetze/typ/{typ} - Get places by type
        get("/typ/{typ}") {
            try {
                val typParam = call.parameters["typ"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing place type")
                )
                val typ = PlatzTypE.valueOf(typParam.uppercase())
                val plaetze = platzService.getPlaetzeByTyp(typ)
                call.respond(HttpStatusCode.OK, plaetze)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid place type: ${e.message}"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/plaetze - Create new place
        post {
            try {
                val platz = call.receive<Platz>()
                val createdPlatz = platzService.createPlatz(platz)
                call.respond(HttpStatusCode.Created, createdPlatz)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // PUT /api/plaetze/{id} - Update place
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing place ID")
                )
                val uuid = uuidFrom(id)
                val platz = call.receive<Platz>()
                val updatedPlatz = platzService.updatePlatz(uuid, platz)
                if (updatedPlatz != null) {
                    call.respond(HttpStatusCode.OK, updatedPlatz)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Place not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // DELETE /api/plaetze/{id} - Delete place
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing place ID")
                )
                val uuid = uuidFrom(id)
                val deleted = platzService.deletePlatz(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Place not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
