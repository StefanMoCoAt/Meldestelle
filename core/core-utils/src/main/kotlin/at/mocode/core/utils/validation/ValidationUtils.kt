package at.mocode.core.utils.validation

/**
 * Common validation utilities
 */
object ValidationUtils {

    /**
     * Validates that a string is not blank
     */
    fun validateNotBlank(value: String?, fieldName: String): ValidationError? {
        return if (value.isNullOrBlank()) {
            ValidationError(fieldName, "$fieldName cannot be blank", "REQUIRED")
        } else null
    }

    /**
     * Validates string length
     */
    fun validateLength(value: String?, fieldName: String, maxLength: Int, minLength: Int = 0): ValidationError? {
        if (value == null) return null

        return when {
            value.length < minLength -> ValidationError(
                fieldName,
                "$fieldName must be at least $minLength characters long",
                "MIN_LENGTH"
            )

            value.length > maxLength -> ValidationError(
                fieldName,
                "$fieldName cannot exceed $maxLength characters",
                "MAX_LENGTH"
            )

            else -> null
        }
    }

    /**
     * Validates email format
     */
    fun validateEmail(email: String?, fieldName: String = "email"): ValidationError? {
        if (email.isNullOrBlank()) return null

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return if (!emailRegex.matches(email)) {
            ValidationError(fieldName, "Invalid email format", "INVALID_FORMAT")
        } else null
    }
}
