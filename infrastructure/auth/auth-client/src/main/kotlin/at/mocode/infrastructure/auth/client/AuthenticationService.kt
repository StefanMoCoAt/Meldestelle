package at.mocode.infrastructure.auth.client

import com.benasher44.uuid.Uuid
import java.time.LocalDateTime

/**
 * Service for user authentication and password management.
 */
interface AuthenticationService {
    /**
     * Authenticates a user with the given username and password.
     *
     * @param username The username
     * @param password The password
     * @return The authentication result
     */
    suspend fun authenticate(username: String, password: String): AuthResult

    /**
     * Changes a user's password.
     *
     * @param userId The user ID
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return The password change result
     */
    suspend fun changePassword(userId: Uuid, currentPassword: String, newPassword: String): PasswordChangeResult

    /**
     * Possible results of an authentication attempt.
     */
    sealed class AuthResult {
        /**
         * Authentication was successful.
         *
         * @param token The JWT token
         * @param user The authenticated user
         */
        data class Success(val token: String, val user: AuthenticatedUser) : AuthResult()

        /**
         * Authentication failed.
         *
         * @param reason The reason for the failure
         */
        data class Failure(val reason: String) : AuthResult()

        /**
         * The account is locked.
         *
         * @param lockedUntil The time until which the account is locked
         */
        data class Locked(val lockedUntil: LocalDateTime) : AuthResult()
    }

    /**
     * Possible results of a password change attempt.
     */
    sealed class PasswordChangeResult {
        /**
         * Password change was successful.
         */
        object Success : PasswordChangeResult()

        /**
         * Password change failed.
         *
         * @param reason The reason for the failure
         */
        data class Failure(val reason: String) : PasswordChangeResult()

        /**
         * The new password is too weak.
         */
        object WeakPassword : PasswordChangeResult()
    }

    /**
     * Represents an authenticated user.
     */
    data class AuthenticatedUser(
        val userId: Uuid,
        val personId: Uuid,
        val username: String,
        val email: String,
        val permissions: List<String>
    )
}
