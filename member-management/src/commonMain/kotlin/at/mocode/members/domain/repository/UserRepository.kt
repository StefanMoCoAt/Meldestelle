package at.mocode.members.domain.repository

import at.mocode.members.domain.model.DomUser
import com.benasher44.uuid.Uuid

/**
 * Repository interface for user management operations.
 *
 * Provides methods for user authentication, user management,
 * and user-related database operations.
 */
interface UserRepository {

    /**
     * Creates a new user in the system.
     *
     * @param user The user to create
     * @return The created user with generated ID
     */
    suspend fun createUser(user: DomUser): DomUser

    /**
     * Finds a user by their unique user ID.
     *
     * @param userId The unique user ID
     * @return The user if found, null otherwise
     */
    suspend fun findById(userId: Uuid): DomUser?

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for
     * @return The user if found, null otherwise
     */
    suspend fun findByUsername(username: String): DomUser?

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for
     * @return The user if found, null otherwise
     */
    suspend fun findByEmail(email: String): DomUser?

    /**
     * Finds a user by their associated person ID.
     *
     * @param personId The person ID to search for
     * @return The user if found, null otherwise
     */
    suspend fun findByPersonId(personId: Uuid): DomUser?

    /**
     * Updates an existing user.
     *
     * @param user The user to update
     * @return The updated user
     */
    suspend fun updateUser(user: DomUser): DomUser

    /**
     * Updates the last login timestamp for a user.
     *
     * @param userId The user ID
     */
    suspend fun updateLastLogin(userId: Uuid)

    /**
     * Increments the failed login attempts counter for a user.
     *
     * @param userId The user ID
     */
    suspend fun incrementFailedLoginAttempts(userId: Uuid)

    /**
     * Resets the failed login attempts counter for a user.
     *
     * @param userId The user ID
     */
    suspend fun resetFailedLoginAttempts(userId: Uuid)

    /**
     * Locks a user account until the specified timestamp.
     *
     * @param userId The user ID
     * @param lockedUntil The timestamp until when the user is locked
     */
    suspend fun lockUser(userId: Uuid, lockedUntil: kotlinx.datetime.Instant)

    /**
     * Unlocks a user account.
     *
     * @param userId The user ID
     */
    suspend fun unlockUser(userId: Uuid)

    /**
     * Activates or deactivates a user account.
     *
     * @param userId The user ID
     * @param isActive Whether the user should be active
     */
    suspend fun setUserActive(userId: Uuid, isActive: Boolean)

    /**
     * Marks a user's email as verified.
     *
     * @param userId The user ID
     */
    suspend fun markEmailAsVerified(userId: Uuid)

    /**
     * Updates a user's password hash and salt.
     *
     * @param userId The user ID
     * @param passwordHash The new password hash
     * @param salt The new salt
     */
    suspend fun updatePassword(userId: Uuid, passwordHash: String, salt: String)

    /**
     * Deletes a user from the system.
     *
     * @param userId The user ID to delete
     * @return True if the user was deleted, false if not found
     */
    suspend fun deleteUser(userId: Uuid): Boolean

    /**
     * Gets all users in the system.
     *
     * @return List of all users
     */
    suspend fun getAllUsers(): List<DomUser>

    /**
     * Gets all active users in the system.
     *
     * @return List of all active users
     */
    suspend fun getActiveUsers(): List<DomUser>
}
