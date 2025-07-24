package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of MemberRepository for development and testing purposes.
 */
@Repository
class InMemoryMemberRepository : MemberRepository {

    private val members = ConcurrentHashMap<Uuid, Member>()

    override suspend fun findById(id: Uuid): Member? {
        return members[id]
    }

    override suspend fun findByMembershipNumber(membershipNumber: String): Member? {
        return members.values.find { it.membershipNumber == membershipNumber }
    }

    override suspend fun findByEmail(email: String): Member? {
        return members.values.find { it.email.equals(email, ignoreCase = true) }
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Member> {
        return members.values
            .filter {
                it.firstName.contains(searchTerm, ignoreCase = true) ||
                it.lastName.contains(searchTerm, ignoreCase = true)
            }
            .take(limit)
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Member> {
        return members.values
            .filter { it.isActive }
            .drop(offset)
            .take(limit)
    }

    override suspend fun findAll(limit: Int, offset: Int): List<Member> {
        return members.values
            .drop(offset)
            .take(limit)
    }

    override suspend fun findByMembershipStartDateRange(startDate: LocalDate, endDate: LocalDate): List<Member> {
        return members.values
            .filter { it.membershipStartDate >= startDate && it.membershipStartDate <= endDate }
    }

    override suspend fun findByMembershipEndDateRange(startDate: LocalDate, endDate: LocalDate): List<Member> {
        return members.values
            .filter { member ->
                member.membershipEndDate?.let { memberEndDate ->
                    memberEndDate >= startDate && memberEndDate <= endDate
                } ?: false
            }
    }

    override suspend fun findMembersWithExpiringMembership(daysAhead: Int): List<Member> {
        // Simplified implementation - returns members with end dates set
        return members.values
            .filter { it.membershipEndDate != null }
    }

    override suspend fun save(member: Member): Member {
        members[member.memberId] = member
        return member
    }

    override suspend fun delete(id: Uuid): Boolean {
        return members.remove(id) != null
    }

    override suspend fun countActive(): Long {
        return members.values.count { it.isActive }.toLong()
    }

    override suspend fun countAll(): Long {
        return members.size.toLong()
    }

    override suspend fun existsByMembershipNumber(membershipNumber: String, excludeMemberId: Uuid?): Boolean {
        return members.values.any {
            it.membershipNumber == membershipNumber && it.memberId != excludeMemberId
        }
    }

    override suspend fun existsByEmail(email: String, excludeMemberId: Uuid?): Boolean {
        return members.values.any {
            it.email.equals(email, ignoreCase = true) && it.memberId != excludeMemberId
        }
    }
}
