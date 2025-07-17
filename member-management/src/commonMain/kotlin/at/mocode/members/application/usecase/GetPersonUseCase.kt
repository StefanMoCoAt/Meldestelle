package at.mocode.members.application.usecase

import at.mocode.dto.base.ApiResponse
import at.mocode.dto.base.ErrorDto
import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import com.benasher44.uuid.Uuid

/**
 * Use case for retrieving person information from the member management context.
 *
 * This use case handles the business logic for person retrieval including:
 * - Finding persons by ID or OEPS Satznummer
 * - Searching persons by name
 * - Retrieving persons by club membership
 * - Listing active persons with pagination
 */
class GetPersonUseCase(
    private val personRepository: PersonRepository
) {

    /**
     * Request data for getting a person by ID.
     */
    data class GetPersonByIdRequest(
        val personId: Uuid
    )

    /**
     * Request data for getting a person by OEPS Satznummer.
     */
    data class GetPersonByOepsSatzNrRequest(
        val oepsSatzNr: String
    )

    /**
     * Request data for searching persons by name.
     */
    data class SearchPersonsByNameRequest(
        val searchTerm: String,
        val limit: Int = 50
    )

    /**
     * Request data for getting persons by club.
     */
    data class GetPersonsByClubRequest(
        val vereinId: Uuid
    )

    /**
     * Request data for listing active persons.
     */
    data class ListActivePersonsRequest(
        val limit: Int = 50,
        val offset: Int = 0
    )

    /**
     * Response data for person retrieval operations.
     */
    data class GetPersonResponse(
        val person: DomPerson
    )

    /**
     * Response data for person list operations.
     */
    data class GetPersonsResponse(
        val persons: List<DomPerson>,
        val total: Long? = null
    )

    /**
     * Gets a person by their unique ID.
     */
    suspend fun getById(request: GetPersonByIdRequest): ApiResponse<GetPersonResponse> {
        return try {
            val person = personRepository.findById(request.personId)
            if (person != null) {
                ApiResponse(
                    success = true,
                    data = GetPersonResponse(person)
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "PERSON_NOT_FOUND",
                        message = "Person with ID ${request.personId} not found"
                    )
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while retrieving the person: ${e.message}"
                )
            )
        }
    }

    /**
     * Gets a person by their OEPS Satznummer.
     */
    suspend fun getByOepsSatzNr(request: GetPersonByOepsSatzNrRequest): ApiResponse<GetPersonResponse> {
        return try {
            if (request.oepsSatzNr.length != 6) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "INVALID_OEPS_SATZNR",
                        message = "OEPS Satznummer must be exactly 6 digits"
                    )
                )
            }

            val person = personRepository.findByOepsSatzNr(request.oepsSatzNr)
            if (person != null) {
                ApiResponse(
                    success = true,
                    data = GetPersonResponse(person)
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "PERSON_NOT_FOUND",
                        message = "Person with OEPS Satznummer ${request.oepsSatzNr} not found"
                    )
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while retrieving the person: ${e.message}"
                )
            )
        }
    }

    /**
     * Searches persons by name (first name or last name).
     */
    suspend fun searchByName(request: SearchPersonsByNameRequest): ApiResponse<GetPersonsResponse> {
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

            val persons = personRepository.findByName(request.searchTerm, request.limit)
            ApiResponse(
                success = true,
                data = GetPersonsResponse(persons)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while searching persons: ${e.message}"
                )
            )
        }
    }

    /**
     * Gets all persons belonging to a specific club.
     */
    suspend fun getByClub(request: GetPersonsByClubRequest): ApiResponse<GetPersonsResponse> {
        return try {
            val persons = personRepository.findByStammVereinId(request.vereinId)
            ApiResponse(
                success = true,
                data = GetPersonsResponse(persons)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while retrieving club members: ${e.message}"
                )
            )
        }
    }

    /**
     * Lists active persons with pagination.
     */
    suspend fun listActive(request: ListActivePersonsRequest): ApiResponse<GetPersonsResponse> {
        return try {
            val persons = personRepository.findAllActive(request.limit, request.offset)
            val total = if (request.offset == 0) personRepository.countActive() else null

            ApiResponse(
                success = true,
                data = GetPersonsResponse(persons, total)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while listing active persons: ${e.message}"
                )
            )
        }
    }
}
