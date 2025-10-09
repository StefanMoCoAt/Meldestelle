@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import java.time.LocalDateTime
import kotlin.uuid.Uuid

/**
 * Service für Benutzerauthentifizierung und Passwortverwaltung.
 */
interface AuthenticationService {
    /**
     * Authentifiziert einen Benutzer mit Benutzernamen und Passwort.
     *
     * @param username Der Benutzername
     * @param password Das Passwort
     * @return Das Authentifizierungsergebnis
     */
    suspend fun authenticate(username: String, password: String): AuthResult

    /**
     * Ändert das Passwort eines Benutzers.
     *
     * @param userId Die Benutzer-ID
     * @param currentPassword Das aktuelle Passwort
     * @param newPassword Das neue Passwort
     * @return Das Ergebnis der Passwortänderung
     */
    suspend fun changePassword(userId: Uuid?, currentPassword: String, newPassword: String): PasswordChangeResult

    /**
     * Mögliche Ergebnisse eines Authentifizierungsversuchs.
     */
    sealed class AuthResult {
        /**
         * Authentifizierung war erfolgreich.
         *
         * @param token Das JWT-Token
         * @param user Der authentifizierte Benutzer
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
         * The password change was successful.
         */
        data object Success : PasswordChangeResult()

        /**
         * Password change failed.
         *
         * @param reason The reason for the failure
         */
        data class Failure(val reason: String) : PasswordChangeResult()

        /**
         * The new password is too weak.
         */
        data object WeakPassword : PasswordChangeResult()
    }

    /**
     * Represents an authenticated user.
     */
    data class AuthenticatedUser(
        val userId: Uuid?,
        val personId: Uuid?,
        val username: String,
        val email: String,
        val permissions: List<BerechtigungE>
    )
}
