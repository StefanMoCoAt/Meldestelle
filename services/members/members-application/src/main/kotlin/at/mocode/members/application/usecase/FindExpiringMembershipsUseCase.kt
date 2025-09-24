package at.mocode.members.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository

/**
 * Use case for finding members with expiring memberships.
 *
 * This use case handles the business logic for finding members
 * whose memberships are expiring within a specified number of days.
 */
class FindExpiringMembershipsUseCase(
    private val memberRepository: MemberRepository
) {

    /**
     * Request data for finding expiring memberships.
     */
    data class FindExpiringMembershipsRequest(
        val daysAhead: Int = 30
    )

    /**
     * Response data containing the list of members with expiring memberships.
     */
    data class FindExpiringMembershipsResponse(
        val members: List<Member>,
        val count: Int
    )

    /**
     * Executes the find expiring memberships use case.
     *
     * @param request The request containing the number of days to look ahead
     * @return ApiResponse with the list of members or error information
     */
    suspend fun execute(request: FindExpiringMembershipsRequest): ApiResponse<FindExpiringMembershipsResponse> {
        return try {
            // Validate input
            if (request.daysAhead < 0) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "INVALID_DAYS_AHEAD",
                        message = "Days ahead must be a positive number"
                    )
                )
            }

            val members = memberRepository.findMembersWithExpiringMembership(request.daysAhead)

            ApiResponse(
                success = true,
                data = FindExpiringMembershipsResponse(
                    members = members,
                    count = members.size
                )
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to find expiring memberships: ${e.message}"
                )
            )
        }
    }
}
