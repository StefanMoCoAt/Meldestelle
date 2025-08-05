package at.mocode.core.utils.validation

/**
 * API-specific validation utilities for all modules.
 */
object ApiValidationUtils {

    /**
     * Validates query parameters with common validation rules
     */
    fun validateQueryParameters(
        limit: String? = null,
        offset: String? = null,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // Validate limit parameter
        limit?.let { limitStr ->
            try {
                val limitValue = limitStr.toInt()
                if (limitValue < 1 || limitValue > 1000) {
                    errors.add(ValidationError("limit", "Limit must be between 1 and 1000", "INVALID_RANGE"))
                }
            } catch (_: NumberFormatException) {
                errors.add(ValidationError("limit", "Limit must be a valid integer", "INVALID_FORMAT"))
            }
        }

        // Validate offset parameter
        offset?.let { offsetStr ->
            try {
                val offsetValue = offsetStr.toInt()
                if (offsetValue < 0) {
                    errors.add(ValidationError("offset", "Offset must be non-negative", "INVALID_RANGE"))
                }
            } catch (_: NumberFormatException) {
                errors.add(ValidationError("offset", "Offset must be a valid integer", "INVALID_FORMAT"))
            }
        }

        return errors
    }

    /**
     * Validates authentication request data
     */
    fun validateLoginRequest(username: String?, password: String?): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        ValidationUtils.validateNotBlank(username, "username")?.let { errors.add(it) }
        ValidationUtils.validateNotBlank(password, "password")?.let { errors.add(it) }

        username?.let {
            ValidationUtils.validateLength(it, "username", 50, 3)?.let { error -> errors.add(error) }
            if (it.contains("@")) {
                ValidationUtils.validateEmail(it, "username")?.let { error -> errors.add(error) }
            }
        }

        password?.let {
            ValidationUtils.validateLength(it, "password", 128, 8)?.let { error -> errors.add(error) }
        }

        return errors
    }
}
