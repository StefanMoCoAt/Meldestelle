package at.mocode.routes

import at.mocode.di.ServiceRegistry
import at.mocode.di.resolve
import at.mocode.services.PersonService
import at.mocode.stammdaten.Person
import at.mocode.utils.ResponseUtils.handleException
import at.mocode.utils.ResponseUtils.respondCreated
import at.mocode.utils.ResponseUtils.respondNoContent
import at.mocode.utils.ResponseUtils.respondNotFound
import at.mocode.utils.ResponseUtils.respondSuccess
import at.mocode.utils.ResponseUtils.respondValidationError
import at.mocode.validation.ValidationException
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.personRoutes() {
    val personService: PersonService = ServiceRegistry.serviceLocator.resolve()

    route("/persons") {
        // GET /api/persons - Get all persons
        get {
            try {
                val persons = personService.getAllPersons()
                call.respondSuccess(persons)
            } catch (e: Exception) {
                call.handleException(e, "getting all persons")
            }
        }

        // GET /api/persons/{id} - Get person by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respondValidationError("Missing person ID")
                val uuid = uuidFrom(id)
                val person = personService.getPersonById(uuid)
                if (person != null) {
                    call.respondSuccess(person)
                } else {
                    call.respondNotFound("Person")
                }
            } catch (e: Exception) {
                call.handleException(e, "getting person by ID")
            }
        }

        // GET /api/persons/oeps/{oepsSatzNr} - Get person by OEPS number
        get("/oeps/{oepsSatzNr}") {
            try {
                val oepsSatzNr = call.parameters["oepsSatzNr"] ?: return@get call.respondValidationError("Missing OEPS Satz number")
                val person = personService.getPersonByOepsSatzNr(oepsSatzNr)
                if (person != null) {
                    call.respondSuccess(person)
                } else {
                    call.respondNotFound("Person")
                }
            } catch (e: Exception) {
                call.handleException(e, "getting person by OEPS number")
            }
        }

        // GET /api/persons/search?q={query} - Search persons
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respondValidationError("Missing search query parameter 'q'")
                val persons = personService.searchPersons(query)
                call.respondSuccess(persons)
            } catch (e: Exception) {
                call.handleException(e, "searching persons")
            }
        }

        // GET /api/persons/verein/{vereinId} - Get persons by club ID
        get("/verein/{vereinId}") {
            try {
                val vereinId = call.parameters["vereinId"] ?: return@get call.respondValidationError("Missing verein ID")
                val uuid = uuidFrom(vereinId)
                val persons = personService.getPersonsByVereinId(uuid)
                call.respondSuccess(persons)
            } catch (e: Exception) {
                call.handleException(e, "getting persons by verein ID")
            }
        }

        // POST /api/persons - Create new person
        post {
            try {
                val person = call.receive<Person>()
                val createdPerson = personService.createPerson(person)
                call.respondCreated(createdPerson)
            } catch (e: ValidationException) {
                call.respondValidationError(
                    "Person validation failed",
                    e.validationResult.errors.joinToString("; ") { "${it.field}: ${it.message}" }
                )
            } catch (e: Exception) {
                call.handleException(e, "creating person")
            }
        }

        // PUT /api/persons/{id} - Update person
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respondValidationError("Missing person ID")
                val uuid = uuidFrom(id)
                val person = call.receive<Person>()
                val updatedPerson = personService.updatePerson(uuid, person)
                if (updatedPerson != null) {
                    call.respondSuccess(updatedPerson)
                } else {
                    call.respondNotFound("Person")
                }
            } catch (e: ValidationException) {
                call.respondValidationError(
                    "Person validation failed",
                    e.validationResult.errors.joinToString("; ") { "${it.field}: ${it.message}" }
                )
            } catch (e: Exception) {
                call.handleException(e, "updating person")
            }
        }

        // DELETE /api/persons/{id} - Delete person
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respondValidationError("Missing person ID")
                val uuid = uuidFrom(id)
                val deleted = personService.deletePerson(uuid)
                if (deleted) {
                    call.respondNoContent()
                } else {
                    call.respondNotFound("Person")
                }
            } catch (e: Exception) {
                call.handleException(e, "deleting person")
            }
        }
    }
}
