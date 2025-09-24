package at.mocode.members.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import kotlinx.datetime.LocalDate

/**
 * Use case for finding members by date ranges.
 *
 * This use case handles the business logic for finding members
 * based on their membership start or end date ranges.
 */
class FindMembersByDateRangeUseCase(
    private val memberRepository: MemberRepository
) {

    /**
     * Request data for finding members by date range.
     */
    data class FindMembersByDateRangeRequest(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val dateType: DateRangeType
    )

    /**
     * Type of date range to search by.
     */
    enum class DateRangeType {
        MEMBERSHIP_START_DATE,
        MEMBERSHIP_END_DATE
    }

    /**
     * Response data containing the list of members within the date range.
     */
    data class FindMembersByDateRangeResponse(
        val members: List<Member>,
        val count: Int,
        val dateType: DateRangeType,
        val startDate: LocalDate,
        val endDate: LocalDate
    )

    /**
     * Executes the find members by date range use case.
     *
     * @param request The request containing the date range and type
     * @return ApiResponse with the list of members or error information
     */
    suspend fun execute(request: FindMembersByDateRangeRequest): ApiResponse<FindMembersByDateRangeResponse> {
        return try {
            // Validate input
            if (request.startDate > request.endDate) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "INVALID_DATE_RANGE",
                        message = "Start date cannot be after end date"
                    )
                )
            }

            val members = when (request.dateType) {
                DateRangeType.MEMBERSHIP_START_DATE ->
                    memberRepository.findByMembershipStartDateRange(request.startDate, request.endDate)
                DateRangeType.MEMBERSHIP_END_DATE ->
                    memberRepository.findByMembershipEndDateRange(request.startDate, request.endDate)
            }

            ApiResponse(
                success = true,
                data = FindMembersByDateRangeResponse(
                    members = members,
                    count = members.size,
                    dateType = request.dateType,
                    startDate = request.startDate,
                    endDate = request.endDate
                )
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to find members by date range: ${e.message}"
                )
            )
        }
    }
}
