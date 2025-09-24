package at.mocode.members.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.members.domain.repository.MemberRepository
import com.benasher44.uuid.Uuid

/**
 * Use case for deleting members.
 *
 * This use case handles the business logic for deleting members
 * from the system.
 */
class DeleteMemberUseCase(
    private val memberRepository: MemberRepository
) {

    /**
     * Request data for deleting a member.
     */
    data class DeleteMemberRequest(
        val memberId: Uuid
    )

    /**
     * Response data for delete operation.
     */
    data class DeleteMemberResponse(
        val success: Boolean,
        val message: String
    )

    /**
     * Executes the delete member use case.
     *
     * @param request The request containing member ID to delete
     * @return ApiResponse with the result or error information
     */
    suspend fun execute(request: DeleteMemberRequest): ApiResponse<DeleteMemberResponse> {
        return try {
            // Check if member exists
            val existingMember = memberRepository.findById(request.memberId)
            if (existingMember == null) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "MEMBER_NOT_FOUND",
                        message = "Member not found"
                    )
                )
            }

            // Delete the member
            val deleted = memberRepository.delete(request.memberId)

            if (deleted) {
                ApiResponse(
                    success = true,
                    data = DeleteMemberResponse(
                        success = true,
                        message = "Member deleted successfully"
                    )
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "DELETE_FAILED",
                        message = "Failed to delete member"
                    )
                )
            }

        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to delete member: ${e.message}"
                )
            )
        }
    }
}
