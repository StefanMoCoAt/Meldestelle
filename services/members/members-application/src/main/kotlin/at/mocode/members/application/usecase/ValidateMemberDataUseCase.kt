@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.members.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.members.domain.repository.MemberRepository
import kotlin.uuid.Uuid

/**
 * Use case for validating member data.
 *
 * This use case handles the business logic for validating
 * member data such as email and membership number uniqueness.
 */
class ValidateMemberDataUseCase(
    private val memberRepository: MemberRepository
) {

    /**
     * Request data for validating email uniqueness.
     */
    data class ValidateEmailRequest(
        val email: String,
        val excludeMemberId: Uuid? = null
    )

    /**
     * Request data for validating membership number uniqueness.
     */
    data class ValidateMembershipNumberRequest(
        val membershipNumber: String,
        val excludeMemberId: Uuid? = null
    )

    /**
     * Response data for validation results.
     */
    data class ValidationResponse(
        val isValid: Boolean,
        val exists: Boolean,
        val message: String
    )

    /**
     * Validates if an email address is unique.
     *
     * @param request The request containing email and optional member ID to exclude
     * @return ApiResponse with validation result
     */
    suspend fun validateEmail(request: ValidateEmailRequest): ApiResponse<ValidationResponse> {
        return try {
            // Basic email format validation
            if (request.email.isBlank()) {
                return ApiResponse(
                    success = true,
                    data = ValidationResponse(
                        isValid = false,
                        exists = false,
                        message = "Email is required"
                    )
                )
            }

            if (!isValidEmailFormat(request.email)) {
                return ApiResponse(
                    success = true,
                    data = ValidationResponse(
                        isValid = false,
                        exists = false,
                        message = "Email format is invalid"
                    )
                )
            }

            val exists = memberRepository.existsByEmail(request.email, request.excludeMemberId)

            ApiResponse(
                success = true,
                data = ValidationResponse(
                    isValid = !exists,
                    exists = exists,
                    message = if (exists) "Email already exists" else "Email is available"
                )
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to validate email: ${e.message}"
                )
            )
        }
    }

    /**
     * Validates if a membership number is unique.
     *
     * @param request The request containing membership number and optional member ID to exclude
     * @return ApiResponse with validation result
     */
    suspend fun validateMembershipNumber(request: ValidateMembershipNumberRequest): ApiResponse<ValidationResponse> {
        return try {
            // Basic membership number validation
            if (request.membershipNumber.isBlank()) {
                return ApiResponse(
                    success = true,
                    data = ValidationResponse(
                        isValid = false,
                        exists = false,
                        message = "Membership number is required"
                    )
                )
            }

            val exists = memberRepository.existsByMembershipNumber(request.membershipNumber, request.excludeMemberId)

            ApiResponse(
                success = true,
                data = ValidationResponse(
                    isValid = !exists,
                    exists = exists,
                    message = if (exists) "Membership number already exists" else "Membership number is available"
                )
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to validate membership number: ${e.message}"
                )
            )
        }
    }

    /**
     * Basic email format validation.
     */
    private fun isValidEmailFormat(email: String): Boolean {
        return email.contains("@") &&
               email.contains(".") &&
               email.indexOf("@") > 0 &&
               email.lastIndexOf(".") > email.indexOf("@") &&
               email.length > 5
    }
}
