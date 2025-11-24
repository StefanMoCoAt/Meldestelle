@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package at.mocode.members.application.usecase

import at.mocode.infrastructure.messaging.client.EventPublisher
import at.mocode.members.domain.events.MemberCreatedEvent
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.uuid.Uuid

/**
 * UseCase: Stellt sicher, dass für den authentifizierten Benutzer ein Member-Profil existiert.
 *
 * Ablauf:
 * 1) Prüft, ob ein Member zu E-Mail existiert (Fallback: username-basiert via Dummy-E-Mail)
 * 2) Wenn nicht vorhanden: erstellt minimalen Member-Datensatz (Mock für OEPS-Datenbezug)
 * 3) Publiziert MemberCreatedEvent
 */
class EnsureMemberProfileExistsUseCase(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher
) {

    data class Request(
        val userSub: String,
        val email: String?,
        val username: String?
    )

    data class Response(
        val ensured: Boolean,            // true, wenn jetzt auf jeden Fall vorhanden (neu oder bereits vorhanden)
        val created: Boolean,            // true, wenn neu angelegt
        val memberId: Uuid?,
        val membershipNumber: String?
    )

    suspend fun execute(request: Request): Response {
        // 1) Versuche per E-Mail zu finden (stabilster Identifier). Fallback: generierte Pseudo-E-Mail aus username/sub.
        val lookupEmail = request.email ?: request.username?.let { "$it@local" }
            ?: "user-${request.userSub.takeLast(8)}@local"

        val existing = memberRepository.findByEmail(lookupEmail)
        if (existing != null) {
            return Response(ensured = true, created = false, memberId = existing.memberId, membershipNumber = existing.membershipNumber)
        }

        // 2) Keine Daten vorhanden: OEPS-Daten mocken und Member erzeugen
        val (firstName, lastName) = mockFetchOepsNames(request)
        val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val generatedMembershipNumber = "AUTO-${request.userSub.takeLast(8)}"

        val newMember = Member(
            firstName = firstName,
            lastName = lastName,
            email = lookupEmail,
            membershipNumber = generatedMembershipNumber,
            membershipStartDate = today,
            // optionale Felder leer lassen
            phone = null,
            dateOfBirth = null,
            membershipEndDate = null,
            isActive = true,
            address = null,
            emergencyContact = null
        )

        val saved = memberRepository.save(newMember)

        // 3) Domain-Event publizieren (best effort, Fehler sollen nicht verhindern)
        runCatching {
            val event = MemberCreatedEvent(
                eventId = "evt-${saved.memberId}",
                memberId = saved.memberId,
                timestamp = Clock.System.now(),
                firstName = saved.firstName,
                lastName = saved.lastName,
                email = saved.email,
                membershipNumber = saved.membershipNumber,
                membershipStartDate = saved.membershipStartDate,
                isActive = saved.isActive
            )
            eventPublisher.publishEvent(
                topic = "members.events",
                key = saved.memberId.toString(),
                event = event
            )
        }

        return Response(ensured = true, created = true, memberId = saved.memberId, membershipNumber = saved.membershipNumber)
    }

    /**
     * Mock für den späteren OEPS-Datenbezug. Leitet aus Username/E-Mail simple Namen ab.
     */
    private fun mockFetchOepsNames(request: Request): Pair<String, String> {
        val source = request.username ?: request.email ?: request.userSub
        val base = source.substringBefore('@').replace(".", " ").trim().ifBlank { "Reiter" }
        val parts = base.split(" ")
        val first = parts.firstOrNull()?.replaceFirstChar { it.titlecase() } ?: "Reiter"
        val last = parts.drop(1).joinToString(" ").ifBlank { "Unbekannt" }.replaceFirstChar { it.titlecase() }
        return first to last
    }
}
