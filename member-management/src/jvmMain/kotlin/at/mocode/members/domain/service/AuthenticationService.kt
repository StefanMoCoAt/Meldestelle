package at.mocode.members.domain.service

import at.mocode.members.domain.model.DomUser
import at.mocode.members.domain.repository.UserRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock

/**
 * Service für die Authentifizierung von Benutzern im System.
 */
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService,
    private val jwtService: JwtService
) {

    companion object {
        // Konfigurierbare Werte für die Kontosperrung
        private const val MAX_FAILED_LOGIN_ATTEMPTS = 5
        private const val LOCK_DURATION_MINUTES = 15L
    }

    /**
     * Authentifiziert einen Benutzer anhand von Benutzername und Passwort.
     *
     * @param username Der Benutzername
     * @param password Das Passwort
     * @return AuthResult mit dem Ergebnis der Authentifizierung
     */
    suspend fun authenticate(username: String, password: String): AuthResult {
        // Benutzer suchen
        val user = userRepository.findByUsername(username)
            ?: return AuthResult.Failure("Ungültiger Benutzername oder Passwort")

        // Prüfen, ob der Benutzer aktiv ist
        if (!user.istAktiv) {
            return AuthResult.Failure("Dieser Account ist deaktiviert")
        }

        // Prüfen, ob der Account gesperrt ist
        if (user.isLocked()) {
            return AuthResult.Locked(user.gesperrtBis!!)
        }

        // Passwort überprüfen
        if (!passwordService.verifyPassword(password, user.passwordHash, user.salt)) {
            // Fehlgeschlagene Anmeldeversuche erhöhen
            userRepository.incrementFailedLoginAttempts(user.userId)

            // Benutzer sperren, wenn zu viele Anmeldeversuche fehlgeschlagen sind
            val updatedUser = userRepository.findById(user.userId)!!
            if (updatedUser.fehlgeschlageneAnmeldungen >= MAX_FAILED_LOGIN_ATTEMPTS) {
                val lockUntil = Clock.System.now().plus(kotlin.time.Duration.parse("${LOCK_DURATION_MINUTES}m"))
                userRepository.lockUser(user.userId, lockUntil)
                return AuthResult.Locked(lockUntil)
            }

            return AuthResult.Failure("Ungültiger Benutzername oder Passwort")
        }

        // Erfolgreiche Anmeldung - Fehlgeschlagene Anmeldeversuche zurücksetzen und letzten Login aktualisieren
        userRepository.resetFailedLoginAttempts(user.userId)
        userRepository.updateLastLogin(user.userId)

        // JWT-Token erstellen
        val token = jwtService.createToken(user)

        return AuthResult.Success(token, user)
    }

    /**
     * Registriert einen neuen Benutzer im System.
     *
     * @param username Der Benutzername
     * @param email Die E-Mail-Adresse
     * @param password Das Passwort
     * @param personId Die ID der zugehörigen Person
     * @return RegisterResult mit dem Ergebnis der Registrierung
     */
    suspend fun registerUser(username: String, email: String, password: String, personId: Uuid): RegisterResult {
        // Prüfen, ob Benutzername bereits existiert
        if (userRepository.findByUsername(username) != null) {
            return RegisterResult.Failure("Benutzername wird bereits verwendet")
        }

        // Prüfen, ob E-Mail bereits existiert
        if (userRepository.findByEmail(email) != null) {
            return RegisterResult.Failure("E-Mail-Adresse wird bereits verwendet")
        }

        // Prüfen, ob Person bereits einen Benutzer hat
        if (userRepository.findByPersonId(personId) != null) {
            return RegisterResult.Failure("Diese Person hat bereits einen Benutzeraccount")
        }

        // Passwort-Stärke prüfen
        val passwordStrength = passwordService.checkPasswordStrength(password)
        if (passwordStrength.strength == PasswordStrength.Strength.WEAK) {
            return RegisterResult.WeakPassword(passwordStrength.issues)
        }

        // Salt und Hash generieren
        val salt = passwordService.generateSalt()
        val passwordHash = passwordService.hashPassword(password, salt)

        // Benutzer erstellen
        val user = DomUser(
            personId = personId,
            username = username,
            email = email,
            passwordHash = passwordHash,
            salt = salt
        )

        // Benutzer speichern
        val createdUser = userRepository.createUser(user)

        return RegisterResult.Success(createdUser)
    }

    /**
     * Ändert das Passwort eines Benutzers.
     *
     * @param userId Die ID des Benutzers
     * @param currentPassword Das aktuelle Passwort
     * @param newPassword Das neue Passwort
     * @return PasswordChangeResult mit dem Ergebnis der Passwortänderung
     */
    suspend fun changePassword(userId: Uuid, currentPassword: String, newPassword: String): PasswordChangeResult {
        // Benutzer suchen
        val user = userRepository.findById(userId)
            ?: return PasswordChangeResult.Failure("Benutzer nicht gefunden")

        // Aktuelles Passwort überprüfen
        if (!passwordService.verifyPassword(currentPassword, user.passwordHash, user.salt)) {
            return PasswordChangeResult.Failure("Aktuelles Passwort ist falsch")
        }

        // Passwort-Stärke prüfen
        val passwordStrength = passwordService.checkPasswordStrength(newPassword)
        if (passwordStrength.strength == PasswordStrength.Strength.WEAK) {
            return PasswordChangeResult.WeakPassword(passwordStrength.issues)
        }

        // Neues Passwort setzen
        val salt = passwordService.generateSalt()
        val passwordHash = passwordService.hashPassword(newPassword, salt)

        userRepository.updatePassword(userId, passwordHash, salt)

        return PasswordChangeResult.Success
    }

    /**
     * Setzt das Passwort eines Benutzers zurück.
     *
     * @param userId Die ID des Benutzers
     * @param newPassword Das neue Passwort
     * @return PasswordResetResult mit dem Ergebnis der Passwortzurücksetzung
     */
    suspend fun resetPassword(userId: Uuid, newPassword: String): PasswordResetResult {
        // Benutzer suchen
        val user = userRepository.findById(userId)
            ?: return PasswordResetResult.Failure("Benutzer nicht gefunden")

        // Passwort-Stärke prüfen
        val passwordStrength = passwordService.checkPasswordStrength(newPassword)
        if (passwordStrength.strength == PasswordStrength.Strength.WEAK) {
            return PasswordResetResult.WeakPassword(passwordStrength.issues)
        }

        // Neues Passwort setzen
        val salt = passwordService.generateSalt()
        val passwordHash = passwordService.hashPassword(newPassword, salt)

        userRepository.updatePassword(userId, passwordHash, salt)

        return PasswordResetResult.Success
    }

    /**
     * Ergebnis einer Authentifizierung.
     */
    sealed class AuthResult {
        /**
         * Erfolgreiche Authentifizierung.
         *
         * @property token Das JWT-Token für den authentifizierten Benutzer
         * @property user Der authentifizierte Benutzer
         */
        data class Success(val token: String, val user: DomUser) : AuthResult()

        /**
         * Fehlgeschlagene Authentifizierung.
         *
         * @property reason Der Grund für den Fehlschlag
         */
        data class Failure(val reason: String) : AuthResult()

        /**
         * Account ist gesperrt.
         *
         * @property lockedUntil Zeitpunkt, bis zu dem der Account gesperrt ist
         */
        data class Locked(val lockedUntil: kotlinx.datetime.Instant) : AuthResult()
    }

    /**
     * Ergebnis einer Benutzerregistrierung.
     */
    sealed class RegisterResult {
        /**
         * Erfolgreiche Registrierung.
         *
         * @property user Der erstellte Benutzer
         */
        data class Success(val user: DomUser) : RegisterResult()

        /**
         * Fehlgeschlagene Registrierung.
         *
         * @property reason Der Grund für den Fehlschlag
         */
        data class Failure(val reason: String) : RegisterResult()

        /**
         * Zu schwaches Passwort.
         *
         * @property issues Liste der Probleme mit dem Passwort
         */
        data class WeakPassword(val issues: List<String>) : RegisterResult()
    }

    /**
     * Ergebnis einer Passwortänderung.
     */
    sealed class PasswordChangeResult {
        /**
         * Erfolgreiche Passwortänderung.
         */
        object Success : PasswordChangeResult()

        /**
         * Fehlgeschlagene Passwortänderung.
         *
         * @property reason Der Grund für den Fehlschlag
         */
        data class Failure(val reason: String) : PasswordChangeResult()

        /**
         * Zu schwaches Passwort.
         *
         * @property issues Liste der Probleme mit dem Passwort
         */
        data class WeakPassword(val issues: List<String>) : PasswordChangeResult()
    }

    /**
     * Ergebnis einer Passwortzurücksetzung.
     */
    sealed class PasswordResetResult {
        /**
         * Erfolgreiche Passwortzurücksetzung.
         */
        object Success : PasswordResetResult()

        /**
         * Fehlgeschlagene Passwortzurücksetzung.
         *
         * @property reason Der Grund für den Fehlschlag
         */
        data class Failure(val reason: String) : PasswordResetResult()

        /**
         * Zu schwaches Passwort.
         *
         * @property issues Liste der Probleme mit dem Passwort
         */
        data class WeakPassword(val issues: List<String>) : PasswordResetResult()
    }
}
