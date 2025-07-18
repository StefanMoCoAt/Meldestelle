package at.mocode.members.domain.service

import at.mocode.members.domain.model.DomUser
import at.mocode.members.domain.repository.UserRepository
import at.mocode.validation.ValidationResult
import at.mocode.validation.ValidationError
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Service for user authentication and session management.
 *
 * Handles user login, logout, registration, and JWT token management.
 * Coordinates between UserRepository, PasswordService, and other authentication components.
 */
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService,
    private val jwtService: JwtService
) {

    companion object {
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MINUTES = 30L
    }

    /**
     * Data class for login credentials.
     */
    data class LoginCredentials(
        val usernameOrEmail: String,
        val password: String
    )

    /**
     * Data class for user registration.
     */
    data class UserRegistration(
        val personId: Uuid,
        val username: String,
        val email: String,
        val password: String
    )

    /**
     * Data class for authentication result.
     */
    data class AuthenticationResult(
        val success: Boolean,
        val user: DomUser? = null,
        val token: String? = null,
        val message: String? = null
    )

    /**
     * Authenticates a user with username/email and password.
     *
     * @param credentials The login credentials
     * @return AuthenticationResult with success status and user data
     */
    suspend fun authenticate(credentials: LoginCredentials): AuthenticationResult {
        try {
            // Find user by username or email
            val user = findUserByUsernameOrEmail(credentials.usernameOrEmail)
                ?: return AuthenticationResult(
                    success = false,
                    message = "Invalid username or password"
                )

            // Check if user is locked
            if (isUserLocked(user)) {
                return AuthenticationResult(
                    success = false,
                    message = "Account is temporarily locked due to too many failed login attempts"
                )
            }

            // Check if user is active
            if (!user.istAktiv) {
                return AuthenticationResult(
                    success = false,
                    message = "Account is deactivated"
                )
            }

            // Verify password
            if (!passwordService.verifyPassword(credentials.password, user.passwordHash, user.salt)) {
                // Increment failed attempts
                userRepository.incrementFailedLoginAttempts(user.userId)

                // Lock user if too many failed attempts
                val updatedUser = userRepository.findById(user.userId)
                if (updatedUser != null && updatedUser.fehlgeschlageneAnmeldungen >= MAX_FAILED_ATTEMPTS) {
                    val lockUntil = Clock.System.now().plus(30.minutes)
                    userRepository.lockUser(user.userId, lockUntil)
                }

                return AuthenticationResult(
                    success = false,
                    message = "Invalid username or password"
                )
            }

            // Reset failed attempts on successful login
            userRepository.resetFailedLoginAttempts(user.userId)
            userRepository.updateLastLogin(user.userId)

            // Generate JWT token
            val tokenInfo = jwtService.generateToken(user)
            val token = tokenInfo.token

            return AuthenticationResult(
                success = true,
                user = user,
                token = token,
                message = "Login successful"
            )

        } catch (e: Exception) {
            return AuthenticationResult(
                success = false,
                message = "Authentication failed: ${e.message}"
            )
        }
    }

    /**
     * Data class for user registration result.
     */
    data class UserRegistrationResult(
        val success: Boolean,
        val user: DomUser? = null,
        val validationResult: ValidationResult? = null,
        val message: String? = null
    )

    /**
     * Registers a new user in the system.
     *
     * @param registration The user registration data
     * @return UserRegistrationResult with success status and user data
     */
    suspend fun registerUser(registration: UserRegistration): UserRegistrationResult {
        try {
            // Validate password strength
            val passwordErrors = passwordService.getPasswordValidationErrors(registration.password)
            if (passwordErrors.isNotEmpty()) {
                val errors = passwordErrors.map { ValidationError("password", it) }
                return UserRegistrationResult(
                    success = false,
                    validationResult = ValidationResult.Invalid(errors)
                )
            }

            // Check if username already exists
            val existingUserByUsername = userRepository.findByUsername(registration.username)
            if (existingUserByUsername != null) {
                return UserRegistrationResult(
                    success = false,
                    validationResult = ValidationResult.Invalid(listOf(ValidationError("username", "Username already exists")))
                )
            }

            // Check if email already exists
            val existingUserByEmail = userRepository.findByEmail(registration.email)
            if (existingUserByEmail != null) {
                return UserRegistrationResult(
                    success = false,
                    validationResult = ValidationResult.Invalid(listOf(ValidationError("email", "Email already exists")))
                )
            }

            // Check if person already has a user account
            val existingUserByPerson = userRepository.findByPersonId(registration.personId)
            if (existingUserByPerson != null) {
                return UserRegistrationResult(
                    success = false,
                    validationResult = ValidationResult.Invalid(listOf(ValidationError("personId", "Person already has a user account")))
                )
            }

            // Generate salt and hash password
            val salt = passwordService.generateSalt()
            val passwordHash = passwordService.hashPassword(registration.password, salt)

            // Create new user
            val newUser = DomUser(
                personId = registration.personId,
                username = registration.username,
                email = registration.email,
                passwordHash = passwordHash,
                salt = salt
            )

            val createdUser = userRepository.createUser(newUser)
            return UserRegistrationResult(
                success = true,
                user = createdUser,
                validationResult = ValidationResult.Valid,
                message = "User registered successfully"
            )

        } catch (e: Exception) {
            return UserRegistrationResult(
                success = false,
                validationResult = ValidationResult.Invalid(listOf(ValidationError("general", "Registration failed: ${e.message}"))),
                message = "Registration failed: ${e.message}"
            )
        }
    }

    /**
     * Data class for password change result.
     */
    data class PasswordChangeResult(
        val success: Boolean,
        val validationResult: ValidationResult,
        val message: String? = null
    )

    /**
     * Changes a user's password.
     *
     * @param userId The user ID
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return PasswordChangeResult indicating success or failure
     */
    suspend fun changePassword(userId: Uuid, currentPassword: String, newPassword: String): PasswordChangeResult {
        try {
            val user = userRepository.findById(userId)
                ?: return PasswordChangeResult(
                    success = false,
                    validationResult = ValidationResult.Invalid(listOf(ValidationError("userId", "User not found")))
                )

            // Verify current password
            if (!passwordService.verifyPassword(currentPassword, user.passwordHash, user.salt)) {
                return PasswordChangeResult(
                    success = false,
                    validationResult = ValidationResult.Invalid(listOf(ValidationError("currentPassword", "Current password is incorrect")))
                )
            }

            // Validate new password strength
            val passwordErrors = passwordService.getPasswordValidationErrors(newPassword)
            if (passwordErrors.isNotEmpty()) {
                val errors = passwordErrors.map { ValidationError("newPassword", it) }
                return PasswordChangeResult(
                    success = false,
                    validationResult = ValidationResult.Invalid(errors)
                )
            }

            // Generate new salt and hash new password
            val newSalt = passwordService.generateSalt()
            val newPasswordHash = passwordService.hashPassword(newPassword, newSalt)

            // Update password in database
            userRepository.updatePassword(userId, newPasswordHash, newSalt)

            return PasswordChangeResult(
                success = true,
                validationResult = ValidationResult.Valid,
                message = "Password changed successfully"
            )

        } catch (e: Exception) {
            return PasswordChangeResult(
                success = false,
                validationResult = ValidationResult.Invalid(listOf(ValidationError("general", "Password change failed: ${e.message}"))),
                message = "Password change failed: ${e.message}"
            )
        }
    }

    /**
     * Finds a user by username or email.
     */
    private suspend fun findUserByUsernameOrEmail(usernameOrEmail: String): DomUser? {
        return userRepository.findByUsername(usernameOrEmail)
            ?: userRepository.findByEmail(usernameOrEmail)
    }

    /**
     * Checks if a user is currently locked.
     */
    private fun isUserLocked(user: DomUser): Boolean {
        val lockUntil = user.gesperrtBis ?: return false
        return Clock.System.now() < lockUntil
    }

    /**
     * Validates a JWT token and returns the associated user.
     *
     * @param token The JWT token to validate
     * @return DomUser if token is valid and user exists, null otherwise
     */
    suspend fun validateJwtToken(token: String): DomUser? {
        val payload = jwtService.validateToken(token) ?: return null
        return userRepository.findById(payload.userId)
    }

    /**
     * Refreshes a JWT token.
     *
     * @param token The current JWT token
     * @return New token string if refresh is successful, null otherwise
     */
    fun refreshJwtToken(token: String): String? {
        val tokenInfo = jwtService.refreshToken(token) ?: return null
        return tokenInfo.token
    }

    /**
     * Extracts user ID from a JWT token without full validation.
     *
     * @param token The JWT token
     * @return User ID if extractable, null otherwise
     */
    fun extractUserIdFromToken(token: String): Uuid? {
        return jwtService.extractUserId(token)
    }
}
