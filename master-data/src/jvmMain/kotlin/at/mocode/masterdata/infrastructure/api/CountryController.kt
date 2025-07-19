package at.mocode.masterdata.infrastructure.api

import at.mocode.dto.base.BaseDto
import at.mocode.dto.base.ApiResponse
import at.mocode.masterdata.application.usecase.CreateCountryUseCase
import at.mocode.masterdata.application.usecase.GetCountryUseCase
import at.mocode.masterdata.domain.model.LandDefinition
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * REST API controller for country management operations.
 *
 * This controller provides HTTP endpoints for the master-data context's
 * country functionality, following REST conventions and proper error handling.
 */
class CountryController(
    private val getCountryUseCase: GetCountryUseCase,
    private val createCountryUseCase: CreateCountryUseCase
) {

    /**
     * DTO for country API responses.
     */
    @Serializable
    data class CountryDto(
        val landId: String,
        val isoAlpha2Code: String,
        val isoAlpha3Code: String,
        val isoNumerischerCode: String? = null,
        val nameDeutsch: String,
        val nameEnglisch: String? = null,
        val wappenUrl: String? = null,
        val istEuMitglied: Boolean? = null,
        val istEwrMitglied: Boolean? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null,
        val createdAt: String,
        val updatedAt: String
    )

    /**
     * DTO for creating a new country.
     */
    @Serializable
    data class CreateCountryDto(
        val isoAlpha2Code: String,
        val isoAlpha3Code: String,
        val isoNumerischerCode: String? = null,
        val nameDeutsch: String,
        val nameEnglisch: String? = null,
        val wappenUrl: String? = null,
        val istEuMitglied: Boolean? = null,
        val istEwrMitglied: Boolean? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * DTO for updating an existing country.
     */
    @Serializable
    data class UpdateCountryDto(
        val isoAlpha2Code: String,
        val isoAlpha3Code: String,
        val isoNumerischerCode: String? = null,
        val nameDeutsch: String,
        val nameEnglisch: String? = null,
        val wappenUrl: String? = null,
        val istEuMitglied: Boolean? = null,
        val istEwrMitglied: Boolean? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Configures the routing for country endpoints.
     */
    fun configureRouting(routing: Routing) {
        routing.route("/api/masterdata/countries") {

            // GET /api/masterdata/countries - Get all active countries
            get {
                try {
                    val orderBySortierung = call.request.queryParameters["orderBySortierung"]?.toBoolean() ?: true
                    val countries = getCountryUseCase.getAllActive(orderBySortierung)
                    val countryDtos = countries.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(countryDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<CountryDto>>("Failed to retrieve countries: ${e.message}"))
                }
            }

            // GET /api/masterdata/countries/{id} - Get country by ID
            get("/{id}") {
                try {
                    val countryId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<CountryDto>("Invalid country ID"))

                    val country = getCountryUseCase.getById(countryId)
                    if (country != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(country.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<CountryDto>("Country not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<CountryDto>("Failed to retrieve country: ${e.message}"))
                }
            }

            // GET /api/masterdata/countries/iso2/{code} - Get country by ISO Alpha-2 code
            get("/iso2/{code}") {
                try {
                    val isoCode = call.parameters["code"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<CountryDto>("ISO code is required"))

                    val country = getCountryUseCase.getByIsoAlpha2Code(isoCode)
                    if (country != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(country.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<CountryDto>("Country not found"))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<CountryDto>(e.message ?: "Invalid ISO code"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<CountryDto>("Failed to retrieve country: ${e.message}"))
                }
            }

            // GET /api/masterdata/countries/iso3/{code} - Get country by ISO Alpha-3 code
            get("/iso3/{code}") {
                try {
                    val isoCode = call.parameters["code"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<CountryDto>("ISO code is required"))

                    val country = getCountryUseCase.getByIsoAlpha3Code(isoCode)
                    if (country != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(country.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<CountryDto>("Country not found"))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<CountryDto>(e.message ?: "Invalid ISO code"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<CountryDto>("Failed to retrieve country: ${e.message}"))
                }
            }

            // GET /api/masterdata/countries/search - Search countries by name
            get("/search") {
                try {
                    val searchTerm = call.request.queryParameters["q"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<CountryDto>>("Search term 'q' is required"))

                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

                    val countries = getCountryUseCase.searchByName(searchTerm, limit)
                    val countryDtos = countries.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(countryDtos))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<CountryDto>>(e.message ?: "Invalid search parameters"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<CountryDto>>("Failed to search countries: ${e.message}"))
                }
            }

            // GET /api/masterdata/countries/eu - Get EU member countries
            get("/eu") {
                try {
                    val countries = getCountryUseCase.getEuMembers()
                    val countryDtos = countries.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(countryDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<CountryDto>>("Failed to retrieve EU countries: ${e.message}"))
                }
            }

            // GET /api/masterdata/countries/ewr - Get EWR member countries
            get("/ewr") {
                try {
                    val countries = getCountryUseCase.getEwrMembers()
                    val countryDtos = countries.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(countryDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<CountryDto>>("Failed to retrieve EWR countries: ${e.message}"))
                }
            }

            // POST /api/masterdata/countries - Create new country
            post {
                try {
                    val createDto = call.receive<CreateCountryDto>()
                    val request = CreateCountryUseCase.CreateCountryRequest(
                        isoAlpha2Code = createDto.isoAlpha2Code,
                        isoAlpha3Code = createDto.isoAlpha3Code,
                        isoNumerischerCode = createDto.isoNumerischerCode,
                        nameDeutsch = createDto.nameDeutsch,
                        nameEnglisch = createDto.nameEnglisch,
                        wappenUrl = createDto.wappenUrl,
                        istEuMitglied = createDto.istEuMitglied,
                        istEwrMitglied = createDto.istEwrMitglied,
                        istAktiv = createDto.istAktiv,
                        sortierReihenfolge = createDto.sortierReihenfolge
                    )

                    val result = createCountryUseCase.createCountry(request)
                    if (result.success) {
                        call.respond(HttpStatusCode.Created, ApiResponse.success(result.country!!.toDto()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.error<CountryDto>("Validation failed: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<CountryDto>("Failed to create country: ${e.message}"))
                }
            }

            // PUT /api/masterdata/countries/{id} - Update existing country
            put("/{id}") {
                try {
                    val countryId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.error<CountryDto>("Invalid country ID"))

                    val updateDto = call.receive<UpdateCountryDto>()
                    val request = CreateCountryUseCase.UpdateCountryRequest(
                        landId = countryId,
                        isoAlpha2Code = updateDto.isoAlpha2Code,
                        isoAlpha3Code = updateDto.isoAlpha3Code,
                        isoNumerischerCode = updateDto.isoNumerischerCode,
                        nameDeutsch = updateDto.nameDeutsch,
                        nameEnglisch = updateDto.nameEnglisch,
                        wappenUrl = updateDto.wappenUrl,
                        istEuMitglied = updateDto.istEuMitglied,
                        istEwrMitglied = updateDto.istEwrMitglied,
                        istAktiv = updateDto.istAktiv,
                        sortierReihenfolge = updateDto.sortierReihenfolge
                    )

                    val result = createCountryUseCase.updateCountry(request)
                    if (result.success) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(result.country!!.toDto()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.error<CountryDto>("Validation failed: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<CountryDto>("Failed to update country: ${e.message}"))
                }
            }

            // DELETE /api/masterdata/countries/{id} - Delete country
            delete("/{id}") {
                try {
                    val countryId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>("Invalid country ID"))

                    val result = createCountryUseCase.deleteCountry(countryId)
                    if (result.success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Country not found: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Unit>("Failed to delete country: ${e.message}"))
                }
            }
        }
    }

    /**
     * Extension function to convert LandDefinition domain object to CountryDto.
     */
    private fun LandDefinition.toDto(): CountryDto {
        return CountryDto(
            landId = this.landId.toString(),
            isoAlpha2Code = this.isoAlpha2Code,
            isoAlpha3Code = this.isoAlpha3Code,
            isoNumerischerCode = this.isoNumerischerCode,
            nameDeutsch = this.nameDeutsch,
            nameEnglisch = this.nameEnglisch,
            wappenUrl = this.wappenUrl,
            istEuMitglied = this.istEuMitglied,
            istEwrMitglied = this.istEwrMitglied,
            istAktiv = this.istAktiv,
            sortierReihenfolge = this.sortierReihenfolge,
            createdAt = this.createdAt.toString(),
            updatedAt = this.updatedAt.toString()
        )
    }
}
