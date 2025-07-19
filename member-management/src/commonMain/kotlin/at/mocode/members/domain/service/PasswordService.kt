package at.mocode.members.domain.service

/**
 * Service for password hashing and verification.
 * Platform-specific implementation required for secure password handling.
 */
expect class PasswordService {
    fun generateSalt(): String
    fun hashPassword(password: String, salt: String): String
    fun verifyPassword(inputPassword: String, storedHash: String, storedSalt: String): Boolean
    fun generateRandomPassword(length: Int = 16): String
    fun checkPasswordStrength(password: String): PasswordStrength
}

/**
 * Contains information about password strength.
 */
data class PasswordStrength(
    val strength: Strength,
    val score: Int,
    val maxScore: Int,
    val issues: List<String>
) {
    enum class Strength {
        WEAK, MEDIUM, STRONG
    }
}
