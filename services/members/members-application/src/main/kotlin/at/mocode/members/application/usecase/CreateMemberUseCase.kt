package at.mocode.members.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import at.mocode.members.domain.events.MemberCreatedEvent
import at.mocode.infrastructure.messaging.client.EventPublisher
import at.mocode.core.domain.model.ValidationResult
import at.mocode.core.domain.model.ValidationError
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

/**
 * Use case for creating new members.
 *
 * This use case handles the business logic for creating members,
 * including validation and persistence.
 */
class CreateMemberUseCase(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher
) {

    /**
     * Request data for creating a new member.
     */
    data class CreateMemberRequest(
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
     * Response data containing the created member.
     */
    data class CreateMemberResponse(
        val member: Member
    )

    /**
     * Executes the create member use case.
     *
     * @param request The request containing member data
     * @return ApiResponse with the created member or error information
     */
    suspend fun execute(request: CreateMemberRequest): ApiResponse<CreateMemberResponse> {
        return try {
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

            // Check for duplicate membership number
            if (memberRepository.existsByMembershipNumber(request.membershipNumber)) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "DUPLICATE_MEMBERSHIP_NUMBER",
                        message = "Membership number already exists"
                    )
                )
            }

            // Check for duplicate email
            if (memberRepository.existsByEmail(request.email)) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "DUPLICATE_EMAIL",
                        message = "Email address already exists"
                    )
                )
            }

            // Create the domain object
            val member = Member(
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
                emergencyContact = request.emergencyContact?.trim(),
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )

            // Validate the domain object
            val domainValidationErrors = member.validate()
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

            // Save the member
            val savedMember = memberRepository.save(member)

            // Publish member created event
            try {
                val event = MemberCreatedEvent(
                    eventId = uuid4().toString(),
                    memberId = savedMember.memberId,
                    timestamp = Clock.System.now(),
                    firstName = savedMember.firstName,
                    lastName = savedMember.lastName,
                    email = savedMember.email,
                    membershipNumber = savedMember.membershipNumber,
                    membershipStartDate = savedMember.membershipStartDate,
                    isActive = savedMember.isActive
                )
                eventPublisher.publishEvent("member-events", savedMember.memberId.toString(), event)
            } catch (e: Exception) {
                // Log the error but don't fail the operation
                // In a production system, you might want to use a dead letter queue or retry mechanism
                println("Failed to publish member created event: ${e.message}")
            }

            ApiResponse(
                success = true,
                data = CreateMemberResponse(savedMember)
            )

        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to create member: ${e.message}"
                )
            )
        }
    }

    /**
     * Validates the create member request.
     */
    private fun validateRequest(request: CreateMemberRequest): ValidationResult {
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
