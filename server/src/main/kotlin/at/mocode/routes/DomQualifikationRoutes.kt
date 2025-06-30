package at.mocode.routes

import at.mocode.repositories.DomQualifikationRepository
import at.mocode.repositories.PostgresDomQualifikationRepository
import at.mocode.model.domaene.DomQualifikation
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

fun Route.domQualifikationRoutes() {
    val domQualifikationRepository: DomQualifikationRepository = PostgresDomQualifikationRepository()

    route("/api/dom-qualifikationen") {
        // GET /api/dom-qualifikationen - Get all qualifications
        get {
            try {
                val qualifikationen = domQualifikationRepository.findAll()
                call.respond(HttpStatusCode.OK, qualifikationen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-qualifikationen/{id} - Get qualification by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing qualification ID")
                )
                val uuid = uuidFrom(id)
                val qualifikation = domQualifikationRepository.findById(uuid)
                if (qualifikation != null) {
                    call.respond(HttpStatusCode.OK, qualifikation)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Qualification not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-qualifikationen/person/{personId} - Get qualifications by person ID
        get("/person/{personId}") {
            try {
                val personId = call.parameters["personId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing person ID")
                )
                val uuid = uuidFrom(personId)
                val qualifikationen = domQualifikationRepository.findByPersonId(uuid)
                call.respond(HttpStatusCode.OK, qualifikationen)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-qualifikationen/person/{personId}/active - Get active qualifications by person ID
        get("/person/{personId}/active") {
            try {
                val personId = call.parameters["personId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing person ID")
                )
                val uuid = uuidFrom(personId)
                val qualifikationen = domQualifikationRepository.findActiveByPersonId(uuid)
                call.respond(HttpStatusCode.OK, qualifikationen)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-qualifikationen/qual-typ/{qualTypId} - Get qualifications by qualification type
        get("/qual-typ/{qualTypId}") {
            try {
                val qualTypId = call.parameters["qualTypId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing qualification type ID")
                )
                val uuid = uuidFrom(qualTypId)
                val qualifikationen = domQualifikationRepository.findByQualTypId(uuid)
                call.respond(HttpStatusCode.OK, qualifikationen)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/dom-qualifikationen/validity-period?from={fromDate}&to={toDate} - Get qualifications by validity period
        get("/validity-period") {
            try {
                val fromDateStr = call.request.queryParameters["from"]
                val toDateStr = call.request.queryParameters["to"]

                val fromDate = fromDateStr?.let { LocalDate.parse(it) }
                val toDate = toDateStr?.let { LocalDate.parse(it) }

                val qualifikationen = domQualifikationRepository.findByValidityPeriod(fromDate, toDate)
                call.respond(HttpStatusCode.OK, qualifikationen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid date format. Use YYYY-MM-DD"))
            }
        }

        // GET /api/dom-qualifikationen/search?q={query} - Search qualifications
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val qualifikationen = domQualifikationRepository.search(query)
                call.respond(HttpStatusCode.OK, qualifikationen)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/dom-qualifikationen - Create new qualification
        post {
            try {
                val qualifikation = call.receive<DomQualifikation>()
                val createdQualifikation = domQualifikationRepository.create(qualifikation)
                call.respond(HttpStatusCode.Created, createdQualifikation)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/dom-qualifikationen/{id} - Update qualification
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing qualification ID")
                )
                val uuid = uuidFrom(id)
                val qualifikation = call.receive<DomQualifikation>()
                val updatedQualifikation = domQualifikationRepository.update(uuid, qualifikation)
                if (updatedQualifikation != null) {
                    call.respond(HttpStatusCode.OK, updatedQualifikation)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Qualification not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/dom-qualifikationen/{id} - Delete qualification
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing qualification ID")
                )
                val uuid = uuidFrom(id)
                val deleted = domQualifikationRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Qualification not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
