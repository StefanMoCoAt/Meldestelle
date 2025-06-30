package at.mocode.routes

import at.mocode.model.Bewerb
import at.mocode.repositories.BewerbRepository
import at.mocode.repositories.PostgresBewerbRepository
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bewerbRoutes() {
    val bewerbRepository: BewerbRepository = PostgresBewerbRepository()

    route("/bewerbe") {
        // GET /api/bewerbe - Get all bewerbe
        get {
            try {
                val bewerbe = bewerbRepository.findAll()
                call.respond(HttpStatusCode.OK, bewerbe)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/bewerbe/{id} - Get bewerb by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing bewerb ID")
                )
                val uuid = uuidFrom(id)
                val bewerb = bewerbRepository.findById(uuid)
                if (bewerb != null) {
                    call.respond(HttpStatusCode.OK, bewerb)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Bewerb not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/bewerbe/search?q={query} - Search bewerbe
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val bewerbe = bewerbRepository.search(query)
                call.respond(HttpStatusCode.OK, bewerbe)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/bewerbe/turnier/{turnierId} - Get bewerbe by turnier ID
        get("/turnier/{turnierId}") {
            try {
                val turnierId = call.parameters["turnierId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing turnier ID")
                )
                val uuid = uuidFrom(turnierId)
                val bewerbe = bewerbRepository.findByTurnierId(uuid)
                call.respond(HttpStatusCode.OK, bewerbe)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/bewerbe/sparte/{sparte} - Get bewerbe by sparte
        get("/sparte/{sparte}") {
            try {
                val sparte = call.parameters["sparte"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing sparte parameter")
                )
                val bewerbe = bewerbRepository.findBySparte(sparte)
                call.respond(HttpStatusCode.OK, bewerbe)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/bewerbe/klasse/{klasse} - Get bewerbe by klasse
        get("/klasse/{klasse}") {
            try {
                val klasse = call.parameters["klasse"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing klasse parameter")
                )
                val bewerbe = bewerbRepository.findByKlasse(klasse)
                call.respond(HttpStatusCode.OK, bewerbe)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/bewerbe - Create new bewerb
        post {
            try {
                val bewerb = call.receive<Bewerb>()
                val createdBewerb = bewerbRepository.create(bewerb)
                call.respond(HttpStatusCode.Created, createdBewerb)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/bewerbe/{id} - Update bewerb
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing bewerb ID")
                )
                val uuid = uuidFrom(id)
                val bewerb = call.receive<Bewerb>()
                val updatedBewerb = bewerbRepository.update(uuid, bewerb)
                if (updatedBewerb != null) {
                    call.respond(HttpStatusCode.OK, updatedBewerb)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Bewerb not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/bewerbe/{id} - Delete bewerb
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing bewerb ID")
                )
                val uuid = uuidFrom(id)
                val deleted = bewerbRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Bewerb not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
