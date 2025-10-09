@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.members.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import at.mocode.core.domain.model.ValidationResult
import at.mocode.core.domain.model.ValidationError
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

/**
 * Use case for updating existing members.
 *
 * This use case handles the business logic for updating members,
 * including validation and persistence.
 */
class UpdateMemberUseCase(
    private val memberRepository: MemberRepository
) {

    /**
     * Request data for updating a member.
     */
    data class UpdateMemberRequest(
        val memberId: Uuid,
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String? = null,
        val dateOfBirth: LocalDate? = null,
        val membershipNumber: String,
        val membershipStartDate: LocalDate,
        val membershipEndDate: LocalDate? = null,
        val isActive: Boolean = true,
        val address: String? = null,
        val emergencyContact: String? = null
    )

    /**
     * Response data containing the updated member.
     */
    data class UpdateMemberResponse(
        val member: Member
    )

    /**
     * Executes the update member use case.
     *
     * @param request The request containing updated member data
     * @return ApiResponse with the updated member or error information
     */
    suspend fun execute(request: UpdateMemberRequest): ApiResponse<UpdateMemberResponse> {
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

            // Validate the request
            val validationResult = validateRequest(request)
            if (!validationResult.isValid()) {
                val errors = (validationResult as ValidationResult.Invalid).errors
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "VALIDATION_ERROR",
                        message = "Invalid input data",
                        details = errors.associate { it.field to it.message }
                    )
                )
            }

            // Check for duplicate membership number (excluding current member)
            if (memberRepository.existsByMembershipNumber(request.membershipNumber, request.memberId)) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "DUPLICATE_MEMBERSHIP_NUMBER",
                        message = "Membership number already exists"
                    )
                )
            }

            // Check for duplicate email (excluding current member)
            if (memberRepository.existsByEmail(request.email, request.memberId)) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "DUPLICATE_EMAIL",
                        message = "Email address already exists"
                    )
                )
            }

            // Update the member
            val updatedMember = existingMember.copy(
                firstName = request.firstName.trim(),
                lastName = request.lastName.trim(),
                email = request.email.trim().lowercase(),
                phone = request.phone?.trim(),
                dateOfBirth = request.dateOfBirth,
                membershipNumber = request.membershipNumber.trim(),
                membershipStartDate = request.membershipStartDate,
                membershipEndDate = request.membershipEndDate,
                isActive = request.isActive,
                address = request.address?.trim(),
                emergencyContact = request.emergencyContact?.trim()
            ).withUpdatedTimestamp()

            // Validate the domain object
            val domainValidationErrors = updatedMember.validate()
            if (domainValidationErrors.isNotEmpty()) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "DOMAIN_VALIDATION_ERROR",
                        message = "Domain validation failed",
                        details = domainValidationErrors.mapIndexed { index, error ->
                            "error_$index" to error
                        }.toMap()
                    )
                )
            }

            // Save the updated member
            val savedMember = memberRepository.save(updatedMember)

            ApiResponse(
                success = true,
                data = UpdateMemberResponse(savedMember)
            )

        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to update member: ${e.message}"
                )
            )
        }
    }

    /**
     * Validates the update member request.
     */
    private fun validateRequest(request: UpdateMemberRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate first name
        if (request.firstName.isBlank()) {
            errors.add(ValidationError("firstName", "First name is required"))
        } else if (request.firstName.length > 100) {
            errors.add(ValidationError("firstName", "First name must not exceed 100 characters"))
        }

        // Validate last name
        if (request.lastName.isBlank()) {
            errors.add(ValidationError("lastName", "Last name is required"))
        } else if (request.lastName.length > 100) {
            errors.add(ValidationError("lastName", "Last name must not exceed 100 characters"))
        }

        // Validate email
        if (request.email.isBlank()) {
            errors.add(ValidationError("email", "Email is required"))
        } else if (!isValidEmail(request.email)) {
            errors.add(ValidationError("email", "Email format is invalid"))
        } else if (request.email.length > 255) {
            errors.add(ValidationError("email", "Email must not exceed 255 characters"))
        }

        // Validate membership number
        if (request.membershipNumber.isBlank()) {
            errors.add(ValidationError("membershipNumber", "Membership number is required"))
        } else if (request.membershipNumber.length > 50) {
            errors.add(ValidationError("membershipNumber", "Membership number must not exceed 50 characters"))
        }

        // Validate membership dates
        request.membershipEndDate?.let { endDate ->
            if (endDate < request.membershipStartDate) {
                errors.add(ValidationError("membershipEndDate", "Membership end date cannot be before start date"))
            }
        }

        // Validate phone
        request.phone?.let { phone ->
            if (phone.length > 50) {
                errors.add(ValidationError("phone", "Phone number must not exceed 50 characters"))
            }
        }

        // Validate address
        request.address?.let { address ->
            if (address.length > 500) {
                errors.add(ValidationError("address", "Address must not exceed 500 characters"))
            }
        }

        // Validate emergency contact
        request.emergencyContact?.let { contact ->
            if (contact.length > 255) {
                errors.add(ValidationError("emergencyContact", "Emergency contact must not exceed 255 characters"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".")
    }
}
