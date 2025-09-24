package at.mocode.events.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.events.domain.repository.VeranstaltungRepository
import com.benasher44.uuid.Uuid

/**
 * Use case for deleting events (Veranstaltung).
 *
 * This use case handles the business logic for deleting events,
 * including validation and cleanup.
 */
class DeleteVeranstaltungUseCase(
    private val veranstaltungRepository: VeranstaltungRepository
) {

    /**
     * Request data for deleting an event.
     */
    data class DeleteVeranstaltungRequest(
        val veranstaltungId: Uuid,
        val forceDelete: Boolean = false
    )

    /**
     * Response data for successful deletion.
     */
    data class DeleteVeranstaltungResponse(
        val deleted: Boolean,
        val message: String
    )

    /**
     * Executes the delete event use case.
     *
     * @param request The request containing the event ID to delete
     * @return ApiResponse with deletion result or error information
     */
    suspend fun execute(request: DeleteVeranstaltungRequest): ApiResponse<DeleteVeranstaltungResponse> {
        return try {
            // Check if event exists
            val existingVeranstaltung = veranstaltungRepository.findById(request.veranstaltungId)
            if (existingVeranstaltung == null) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "NOT_FOUND",
                        message = "Event not found"
                    )
                )
            }

            // Check if event can be safely deleted
            if (!request.forceDelete) {
                // In a real implementation, you might check for:
                // - Active registrations
                // - Related competitions
                // - Financial transactions
                // For now, we'll allow deletion if the event is not active or is in the future

                if (existingVeranstaltung.istAktiv) {
                    return ApiResponse(
                        success = false,
                        error = ErrorDto(
                            code = "CANNOT_DELETE_ACTIVE_EVENT",
                            message = "Cannot delete active event. Use forceDelete=true to override.",
                            details = mapOf(
                                "eventId" to request.veranstaltungId.toString(),
                                "eventName" to existingVeranstaltung.name
                            )
                        )
                    )
                }
            }

            // Perform the deletion
            val deleted = veranstaltungRepository.delete(request.veranstaltungId)

            if (deleted) {
                ApiResponse(
                    success = true,
                    data = DeleteVeranstaltungResponse(
                        deleted = true,
                        message = "Event '${existingVeranstaltung.name}' has been successfully deleted"
                    )
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "DELETE_FAILED",
                        message = "Failed to delete event from database"
                    )
                )
            }

        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to delete event: ${e.message}"
                )
            )
        }
    }
}
