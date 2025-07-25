package at.mocode.members.api.rest

import at.mocode.core.domain.model.ApiResponse
import at.mocode.infrastructure.messaging.client.EventPublisher
import at.mocode.members.application.usecase.*
import at.mocode.members.domain.repository.MemberRepository
import com.benasher44.uuid.uuidFrom
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

/**
 * Simple no-op EventPublisher implementation for the controller.
 */
class NoOpEventPublisher : EventPublisher {
    override suspend fun publishEvent(topic: String, key: String?, event: Any) {
        // No-op implementation - events are not published in this simple version
    }

    override suspend fun publishEvents(topic: String, events: List<Pair<String?, Any>>) {
        // No-op implementation - events are not published in this simple version
    }
}

/**
 * REST API controller for member management operations.
 *
 * This controller provides HTTP endpoints for all member-related operations
 * including CRUD operations and member search functionality.
 */
@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Member management operations")
class MemberController(
    @Qualifier("memberRepositoryImpl") private val memberRepository: MemberRepository
) {

    // Simple no-op EventPublisher implementation for now
    private val eventPublisher = NoOpEventPublisher()

    private val createMemberUseCase = CreateMemberUseCase(memberRepository, eventPublisher)
    private val getMemberUseCase = GetMemberUseCase(memberRepository)
    private val updateMemberUseCase = UpdateMemberUseCase(memberRepository)
    private val deleteMemberUseCase = DeleteMemberUseCase(memberRepository)
    private val findExpiringMembershipsUseCase = FindExpiringMembershipsUseCase(memberRepository)
    private val findMembersByDateRangeUseCase = FindMembersByDateRangeUseCase(memberRepository)
    private val validateMemberDataUseCase = ValidateMemberDataUseCase(memberRepository)

    /**
     * Helper method to handle common response patterns for use case execution
     */
    private inline fun <T> handleUseCaseExecution(
        crossinline operation: suspend () -> ApiResponse<T>,
        successStatus: HttpStatus = HttpStatus.OK,
        crossinline extractData: (T) -> Any = { it as Any }
    ): ResponseEntity<ApiResponse<*>> {
        return try {
            val response = runBlocking { operation() }

            if (response.success && response.data != null) {
                ResponseEntity.status(successStatus)
                    .body(ApiResponse.success(extractData(response.data!!)))
            } else {
                val statusCode = when (response.error?.code) {
                    "MEMBER_NOT_FOUND" -> HttpStatus.NOT_FOUND
                    "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST
                    else -> HttpStatus.BAD_REQUEST
                }
                ResponseEntity.status(statusCode)
                    .body(ApiResponse.error<Any>(response.error?.message ?: "Operation failed"))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error<Any>("Invalid input format: ${e.message}"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Internal server error: ${e.message}"))
        }
    }

    /**
     * Helper method to handle repository operations with common error handling
     */
    private inline fun <T> handleRepositoryOperation(
        crossinline operation: () -> T,
        errorMessage: String = "Operation failed"
    ): ResponseEntity<ApiResponse<T>> {
        return try {
            val result = runBlocking { operation() }
            ResponseEntity.ok(ApiResponse.success(result))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<T>("$errorMessage: ${e.message}"))
        }
    }

    /**
     * Get all members with optional filtering
     */
    @Operation(
        summary = "Get all members",
        description = "Retrieve all members with optional filtering by active status and search term"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved members"),
            SwaggerApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping
    fun getAllMembers(
        @Parameter(description = "Filter by active members only", example = "true")
        @RequestParam(defaultValue = "true") activeOnly: Boolean,
        @Parameter(description = "Maximum number of results to return", example = "100")
        @RequestParam(defaultValue = "100") limit: Int,
        @Parameter(description = "Number of results to skip", example = "0")
        @RequestParam(defaultValue = "0") offset: Int,
        @Parameter(description = "Search term for member names")
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<List<*>>> {
        return try {
            val members = runBlocking {
                when {
                    search != null -> memberRepository.findByName(search, limit)
                    activeOnly -> memberRepository.findAllActive(limit, offset)
                    else -> memberRepository.findAll(limit, offset)
                }
            }
            ResponseEntity.ok(ApiResponse.success(members))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<List<*>>("Failed to retrieve members: ${e.message}"))
        }
    }

    /**
     * Get member by ID
     */
    @Operation(
        summary = "Get member by ID",
        description = "Retrieve a specific member by their unique identifier"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "Member found successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Invalid member ID format"),
            SwaggerApiResponse(responseCode = "404", description = "Member not found"),
            SwaggerApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/{id}")
    fun getMemberById(
        @Parameter(description = "Member unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<*>> {
        return handleUseCaseExecution(
            operation = {
                val memberId = uuidFrom(id)
                val request = GetMemberUseCase.GetMemberRequest(memberId)
                getMemberUseCase.execute(request)
            },
            extractData = { (it as GetMemberUseCase.GetMemberResponse).member }
        )
    }

    /**
     * Get member by membership number
     */
    @GetMapping("/by-membership-number/{membershipNumber}")
    fun getMemberByMembershipNumber(@PathVariable membershipNumber: String): ResponseEntity<ApiResponse<*>> {
        return try {
            val response = runBlocking { getMemberUseCase.getByMembershipNumber(membershipNumber) }

            if (response.success && response.data != null) {
                ResponseEntity.ok(ApiResponse.success((response.data as GetMemberUseCase.GetMemberResponse).member))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error<Any>("Member not found"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to retrieve member: ${e.message}"))
        }
    }

    /**
     * Get member by email
     */
    @GetMapping("/by-email/{email}")
    fun getMemberByEmail(@PathVariable email: String): ResponseEntity<ApiResponse<*>> {
        return try {
            val response = runBlocking { getMemberUseCase.getByEmail(email) }

            if (response.success && response.data != null) {
                ResponseEntity.ok(ApiResponse.success((response.data as GetMemberUseCase.GetMemberResponse).member))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error<Any>("Member not found"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to retrieve member: ${e.message}"))
        }
    }

    /**
     * Get member statistics
     */
    @GetMapping("/stats")
    fun getMemberStats(): ResponseEntity<ApiResponse<MemberStats>> {
        return try {
            val activeCount = runBlocking { memberRepository.countActive() }
            val totalCount = runBlocking { memberRepository.countAll() }

            val stats = MemberStats(
                totalActive = activeCount,
                totalMembers = totalCount
            )

            ResponseEntity.ok(ApiResponse.success(stats))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<MemberStats>("Failed to retrieve member statistics: ${e.message}"))
        }
    }

    /**
     * Create new member
     */
    @Operation(
        summary = "Create new member",
        description = "Create a new member with the provided information"
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "201", description = "Member created successfully"),
            SwaggerApiResponse(responseCode = "400", description = "Invalid request data"),
            SwaggerApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PostMapping
    fun createMember(
        @Parameter(description = "Member creation request data")
        @RequestBody createRequest: CreateMemberRequest
    ): ResponseEntity<ApiResponse<*>> {
        return handleUseCaseExecution(
            operation = {
                val useCaseRequest = CreateMemberUseCase.CreateMemberRequest(
                    firstName = createRequest.firstName,
                    lastName = createRequest.lastName,
                    email = createRequest.email,
                    phone = createRequest.phone,
                    dateOfBirth = createRequest.dateOfBirth,
                    membershipNumber = createRequest.membershipNumber,
                    membershipStartDate = createRequest.membershipStartDate,
                    membershipEndDate = createRequest.membershipEndDate,
                    isActive = createRequest.isActive,
                    address = createRequest.address,
                    emergencyContact = createRequest.emergencyContact
                )
                createMemberUseCase.execute(useCaseRequest)
            },
            successStatus = HttpStatus.CREATED,
            extractData = { (it as CreateMemberUseCase.CreateMemberResponse).member }
        )
    }

    /**
     * Update member
     */
    @PutMapping("/{id}")
    fun updateMember(@PathVariable id: String, @RequestBody updateRequest: UpdateMemberRequest): ResponseEntity<ApiResponse<*>> {
        return try {
            val memberId = uuidFrom(id)
            val useCaseRequest = UpdateMemberUseCase.UpdateMemberRequest(
                memberId = memberId,
                firstName = updateRequest.firstName,
                lastName = updateRequest.lastName,
                email = updateRequest.email,
                phone = updateRequest.phone,
                dateOfBirth = updateRequest.dateOfBirth,
                membershipNumber = updateRequest.membershipNumber,
                membershipStartDate = updateRequest.membershipStartDate,
                membershipEndDate = updateRequest.membershipEndDate,
                isActive = updateRequest.isActive,
                address = updateRequest.address,
                emergencyContact = updateRequest.emergencyContact
            )

            val response = runBlocking { updateMemberUseCase.execute(useCaseRequest) }

            if (response.success && response.data != null) {
                ResponseEntity.ok(ApiResponse.success((response.data as UpdateMemberUseCase.UpdateMemberResponse).member))
            } else {
                val statusCode = when (response.error?.code) {
                    "MEMBER_NOT_FOUND" -> HttpStatus.NOT_FOUND
                    else -> HttpStatus.BAD_REQUEST
                }
                ResponseEntity.status(statusCode)
                    .body(ApiResponse.error<Any>(response.error?.message ?: "Failed to update member"))
            }
        } catch (_: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error<Any>("Invalid member ID format"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to update member: ${e.message}"))
        }
    }

    /**
     * Get members with expiring memberships
     */
    @GetMapping("/expiring-memberships")
    fun getExpiringMemberships(
        @RequestParam(defaultValue = "30") daysAhead: Int
    ): ResponseEntity<ApiResponse<*>> {
        return try {
            val request = FindExpiringMembershipsUseCase.FindExpiringMembershipsRequest(daysAhead)
            val response = runBlocking { findExpiringMembershipsUseCase.execute(request) }

            if (response.success && response.data != null) {
                ResponseEntity.ok(ApiResponse.success(response.data))
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error<Any>(response.error?.message ?: "Failed to find expiring memberships"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to find expiring memberships: ${e.message}"))
        }
    }

    /**
     * Get members by date range
     */
    @GetMapping("/by-date-range")
    fun getMembersByDateRange(
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam(defaultValue = "MEMBERSHIP_START_DATE") dateType: String
    ): ResponseEntity<ApiResponse<*>> {
        return try {
            val startLocalDate = LocalDate.parse(startDate)
            val endLocalDate = LocalDate.parse(endDate)
            val dateRangeType = FindMembersByDateRangeUseCase.DateRangeType.valueOf(dateType)

            val request = FindMembersByDateRangeUseCase.FindMembersByDateRangeRequest(
                startDate = startLocalDate,
                endDate = endLocalDate,
                dateType = dateRangeType
            )
            val response = runBlocking { findMembersByDateRangeUseCase.execute(request) }

            if (response.success && response.data != null) {
                ResponseEntity.ok(ApiResponse.success(response.data))
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error<Any>(response.error?.message ?: "Failed to find members by date range"))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error<Any>("Invalid date format or date type. Use YYYY-MM-DD format and MEMBERSHIP_START_DATE or MEMBERSHIP_END_DATE"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to find members by date range: ${e.message}"))
        }
    }

    /**
     * Validate email uniqueness
     */
    @GetMapping("/validate/email/{email}")
    fun validateEmail(
        @PathVariable email: String,
        @RequestParam(required = false) excludeMemberId: String?
    ): ResponseEntity<ApiResponse<*>> {
        return try {
            val excludeId = excludeMemberId?.let { uuidFrom(it) }
            val request = ValidateMemberDataUseCase.ValidateEmailRequest(email, excludeId)
            val response = runBlocking { validateMemberDataUseCase.validateEmail(request) }

            if (response.success && response.data != null) {
                ResponseEntity.ok(ApiResponse.success(response.data))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error<Any>(response.error?.message ?: "Failed to validate email"))
            }
        } catch (_: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error<Any>("Invalid member ID format"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to validate email: ${e.message}"))
        }
    }

    /**
     * Validate membership number uniqueness
     */
    @GetMapping("/validate/membership-number/{membershipNumber}")
    fun validateMembershipNumber(
        @PathVariable membershipNumber: String,
        @RequestParam(required = false) excludeMemberId: String?
    ): ResponseEntity<ApiResponse<*>> {
        return try {
            val excludeId = excludeMemberId?.let { uuidFrom(it) }
            val request = ValidateMemberDataUseCase.ValidateMembershipNumberRequest(membershipNumber, excludeId)
            val response = runBlocking { validateMemberDataUseCase.validateMembershipNumber(request) }

            if (response.success && response.data != null) {
                ResponseEntity.ok(ApiResponse.success(response.data))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error<Any>(response.error?.message ?: "Failed to validate membership number"))
            }
        } catch (_: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error<Any>("Invalid member ID format"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to validate membership number: ${e.message}"))
        }
    }

    /**
     * Delete member
     */
    @DeleteMapping("/{id}")
    fun deleteMember(@PathVariable id: String): ResponseEntity<ApiResponse<String>> {
        return try {
            val memberId = uuidFrom(id)
            val request = DeleteMemberUseCase.DeleteMemberRequest(memberId)
            val response = runBlocking { deleteMemberUseCase.execute(request) }

            if (response.success) {
                ResponseEntity.ok(ApiResponse.success("Member deleted successfully"))
            } else {
                val statusCode = when (response.error?.code) {
                    "MEMBER_NOT_FOUND" -> HttpStatus.NOT_FOUND
                    else -> HttpStatus.BAD_REQUEST
                }
                ResponseEntity.status(statusCode)
                    .body(ApiResponse.error<String>(response.error?.message ?: "Failed to delete member"))
            }
        } catch (_: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error<String>("Invalid member ID format"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("Failed to delete member: ${e.message}"))
        }
    }

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

    data class UpdateMemberRequest(
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

    data class MemberStats(
        val totalActive: Long,
        val totalMembers: Long
    )
}
