package at.mocode.routes

import at.mocode.model.ArtikelRepository
import at.mocode.model.PostgresArtikelRepository
import at.mocode.shared.model.Artikel
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.artikelRoutes() {
    val artikelRepository: ArtikelRepository = PostgresArtikelRepository()

    route("/api/artikel") {
        // GET /api/artikel - Get all articles
        get {
            try {
                val artikel = artikelRepository.findAll()
                call.respond(HttpStatusCode.OK, artikel)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/artikel/{id} - Get article by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing artikel ID")
                )
                val uuid = uuidFrom(id)
                val artikel = artikelRepository.findById(uuid)
                if (artikel != null) {
                    call.respond(HttpStatusCode.OK, artikel)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Artikel not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/artikel/search?q={query} - Search articles
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing search query parameter 'q'")
                )
                val artikel = artikelRepository.search(query)
                call.respond(HttpStatusCode.OK, artikel)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/artikel/verbandsabgabe/{istVerbandsabgabe} - Get articles by association fee status
        get("/verbandsabgabe/{istVerbandsabgabe}") {
            try {
                val istVerbandsabgabe = call.parameters["istVerbandsabgabe"]?.toBoolean() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing or invalid verbandsabgabe parameter")
                )
                val artikel = artikelRepository.findByVerbandsabgabe(istVerbandsabgabe)
                call.respond(HttpStatusCode.OK, artikel)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // POST /api/artikel - Create new article
        post {
            try {
                val artikel = call.receive<Artikel>()
                val createdArtikel = artikelRepository.create(artikel)
                call.respond(HttpStatusCode.Created, createdArtikel)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // PUT /api/artikel/{id} - Update article
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing artikel ID")
                )
                val uuid = uuidFrom(id)
                val artikel = call.receive<Artikel>()
                val updatedArtikel = artikelRepository.update(uuid, artikel)
                if (updatedArtikel != null) {
                    call.respond(HttpStatusCode.OK, updatedArtikel)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Artikel not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // DELETE /api/artikel/{id} - Delete article
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Missing artikel ID")
                )
                val uuid = uuidFrom(id)
                val deleted = artikelRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Artikel not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID format"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
