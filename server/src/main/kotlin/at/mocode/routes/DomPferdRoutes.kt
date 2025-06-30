package at.mocode.routes

import at.mocode.repositories.DomPferdRepository
import at.mocode.repositories.PostgresDomPferdRepository
import at.mocode.model.domaene.DomPferd
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.domPferdRoutes() {
    val domPferdRepository: DomPferdRepository = PostgresDomPferdRepository()

    route("/api/horses") {
        // GET /api/horses - Get all horses
        get {
            try {
                val horses = domPferdRepository.findAll()
                call.respond(HttpStatusCode.OK, horses)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/{id} - Get horse by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing horse ID")
                )
                val uuid = uuidFrom(id)
                val horse = domPferdRepository.findById(uuid)
                if (horse != null) {
                    call.respond(HttpStatusCode.OK, horse)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Horse not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/oeps/{oepsSatzNr} - Get horse by OEPS number
        get("/oeps/{oepsSatzNr}") {
            try {
                val oepsSatzNr = call.parameters["oepsSatzNr"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing OEPS Satz number")
                )
                val horse = domPferdRepository.findByOepsSatzNr(oepsSatzNr)
                if (horse != null) {
                    call.respond(HttpStatusCode.OK, horse)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Horse not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/lebensnummer/{lebensnummer} - Get horse by life number
        get("/lebensnummer/{lebensnummer}") {
            try {
                val lebensnummer = call.parameters["lebensnummer"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing Lebensnummer")
                )
                val horse = domPferdRepository.findByLebensnummer(lebensnummer)
                if (horse != null) {
                    call.respond(HttpStatusCode.OK, horse)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Horse not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/search?q={query} - Search horses
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val horses = domPferdRepository.search(query)
                call.respond(HttpStatusCode.OK, horses)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/name/{name} - Get horses by name
        get("/name/{name}") {
            try {
                val name = call.parameters["name"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing horse name")
                )
                val horses = domPferdRepository.findByName(name)
                call.respond(HttpStatusCode.OK, horses)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/owner/{ownerId} - Get horses by owner ID
        get("/owner/{ownerId}") {
            try {
                val ownerId = call.parameters["ownerId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing owner ID")
                )
                val uuid = uuidFrom(ownerId)
                val horses = domPferdRepository.findByBesitzerId(uuid)
                call.respond(HttpStatusCode.OK, horses)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/responsible/{personId} - Get horses by responsible person ID
        get("/responsible/{personId}") {
            try {
                val personId = call.parameters["personId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing person ID")
                )
                val uuid = uuidFrom(personId)
                val horses = domPferdRepository.findByVerantwortlichePersonId(uuid)
                call.respond(HttpStatusCode.OK, horses)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/club/{clubId} - Get horses by home club ID
        get("/club/{clubId}") {
            try {
                val clubId = call.parameters["clubId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing club ID")
                )
                val uuid = uuidFrom(clubId)
                val horses = domPferdRepository.findByHeimatVereinId(uuid)
                call.respond(HttpStatusCode.OK, horses)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/breed/{breed} - Get horses by breed
        get("/breed/{breed}") {
            try {
                val breed = call.parameters["breed"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing breed")
                )
                val horses = domPferdRepository.findByRasse(breed)
                call.respond(HttpStatusCode.OK, horses)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/birth-year/{year} - Get horses by birth year
        get("/birth-year/{year}") {
            try {
                val yearStr = call.parameters["year"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing birth year")
                )
                val year = yearStr.toIntOrNull() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid birth year format")
                )
                val horses = domPferdRepository.findByGeburtsjahr(year)
                call.respond(HttpStatusCode.OK, horses)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/horses/active - Get active horses only
        get("/active") {
            try {
                val horses = domPferdRepository.findActiveHorses()
                call.respond(HttpStatusCode.OK, horses)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/horses - Create a new horse
        post {
            try {
                val horse = call.receive<DomPferd>()
                val createdHorse = domPferdRepository.create(horse)
                call.respond(HttpStatusCode.Created, createdHorse)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/horses/{id} - Update horse
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing horse ID")
                )
                val uuid = uuidFrom(id)
                val horse = call.receive<DomPferd>()
                val updatedHorse = domPferdRepository.update(uuid, horse)
                if (updatedHorse != null) {
                    call.respond(HttpStatusCode.OK, updatedHorse)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Horse not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/horses/{id} - Delete horse
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing horse ID")
                )
                val uuid = uuidFrom(id)
                val deleted = domPferdRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Horse not found"))
                }
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
