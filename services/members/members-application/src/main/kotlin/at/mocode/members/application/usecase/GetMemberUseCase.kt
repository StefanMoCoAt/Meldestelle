package at.mocode.members.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import com.benasher44.uuid.Uuid

/**
 * Use case for retrieving members.
 *
 * This use case handles the business logic for retrieving members
 * by various criteria.
 */
class GetMemberUseCase(
    private val memberRepository: MemberRepository
) {

    /**
     * Request data for getting a member by ID.
     */
    data class GetMemberRequest(
        val memberId: Uuid
    )

    /**
     * Response data containing the retrieved member.
     */
    data class GetMemberResponse(
        val member: Member
    )

    /**
     * Executes the get member use case.
     *
     * @param request The request containing member ID
     * @return ApiResponse with the member or error information
     */
    suspend fun execute(request: GetMemberRequest): ApiResponse<GetMemberResponse> {
        return try {
            val member = memberRepository.findById(request.memberId)

            if (member != null) {
                ApiResponse(
                    success = true,
                    data = GetMemberResponse(member)
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "MEMBER_NOT_FOUND",
                        message = "Member not found"
                    )
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to retrieve member: ${e.message}"
                )
            )
        }
    }

    /**
     * Gets a member by membership number.
     */
    suspend fun getByMembershipNumber(membershipNumber: String): ApiResponse<GetMemberResponse> {
        return try {
            val member = memberRepository.findByMembershipNumber(membershipNumber)

            if (member != null) {
                ApiResponse(
                    success = true,
                    data = GetMemberResponse(member)
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "MEMBER_NOT_FOUND",
                        message = "Member not found"
                    )
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to retrieve member: ${e.message}"
                )
            )
        }
    }

    /**
     * Gets a member by email address.
     */
    suspend fun getByEmail(email: String): ApiResponse<GetMemberResponse> {
        return try {
            val member = memberRepository.findByEmail(email)

            if (member != null) {
                ApiResponse(
                    success = true,
                    data = GetMemberResponse(member)
                )
            } else {
                ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "MEMBER_NOT_FOUND",
                        message = "Member not found"
                    )
                )
            }
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to retrieve member: ${e.message}"
                )
            )
        }
    }
}
