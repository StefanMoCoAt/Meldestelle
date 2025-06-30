package at.mocode.routes

import at.mocode.repositories.DomLizenzRepository
import at.mocode.repositories.PostgresDomLizenzRepository
import at.mocode.model.domaene.DomLizenz
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.domLizenzRoutes() {
    val domLizenzRepository: DomLizenzRepository = PostgresDomLizenzRepository()

    route("/dom-lizenzen") {
        // GET /api/dom-lizenzen - Get all licenses
        get {
            try {
                val lizenzen = domLizenzRepository.findAll()
                call.respond(HttpStatusCode.OK, lizenzen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-lizenzen/{id} - Get license by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing license ID")
                )
                val uuid = uuidFrom(id)
                val lizenz = domLizenzRepository.findById(uuid)
                if (lizenz != null) {
                    call.respond(HttpStatusCode.OK, lizenz)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "License not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-lizenzen/person/{personId} - Get licenses by person ID
        get("/person/{personId}") {
            try {
                val personId = call.parameters["personId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing person ID")
                )
                val uuid = uuidFrom(personId)
                val lizenzen = domLizenzRepository.findByPersonId(uuid)
                call.respond(HttpStatusCode.OK, lizenzen)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-lizenzen/person/{personId}/active - Get active licenses by person ID
        get("/person/{personId}/active") {
            try {
                val personId = call.parameters["personId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing person ID")
                )
                val uuid = uuidFrom(personId)
                val lizenzen = domLizenzRepository.findActiveByPersonId(uuid)
                call.respond(HttpStatusCode.OK, lizenzen)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-lizenzen/lizenz-typ/{lizenzTypGlobalId} - Get licenses by license type
        get("/lizenz-typ/{lizenzTypGlobalId}") {
            try {
                val lizenzTypGlobalId = call.parameters["lizenzTypGlobalId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing license type ID")
                )
                val uuid = uuidFrom(lizenzTypGlobalId)
                val lizenzen = domLizenzRepository.findByLizenzTypGlobalId(uuid)
                call.respond(HttpStatusCode.OK, lizenzen)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-lizenzen/validity-year/{year} - Get licenses by validity year
        get("/validity-year/{year}") {
            try {
                val year = call.parameters["year"]?.toIntOrNull() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid year format")
                )
                val lizenzen = domLizenzRepository.findByValidityYear(year)
                call.respond(HttpStatusCode.OK, lizenzen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-lizenzen/search?q={query} - Search licenses
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val lizenzen = domLizenzRepository.search(query)
                call.respond(HttpStatusCode.OK, lizenzen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/dom-lizenzen - Create new license
        post {
            try {
                val lizenz = call.receive<DomLizenz>()
                val createdLizenz = domLizenzRepository.create(lizenz)
                call.respond(HttpStatusCode.Created, createdLizenz)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/dom-lizenzen/{id} - Update license
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing license ID")
                )
                val uuid = uuidFrom(id)
                val lizenz = call.receive<DomLizenz>()
                val updatedLizenz = domLizenzRepository.update(uuid, lizenz)
                if (updatedLizenz != null) {
                    call.respond(HttpStatusCode.OK, updatedLizenz)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "License not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/dom-lizenzen/{id} - Delete license
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing license ID")
                )
                val uuid = uuidFrom(id)
                val deleted = domLizenzRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "License not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
