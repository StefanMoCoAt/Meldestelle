package at.mocode.members.application.usecase

import at.mocode.dto.base.ApiResponse
import at.mocode.dto.base.ErrorDto
import at.mocode.members.domain.model.DomVerein
import at.mocode.members.domain.repository.VereinRepository
import com.benasher44.uuid.Uuid

/**
 * Use case for retrieving club/association information from the member management context.
 *
 * This use case handles the business logic for club retrieval including:
 * - Finding clubs by ID or OEPS Vereinsnummer
 * - Searching clubs by name
 * - Retrieving clubs by location or geographic region
 * - Listing active clubs with pagination
 */
class GetVereinUseCase(
    private val vereinRepository: VereinRepository
) {

    /**
     * Request data for getting a club by ID.
     */
    data class GetVereinByIdRequest(
        val vereinId: Uuid
    )

    /**
     * Request data for getting a club by OEPS Vereinsnummer.
     */
    data class GetVereinByOepsVereinsNrRequest(
        val oepsVereinsNr: String
    )

    /**
     * Request data for searching clubs by name.
     */
    data class SearchVereinsByNameRequest(
        val searchTerm: String,
        val limit: Int = 50
    )

    /**
     * Request data for getting clubs by Bundesland.
     */
    data class GetVereineByBundeslandRequest(
        val bundeslandId: Uuid
    )

    /**
     * Request data for getting clubs by country.
     */
    data class GetVereineByLandRequest(
        val landId: Uuid
    )

    /**
     * Request data for searching clubs by location.
     */
    data class SearchVereineByLocationRequest(
        val searchTerm: String,
        val limit: Int = 50
    )

    /**
     * Request data for listing active clubs.
     */
    data class ListActiveVereineRequest(
        val limit: Int = 50,
        val offset: Int = 0
    )

    /**
     * Response data for club retrieval operations.
     */
    data class GetVereinResponse(
        val verein: DomVerein
    )

    /**
     * Response data for club list operations.
     */
    data class GetVereineResponse(
        val vereine: List<DomVerein>,
        val total: Long? = null
    )

    /**
     * Gets a club by its unique ID.
     */
    suspend fun getById(request: GetVereinByIdRequest): ApiResponse<GetVereinResponse> {
        return try {
            val verein = vereinRepository.findById(request.vereinId)
            if (verein != null) {
                ApiResponse(
                    success = true,
                    data = GetVereinResponse(verein)
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "VEREIN_NOT_FOUND",
                        message = "Club with ID ${request.vereinId} not found"
                    )
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while retrieving the club: ${e.message}"
                )
            )
        }
    }

    /**
     * Gets a club by its OEPS Vereinsnummer.
     */
    suspend fun getByOepsVereinsNr(request: GetVereinByOepsVereinsNrRequest): ApiResponse<GetVereinResponse> {
        return try {
            if (request.oepsVereinsNr.length != 4) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "INVALID_OEPS_VEREINSNR",
                        message = "OEPS Vereinsnummer must be exactly 4 digits"
                    )
                )
            }

            val verein = vereinRepository.findByOepsVereinsNr(request.oepsVereinsNr)
            if (verein != null) {
                ApiResponse(
                    success = true,
                    data = GetVereinResponse(verein)
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "VEREIN_NOT_FOUND",
                        message = "Club with OEPS Vereinsnummer ${request.oepsVereinsNr} not found"
                    )
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while retrieving the club: ${e.message}"
                )
            )
        }
    }

    /**
     * Searches clubs by name or abbreviation.
     */
    suspend fun searchByName(request: SearchVereinsByNameRequest): ApiResponse<GetVereineResponse> {
        return try {
            if (request.searchTerm.isBlank()) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "INVALID_SEARCH_TERM",
                        message = "Search term cannot be empty"
                    )
                )
            }

            val vereine = vereinRepository.findByName(request.searchTerm, request.limit)
            ApiResponse(
                success = true,
                data = GetVereineResponse(vereine)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while searching clubs: ${e.message}"
                )
            )
        }
    }

    /**
     * Gets all clubs in a specific Bundesland.
     */
    suspend fun getByBundesland(request: GetVereineByBundeslandRequest): ApiResponse<GetVereineResponse> {
        return try {
            val vereine = vereinRepository.findByBundeslandId(request.bundeslandId)
            ApiResponse(
                success = true,
                data = GetVereineResponse(vereine)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while retrieving clubs by Bundesland: ${e.message}"
                )
            )
        }
    }

    /**
     * Gets all clubs in a specific country.
     */
    suspend fun getByLand(request: GetVereineByLandRequest): ApiResponse<GetVereineResponse> {
        return try {
            val vereine = vereinRepository.findByLandId(request.landId)
            ApiResponse(
                success = true,
                data = GetVereineResponse(vereine)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while retrieving clubs by country: ${e.message}"
                )
            )
        }
    }

    /**
     * Searches clubs by location (city or postal code).
     */
    suspend fun searchByLocation(request: SearchVereineByLocationRequest): ApiResponse<GetVereineResponse> {
        return try {
            if (request.searchTerm.isBlank()) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "INVALID_SEARCH_TERM",
                        message = "Search term cannot be empty"
                    )
                )
            }

            val vereine = vereinRepository.findByLocation(request.searchTerm, request.limit)
            ApiResponse(
                success = true,
                data = GetVereineResponse(vereine)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while searching clubs by location: ${e.message}"
                )
            )
        }
    }

    /**
     * Lists active clubs with pagination.
     */
    suspend fun listActive(request: ListActiveVereineRequest): ApiResponse<GetVereineResponse> {
        return try {
            val vereine = vereinRepository.findAllActive(request.limit, request.offset)
            val total = if (request.offset == 0) vereinRepository.countActive() else null

            ApiResponse(
                success = true,
                data = GetVereineResponse(vereine, total)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while listing active clubs: ${e.message}"
                )
            )
        }
    }
}
