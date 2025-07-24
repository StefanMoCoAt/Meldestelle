package at.mocode.members.api.rest

import at.mocode.core.domain.model.ApiResponse
import at.mocode.members.application.usecase.CreateMemberUseCase
import at.mocode.members.application.usecase.DeleteMemberUseCase
import at.mocode.members.application.usecase.GetMemberUseCase
import at.mocode.members.application.usecase.UpdateMemberUseCase
import at.mocode.members.domain.repository.MemberRepository
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST API controller for member management operations.
 *
 * This controller provides HTTP endpoints for all member-related operations
 * including CRUD operations and member search functionality.
 */
@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberRepository: MemberRepository
) {

    private val createMemberUseCase = CreateMemberUseCase(memberRepository)
    private val getMemberUseCase = GetMemberUseCase(memberRepository)
    private val updateMemberUseCase = UpdateMemberUseCase(memberRepository)
    private val deleteMemberUseCase = DeleteMemberUseCase(memberRepository)

    /**
     * Get all members with optional filtering
     */
    @GetMapping
    fun getAllMembers(
        @RequestParam(defaultValue = "true") activeOnly: Boolean,
        @RequestParam(defaultValue = "100") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
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
    @GetMapping("/{id}")
    fun getMemberById(@PathVariable id: String): ResponseEntity<ApiResponse<*>> {
        return try {
            val memberId = uuidFrom(id)
            val request = GetMemberUseCase.GetMemberRequest(memberId)
            val response = runBlocking { getMemberUseCase.execute(request) }

            if (response.success && response.data != null) {
                ResponseEntity.ok(ApiResponse.success((response.data as GetMemberUseCase.GetMemberResponse).member))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error<Any>("Member not found"))
            }
        } catch (_: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error<Any>("Invalid member ID format"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to retrieve member: ${e.message}"))
        }
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
    @PostMapping
    fun createMember(@RequestBody createRequest: CreateMemberRequest): ResponseEntity<ApiResponse<*>> {
        return try {
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

            val response = runBlocking { createMemberUseCase.execute(useCaseRequest) }

            if (response.success && response.data != null) {
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success((response.data as CreateMemberUseCase.CreateMemberResponse).member))
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error<Any>(response.error?.message ?: "Failed to create member"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Any>("Failed to create member: ${e.message}"))
        }
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
