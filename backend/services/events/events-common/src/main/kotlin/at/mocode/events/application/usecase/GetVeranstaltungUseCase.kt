@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.events.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.events.domain.model.Veranstaltung
import at.mocode.events.domain.repository.VeranstaltungRepository
import kotlin.uuid.Uuid

/**
 * Use case for retrieving events (Veranstaltung) by ID.
 *
 * This use case handles the business logic for fetching events
 * from the repository.
 */
class GetVeranstaltungUseCase(
    private val veranstaltungRepository: VeranstaltungRepository
) {

    /**
     * Request data for retrieving an event.
     */
    data class GetVeranstaltungRequest(
        val veranstaltungId: Uuid
    )

    /**
     * Response data containing the retrieved event.
     */
    data class GetVeranstaltungResponse(
        val veranstaltung: Veranstaltung
    )

    /**
     * Executes the get event use case.
     *
     * @param request The request containing the event ID
     * @return ApiResponse with the event or error information
     */
    suspend fun execute(request: GetVeranstaltungRequest): ApiResponse<GetVeranstaltungResponse> {
        return try {
            val veranstaltung = veranstaltungRepository.findById(request.veranstaltungId)

            if (veranstaltung != null) {
                ApiResponse(
                    success = true,
                    data = GetVeranstaltungResponse(veranstaltung)
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "NOT_FOUND",
                        message = "Event not found"
                    )
                )
            }

        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to retrieve event: ${e.message}"
                )
            )
        }
    }
}
