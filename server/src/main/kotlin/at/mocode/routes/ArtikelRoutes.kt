package at.mocode.routes

import at.mocode.dto.ArtikelDto
import at.mocode.dto.CreateArtikelDto
import at.mocode.dto.UpdateArtikelDto
import at.mocode.model.Artikel
import at.mocode.plugins.respondVersioned
import at.mocode.plugins.respondVersionedList
import at.mocode.repositories.ArtikelRepository
import at.mocode.services.ServiceLocator
import at.mocode.utils.ApiResponse
import at.mocode.utils.StructuredLogger
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.collections.mapOf

// Extension functions for converting between Model and DTO
private fun Artikel.toDto(): ArtikelDto = ArtikelDto(
    id = this.id,
    bezeichnung = this.bezeichnung,
    preis = this.preis,
    einheit = this.einheit,
    istVerbandsabgabe = this.istVerbandsabgabe,
    kategorie = this.kategorie,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

private fun CreateArtikelDto.toModel(): Artikel = Artikel(
    bezeichnung = this.bezeichnung,
    preis = this.preis,
    einheit = this.einheit,
    istVerbandsabgabe = this.istVerbandsabgabe,
    kategorie = this.kategorie
)

private fun UpdateArtikelDto.toModel(): Artikel = Artikel(
    bezeichnung = this.bezeichnung,
    preis = this.preis,
    einheit = this.einheit,
    istVerbandsabgabe = this.istVerbandsabgabe,
    kategorie = this.kategorie
)

fun Route.artikelRoutes() {
    val artikelRepository: ArtikelRepository = ServiceLocator.artikelRepository
    val log = StructuredLogger.getLogger("ArtikelRoutes")

    route("/artikel") {
        // GET /api/artikel - Get all articles
        get {
            val startTime = System.currentTimeMillis()
            log.logApiRequest("GET", "/api/artikel")

            try {
                val artikel = artikelRepository.findAll()
                val artikelDtos = artikel.map { it.toDto() }
                val duration = System.currentTimeMillis() - startTime

                log.logApiRequest("GET", "/api/artikel", HttpStatusCode.OK.value, duration)
                log.info("Articles retrieved successfully", mapOf(
                    "operation" to "getAllArtikel",
                    "count" to artikel.size,
                    "duration_ms" to duration
                ))

                call.respondVersionedList(HttpStatusCode.OK, artikelDtos)
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                log.logApiRequest("GET", "/api/artikel", HttpStatusCode.InternalServerError.value, duration)
                log.error("Failed to retrieve articles", e, mapOf(
                    "operation" to "getAllArtikel",
                    "error_type" to e::class.simpleName,
                    "duration_ms" to duration
                ))

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = e.message ?: "An error occurred while fetching articles"
                    )
                )
            }
        }

        // GET /api/artikel/{id} - Get article by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "MISSING_PARAMETER",
                        message = "Missing artikel ID"
                    )
                )
                val uuid = uuidFrom(id)
                val artikel = artikelRepository.findById(uuid)
                if (artikel != null) {
                    call.respondVersioned(HttpStatusCode.OK, artikel.toDto())
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Nothing>(
                            success = false,
                            error = "NOT_FOUND",
                            message = "Artikel not found"
                        )
                    )
                }
            } catch (_: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INVALID_FORMAT",
                        message = "Invalid UUID format"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = e.message ?: "An error occurred while fetching article"
                    )
                )
            }
        }

        // GET /api/artikel/search?q={query} - Search articles
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "MISSING_PARAMETER",
                        message = "Missing search query parameter 'q'"
                    )
                )
                val artikel = artikelRepository.search(query)
                val artikelDtos = artikel.map { it.toDto() }
                call.respondVersionedList(HttpStatusCode.OK, artikelDtos)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = e.message ?: "An error occurred while searching articles"
                    )
                )
            }
        }

        // GET /api/artikel/verbandsabgabe/{istVerbandsabgabe} - Get articles by association fee status
        get("/verbandsabgabe/{istVerbandsabgabe}") {
            try {
                val istVerbandsabgabe = call.parameters["istVerbandsabgabe"]?.toBoolean() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "MISSING_PARAMETER",
                        message = "Missing or invalid verbandsabgabe parameter"
                    )
                )
                val artikel = artikelRepository.findByVerbandsabgabe(istVerbandsabgabe)
                val artikelDtos = artikel.map { it.toDto() }
                call.respondVersionedList(HttpStatusCode.OK, artikelDtos)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = e.message ?: "An error occurred while fetching articles by verbandsabgabe"
                    )
                )
            }
        }

        // POST /api/artikel - Create new article
        post {
            try {
                val createArtikelDto = call.receive<CreateArtikelDto>()
                val artikel = createArtikelDto.toModel()
                val createdArtikel = artikelRepository.create(artikel)
                call.respondVersioned(HttpStatusCode.Created, createdArtikel.toDto())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INVALID_INPUT",
                        message = e.message ?: "Invalid input for creating article"
                    )
                )
            }
        }

        // PUT /api/artikel/{id} - Update article
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "MISSING_PARAMETER",
                        message = "Missing artikel ID"
                    )
                )
                val uuid = uuidFrom(id)
                val updateArtikelDto = call.receive<UpdateArtikelDto>()
                val artikel = updateArtikelDto.toModel()
                val updatedArtikel = artikelRepository.update(uuid, artikel)
                if (updatedArtikel != null) {
                    call.respondVersioned(HttpStatusCode.OK, updatedArtikel.toDto())
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Nothing>(
                            success = false,
                            error = "NOT_FOUND",
                            message = "Artikel not found"
                        )
                    )
                }
            } catch (_: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INVALID_FORMAT",
                        message = "Invalid UUID format"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INVALID_INPUT",
                        message = e.message ?: "Invalid input for updating article"
                    )
                )
            }
        }

        // DELETE /api/artikel/{id} - Delete article
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "MISSING_PARAMETER",
                        message = "Missing artikel ID"
                    )
                )
                val uuid = uuidFrom(id)
                val deleted = artikelRepository.delete(uuid)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<Nothing>(
                            success = false,
                            error = "NOT_FOUND",
                            message = "Artikel not found"
                        )
                    )
                }
            } catch (_: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INVALID_FORMAT",
                        message = "Invalid UUID format"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = e.message ?: "An error occurred while deleting article"
                    )
                )
            }
        }
    }
}
