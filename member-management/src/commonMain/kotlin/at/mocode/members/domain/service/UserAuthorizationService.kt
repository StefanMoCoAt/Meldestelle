package at.mocode.members.domain.service

import at.mocode.enums.BerechtigungE
import at.mocode.enums.RolleE
import at.mocode.members.domain.repository.*
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Service for managing user authorization data.
 *
 * This service provides methods to fetch user roles and permissions from the database
 * and convert them to the format expected by the authorization system.
 */
class UserAuthorizationService(
    private val userRepository: UserRepository,
    private val personRolleRepository: PersonRolleRepository,
    private val rolleRepository: RolleRepository,
    private val rolleBerechtigungRepository: RolleBerechtigungRepository,
    private val berechtigungRepository: BerechtigungRepository
) {

    /**
     * Data class representing user authorization information.
     */
    data class UserAuthInfo(
        val userId: Uuid,
        val personId: Uuid,
        val username: String,
        val email: String,
        val roles: List<RolleE>,
        val permissions: List<BerechtigungE>
    )

    /**
     * Fetches complete authorization information for a user.
     *
     * @param userId The user ID
     * @return UserAuthInfo if the user exists and is active, null otherwise
     */
    suspend fun getUserAuthInfo(userId: Uuid): UserAuthInfo? {
        // Get user
        val user = userRepository.findById(userId) ?: return null

        // Check if the user is active
        if (!user.istAktiv) return null

        // Check if the user is locked
        val now = Clock.System.now()
        if (user.gesperrtBis != null && user.gesperrtBis!! > now) return null

        // Get user's roles
        val roles = getUserRoles(user.personId)

        // Get permissions for those roles
        val permissions = getPermissionsForRoles(roles)

        return UserAuthInfo(
            userId = user.userId,
            personId = user.personId,
            username = user.username,
            email = user.email,
            roles = roles,
            permissions = permissions
        )
    }

    /**
     * Fetches authorization information for a user by username or email.
     *
     * @param usernameOrEmail The username or email
     * @return UserAuthInfo if the user exists and is active, null otherwise
     */
    suspend fun getUserAuthInfoByUsernameOrEmail(usernameOrEmail: String): UserAuthInfo? {
        // Try to find the user by username first
        val user = userRepository.findByUsername(usernameOrEmail)
            ?: userRepository.findByEmail(usernameOrEmail)
            ?: return null

        return getUserAuthInfo(user.userId)
    }

    /**
     * Gets all active roles for a person.
     *
     * @param personId The person ID
     * @return List of active role types
     */
    suspend fun getUserRoles(personId: Uuid): List<RolleE> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Get active person roles
        val personRoles = personRolleRepository.findByPersonId(personId)
            .filter { personRolle ->
                personRolle.istAktiv &&
                personRolle.gueltigVon <= today &&
                (personRolle.gueltigBis == null || personRolle.gueltigBis!! >= today)
            }

        // Get the actual roles
        val roles = mutableListOf<RolleE>()
        for (personRolle in personRoles) {
            val rolle = rolleRepository.findById(personRolle.rolleId)
            if (rolle != null && rolle.istAktiv) {
                roles.add(rolle.rolleTyp)
            }
        }

        return roles.distinct()
    }

    /**
     * Gets all permissions for the given roles.
     *
     * @param roles List of role types
     * @return List of permission types
     */
    suspend fun getPermissionsForRoles(roles: List<RolleE>): List<BerechtigungE> {
        val permissions = mutableSetOf<BerechtigungE>()

        for (roleType in roles) {
            // Find the role by type
            val rolle = rolleRepository.findByTyp(roleType)
            if (rolle != null) {
                // Get role permissions
                val rolleBerechtigungen = rolleBerechtigungRepository.findByRolleId(rolle.rolleId)
                    .filter { it.istAktiv }

                // Get the actual permissions
                for (rolleBerechtigung in rolleBerechtigungen) {
                    val berechtigung = berechtigungRepository.findById(rolleBerechtigung.berechtigungId)
                    if (berechtigung != null && berechtigung.istAktiv) {
                        permissions.add(berechtigung.berechtigungTyp)
                    }
                }
            }
        }

        return permissions.toList()
    }

    /**
     * Checks if a user has a specific role.
     *
     * @param userId The user ID
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    suspend fun hasRole(userId: Uuid, role: RolleE): Boolean {
        val authInfo = getUserAuthInfo(userId) ?: return false
        return authInfo.roles.contains(role)
    }

    /**
     * Checks if a user has a specific permission.
     *
     * @param userId The user ID
     * @param permission The permission to check
     * @return true if the user has the permission, false otherwise
     */
    suspend fun hasPermission(userId: Uuid, permission: BerechtigungE): Boolean {
        val authInfo = getUserAuthInfo(userId) ?: return false
        return authInfo.permissions.contains(permission)
    }
}
