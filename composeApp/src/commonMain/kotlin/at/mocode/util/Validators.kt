package at.mocode.util

/**
 * Validates if the given string is a valid email address
 *
 * @param email The email address to validate
 * @return true if the email is valid, false otherwise
 */
fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return false
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.matches(emailRegex.toRegex())
}
