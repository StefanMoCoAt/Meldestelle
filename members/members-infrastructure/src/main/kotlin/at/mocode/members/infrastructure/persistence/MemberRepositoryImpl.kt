package at.mocode.members.infrastructure.persistence

import at.mocode.core.utils.database.DatabaseFactory
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

/**
 * Database implementation of MemberRepository using Exposed ORM.
 */
@Repository
class MemberRepositoryImpl : MemberRepository {

    override suspend fun findById(id: Uuid): Member? = DatabaseFactory.dbQuery {
        MemberTable.selectAll().where { MemberTable.id eq id }
            .map { rowToMember(it) }
            .singleOrNull()
    }

    override suspend fun findByMembershipNumber(membershipNumber: String): Member? = DatabaseFactory.dbQuery {
        MemberTable.selectAll().where { MemberTable.membershipNumber eq membershipNumber }
            .map { rowToMember(it) }
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): Member? = DatabaseFactory.dbQuery {
        MemberTable.selectAll().where { MemberTable.email.lowerCase() eq email.lowercase() }
            .map { rowToMember(it) }
            .singleOrNull()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Member> = DatabaseFactory.dbQuery {
        MemberTable.selectAll().where {
            (MemberTable.firstName.lowerCase() like "%${searchTerm.lowercase()}%") or
                (MemberTable.lastName.lowerCase() like "%${searchTerm.lowercase()}%")
        }
        .limit(limit)
        .map { rowToMember(it) }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Member> = DatabaseFactory.dbQuery {
        MemberTable.selectAll().where { MemberTable.isActive eq true }
            .limit(limit, offset.toLong())
            .map { rowToMember(it) }
    }

    override suspend fun findAll(limit: Int, offset: Int): List<Member> = DatabaseFactory.dbQuery {
        MemberTable.selectAll()
            .limit(limit, offset.toLong())
            .map { rowToMember(it) }
    }

    override suspend fun findByMembershipStartDateRange(startDate: LocalDate, endDate: LocalDate): List<Member> = DatabaseFactory.dbQuery {
        MemberTable.selectAll().where {
            (MemberTable.membershipStartDate greaterEq startDate) and
                (MemberTable.membershipStartDate lessEq endDate)
        }
        .map { rowToMember(it) }
    }

    override suspend fun findByMembershipEndDateRange(startDate: LocalDate, endDate: LocalDate): List<Member> = DatabaseFactory.dbQuery {
        MemberTable.selectAll().where {
            (MemberTable.membershipEndDate.isNotNull()) and
                (MemberTable.membershipEndDate greaterEq startDate) and
                (MemberTable.membershipEndDate lessEq endDate)
        }
        .map { rowToMember(it) }
    }

    override suspend fun findMembersWithExpiringMembership(daysAhead: Int): List<Member> = DatabaseFactory.dbQuery {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val futureDate = LocalDate(currentDate.year, currentDate.month, currentDate.dayOfMonth + daysAhead)
        MemberTable.selectAll().where {
            (MemberTable.membershipEndDate.isNotNull()) and
                (MemberTable.membershipEndDate lessEq futureDate) and
                (MemberTable.isActive eq true)
        }
        .map { rowToMember(it) }
    }

    override suspend fun save(member: Member): Member = DatabaseFactory.dbQuery {
        val existingMember = MemberTable.selectAll().where { MemberTable.id eq member.memberId }.singleOrNull()

        if (existingMember != null) {
            // Update existing member
            MemberTable.update({ MemberTable.id eq member.memberId }) {
                it[firstName] = member.firstName
                it[lastName] = member.lastName
                it[email] = member.email
                it[phone] = member.phone
                it[dateOfBirth] = member.dateOfBirth
                it[membershipNumber] = member.membershipNumber
                it[membershipStartDate] = member.membershipStartDate
                it[membershipEndDate] = member.membershipEndDate
                it[isActive] = member.isActive
                it[address] = member.address
                it[emergencyContact] = member.emergencyContact
                it[updatedAt] = Clock.System.now()
            }
        } else {
            // Insert new member
            MemberTable.insert {
                it[id] = member.memberId
                it[firstName] = member.firstName
                it[lastName] = member.lastName
                it[email] = member.email
                it[phone] = member.phone
                it[dateOfBirth] = member.dateOfBirth
                it[membershipNumber] = member.membershipNumber
                it[membershipStartDate] = member.membershipStartDate
                it[membershipEndDate] = member.membershipEndDate
                it[isActive] = member.isActive
                it[address] = member.address
                it[emergencyContact] = member.emergencyContact
            }
        }
        member
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        MemberTable.deleteWhere { MemberTable.id eq id } > 0
    }

    override suspend fun countActive(): Long = DatabaseFactory.dbQuery {
        MemberTable.selectAll().where { MemberTable.isActive eq true }.count()
    }

    override suspend fun countAll(): Long = DatabaseFactory.dbQuery {
        MemberTable.selectAll().count()
    }

    override suspend fun existsByMembershipNumber(membershipNumber: String, excludeMemberId: Uuid?): Boolean = DatabaseFactory.dbQuery {
        val query = if (excludeMemberId != null) {
            MemberTable.selectAll().where {
                (MemberTable.membershipNumber eq membershipNumber) and
                    (MemberTable.id neq excludeMemberId)
            }
        } else {
            MemberTable.selectAll().where { MemberTable.membershipNumber eq membershipNumber }
        }
        query.count() > 0
    }

    override suspend fun existsByEmail(email: String, excludeMemberId: Uuid?): Boolean = DatabaseFactory.dbQuery {
        val query = if (excludeMemberId != null) {
            MemberTable.selectAll().where {
                (MemberTable.email.lowerCase() eq email.lowercase()) and
                    (MemberTable.id neq excludeMemberId)
            }
        } else {
            MemberTable.selectAll().where { MemberTable.email.lowerCase() eq email.lowercase() }
        }
        query.count() > 0
    }

    private fun rowToMember(row: ResultRow): Member {
        return Member(
            memberId = row[MemberTable.id],
            firstName = row[MemberTable.firstName],
            lastName = row[MemberTable.lastName],
            email = row[MemberTable.email],
            phone = row[MemberTable.phone],
            dateOfBirth = row[MemberTable.dateOfBirth],
            membershipNumber = row[MemberTable.membershipNumber],
            membershipStartDate = row[MemberTable.membershipStartDate],
            membershipEndDate = row[MemberTable.membershipEndDate],
            isActive = row[MemberTable.isActive],
            address = row[MemberTable.address],
            emergencyContact = row[MemberTable.emergencyContact]
        )
    }
}
