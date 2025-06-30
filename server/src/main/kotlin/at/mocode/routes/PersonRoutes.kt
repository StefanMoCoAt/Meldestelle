package at.mocode.routes

import at.mocode.repositories.PersonRepository
import at.mocode.repositories.PostgresPersonRepository
import at.mocode.stammdaten.Person
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.personRoutes() {
    val personRepository: PersonRepository = PostgresPersonRepository()

    route("/api/persons") {
        // GET /api/persons - Get all persons
        get {
            try {
                val persons = personRepository.findAll()
                call.respond(HttpStatusCode.OK, persons)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/persons/{id} - Get person by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing person ID")
                )
                val uuid = uuidFrom(id)
                val person = personRepository.findById(uuid)
                if (person != null) {
                    call.respond(HttpStatusCode.OK, person)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Person not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/persons/oeps/{oepsSatzNr} - Get person by OEPS number
        get("/oeps/{oepsSatzNr}") {
            try {
                val oepsSatzNr = call.parameters["oepsSatzNr"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing OEPS Satz number")
                )
                val person = personRepository.findByOepsSatzNr(oepsSatzNr)
                if (person != null) {
                    call.respond(HttpStatusCode.OK, person)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Person not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/persons/search?q={query} - Search persons
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val persons = personRepository.search(query)
                call.respond(HttpStatusCode.OK, persons)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/persons/verein/{vereinId} - Get persons by club ID
        get("/verein/{vereinId}") {
            try {
                val vereinId = call.parameters["vereinId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing verein ID")
                )
                val uuid = uuidFrom(vereinId)
                val persons = personRepository.findByVereinId(uuid)
                call.respond(HttpStatusCode.OK, persons)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/persons - Create new person
        post {
            try {
                val person = call.receive<Person>()
                val createdPerson = personRepository.create(person)
                call.respond(HttpStatusCode.Created, createdPerson)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/persons/{id} - Update person
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing person ID")
                )
                val uuid = uuidFrom(id)
                val person = call.receive<Person>()
                val updatedPerson = personRepository.update(uuid, person)
                if (updatedPerson != null) {
                    call.respond(HttpStatusCode.OK, updatedPerson)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Person not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/persons/{id} - Delete person
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing person ID")
                )
                val uuid = uuidFrom(id)
                val deleted = personRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Person not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
