package at.mocode.members.domain.repository

import at.mocode.members.domain.model.Member
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate

/**
 * Repository interface for Member entities.
 *
 * This interface defines the contract for data access operations
 * related to members in the member management bounded context.
 */
interface MemberRepository {

    /**
     * Finds a member by their unique identifier.
     *
     * @param id The unique identifier of the member
     * @return The member if found, null otherwise
     */
    suspend fun findById(id: Uuid): Member?

    /**
     * Finds a member by their membership number.
     *
     * @param membershipNumber The membership number to search for
     * @return The member if found, null otherwise
     */
    suspend fun findByMembershipNumber(membershipNumber: String): Member?

    /**
     * Finds a member by their email address.
     *
     * @param email The email address to search for
     * @return The member if found, null otherwise
     */
    suspend fun findByEmail(email: String): Member?

    /**
     * Finds members by name (partial match on first or last name).
     *
     * @param searchTerm The search term to match against member names
     * @param limit Maximum number of results to return
     * @return List of matching members
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<Member>

    /**
     * Finds all active members.
     *
     * @param limit Maximum number of results to return
     * @param offset Number of results to skip
     * @return List of active members
     */
    suspend fun findAllActive(limit: Int = 100, offset: Int = 0): List<Member>

    /**
     * Finds all members (active and inactive).
     *
     * @param limit Maximum number of results to return
     * @param offset Number of results to skip
     * @return List of all members
     */
    suspend fun findAll(limit: Int = 100, offset: Int = 0): List<Member>

    /**
     * Finds members whose membership started within a date range.
     *
     * @param startDate The earliest membership start date to include
     * @param endDate The latest membership start date to include
     * @return List of members within the specified date range
     */
    suspend fun findByMembershipStartDateRange(startDate: LocalDate, endDate: LocalDate): List<Member>

    /**
     * Finds members whose membership expires within a date range.
     *
     * @param startDate The earliest membership end date to include
     * @param endDate The latest membership end date to include
     * @return List of members whose membership expires within the specified date range
     */
    suspend fun findByMembershipEndDateRange(startDate: LocalDate, endDate: LocalDate): List<Member>

    /**
     * Finds members with expiring memberships (within the next specified days).
     *
     * @param daysAhead Number of days to look ahead for expiring memberships
     * @return List of members with expiring memberships
     */
    suspend fun findMembersWithExpiringMembership(daysAhead: Int = 30): List<Member>

    /**
     * Saves a member (insert or update).
     *
     * @param member The member to save
     * @return The saved member
     */
    suspend fun save(member: Member): Member

    /**
     * Deletes a member by their ID.
     *
     * @param id The unique identifier of the member to delete
     * @return True if the member was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Counts the number of active members.
     *
     * @return The number of active members
     */
    suspend fun countActive(): Long

    /**
     * Counts the total number of members.
     *
     * @return The total number of members
     */
    suspend fun countAll(): Long

    /**
     * Checks if a membership number already exists.
     *
     * @param membershipNumber The membership number to check
     * @param excludeMemberId Optional member ID to exclude from the check (for updates)
     * @return True if the membership number exists, false otherwise
     */
    suspend fun existsByMembershipNumber(membershipNumber: String, excludeMemberId: Uuid? = null): Boolean

    /**
     * Checks if an email address already exists.
     *
     * @param email The email address to check
     * @param excludeMemberId Optional member ID to exclude from the check (for updates)
     * @return True if the email exists, false otherwise
     */
    suspend fun existsByEmail(email: String, excludeMemberId: Uuid? = null): Boolean
}
