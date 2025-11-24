@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.members.api.rest

import at.mocode.core.domain.model.ApiResponse
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import java.time.ZoneId
import java.time.LocalDate as JLocalDate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.uuid.Uuid

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "Mitgliederverwaltungs-Operationen (minimal)")
class MemberController(
    @Qualifier("memberRepositoryImpl") private val memberRepository: MemberRepository
) {

    data class SyncResponse(
        val ensured: Boolean,
        val created: Boolean,
        val memberId: String?,
        val membershipNumber: String?
    )

    data class MemberProfileDto(
        val id: String? = null,
        val username: String? = null,
        val email: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val roles: List<String> = emptyList()
    )

    // Synchronisiert/erstellt bei Bedarf ein Member-Profil basierend auf den JWT-Claims
    @Operation(
        summary = "Synchronisiert das Member-Profil für den eingeloggten Benutzer",
        description = "Erstellt bei Bedarf ein Mitglied basierend auf den JWT-Claims (mock OEPS fetch)"
    )
    @PostMapping("/sync")
    fun syncMemberProfile(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ApiResponse<SyncResponse>> {
        return try {
            val sub = jwt.subject
            val email = jwt.getClaimAsString("email")
            val username = jwt.getClaimAsString("preferred_username")

            val ensured = runBlocking { ensureMemberProfileExists(sub, email, username) }
            ResponseEntity.ok(ApiResponse.success(ensured))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "Sync failed: ${e.message}"))
        }
    }

    // Liefert das Member-Profil des eingeloggten Benutzers
    @Operation(summary = "Eigene Profilinformationen abrufen")
    @GetMapping("/me")
    fun getMyProfile(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ApiResponse<MemberProfileDto>> {
        return try {
            val sub = jwt.subject
            val email = jwt.getClaimAsString("email")
            val username = jwt.getClaimAsString("preferred_username")

            val member = runBlocking {
                val lookupEmail = email ?: username?.let { "$it@local" } ?: "user-${sub.takeLast(8)}@local"
                memberRepository.findByEmail(lookupEmail)
            }

            val profile = if (member != null) {
                MemberProfileDto(
                    id = member.memberId.toString(),
                    username = username,
                    email = member.email,
                    firstName = member.firstName,
                    lastName = member.lastName,
                    roles = emptyList()
                )
            } else {
                MemberProfileDto(
                    id = null,
                    username = username,
                    email = email,
                    firstName = username?.substringBefore('@')?.replaceFirstChar { it.titlecase() },
                    lastName = null,
                    roles = emptyList()
                )
            }

            ResponseEntity.ok(ApiResponse.success(profile))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "Failed to load profile: ${e.message}"))
        }
    }

    private suspend fun ensureMemberProfileExists(userSub: String, email: String?, username: String?): SyncResponse {
        val lookupEmail = email ?: username?.let { "$it@local" } ?: "user-${userSub.takeLast(8)}@local"
        val existing = memberRepository.findByEmail(lookupEmail)
        if (existing != null) {
            return SyncResponse(
                ensured = true,
                created = false,
                memberId = existing.memberId.toString(),
                membershipNumber = existing.membershipNumber
            )
        }

        val (firstName, lastName) = mockFetchNames(userSub, email, username)
        val today: LocalDate = JLocalDate.now(ZoneId.systemDefault()).toKotlinLocalDate()
        val generatedMembershipNumber = "AUTO-${userSub.takeLast(8)}"

        val newMember = Member(
            firstName = firstName,
            lastName = lastName,
            email = lookupEmail,
            membershipNumber = generatedMembershipNumber,
            membershipStartDate = today,
            phone = null,
            dateOfBirth = null,
            membershipEndDate = null,
            isActive = true,
            address = null,
            emergencyContact = null
        )

        val saved = memberRepository.save(newMember)
        return SyncResponse(
            ensured = true,
            created = true,
            memberId = saved.memberId.toString(),
            membershipNumber = saved.membershipNumber
        )
    }

    private fun mockFetchNames(sub: String, email: String?, username: String?): Pair<String, String> {
        val source = username ?: email ?: sub
        val base = source.substringBefore('@').replace(".", " ").trim().ifBlank { "Reiter" }
        val parts = base.split(" ")
        val first = parts.firstOrNull()?.replaceFirstChar { it.titlecase() } ?: "Reiter"
        val last = parts.drop(1).joinToString(" ").ifBlank { "Unbekannt" }.replaceFirstChar { it.titlecase() }
        return first to last
    }
}
