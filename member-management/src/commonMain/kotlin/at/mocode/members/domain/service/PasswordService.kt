package at.mocode.members.domain.service

import kotlin.random.Random

/**
 * Service for password hashing and verification.
 *
 * Provides secure password hashing using salt and verification methods.
 * This is a simplified implementation - in production, consider using
 * more robust hashing algorithms like bcrypt, scrypt, or Argon2.
 */
class PasswordService {

    companion object {
        private const val SALT_LENGTH = 32
    }

    /**
     * Generates a random salt for password hashing.
     *
     * @return A random salt string
     */
    fun generateSalt(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..SALT_LENGTH)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    /**
     * Hashes a password with the given salt.
     *
     * @param password The plain text password
     * @param salt The salt to use for hashing
     * @return The hashed password
     */
    fun hashPassword(password: String, salt: String): String {
        // Simple hash implementation - in production use bcrypt, scrypt, or Argon2
        val combined = password + salt
        return combined.hashCode().toString() + salt.hashCode().toString()
    }

    /**
     * Verifies a password against a stored hash and salt.
     *
     * @param password The plain text password to verify
     * @param storedHash The stored password hash
     * @param salt The salt used for the stored hash
     * @return True if the password matches, false otherwise
     */
    fun verifyPassword(password: String, storedHash: String, salt: String): Boolean {
        val hashedInput = hashPassword(password, salt)
        return hashedInput == storedHash
    }

    /**
     * Validates password strength.
     *
     * @param password The password to validate
     * @return True if the password meets minimum requirements
     */
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }

    /**
     * Gets password validation error messages.
     *
     * @param password The password to validate
     * @return List of validation error messages, empty if valid
     */
    fun getPasswordValidationErrors(password: String): List<String> {
        val errors = mutableListOf<String>()

        if (password.length < 8) {
            errors.add("Password must be at least 8 characters long")
        }

        if (!password.any { it.isUpperCase() }) {
            errors.add("Password must contain at least one uppercase letter")
        }

        if (!password.any { it.isLowerCase() }) {
            errors.add("Password must contain at least one lowercase letter")
        }

        if (!password.any { it.isDigit() }) {
            errors.add("Password must contain at least one digit")
        }

        return errors
    }
}
