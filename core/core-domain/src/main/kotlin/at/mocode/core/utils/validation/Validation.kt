package at.mocode.core.utils.validation

/**
 * Represents a single validation error.
 * @param field The name of the field that failed validation.
 * @param message A user-friendly error message.
 * @param code A machine-readable error code for the client.
 */
data class ValidationError(
    val field: String,
    val message: String,
    val code: String? = null
)

/**
 * Represents the result of a validation process as a sealed class.
 * This ensures that a result is either Valid or Invalid, but never both.
 */
sealed class ValidationResult {
    /**
     * Represents a successful validation.
     */
    object Valid : ValidationResult()

    /**
     * Represents a failed validation with a list of specific errors.
     */
    data class Invalid(val errors: List<ValidationError>) : ValidationResult()

    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid

    companion object {
        fun invalid(field: String, message: String, code: String? = null): Invalid {
            return Invalid(listOf(ValidationError(field, message, code)))
        }
    }
}

/**
 * An exception that can be thrown to represent validation failure,
 * allowing it to be caught by centralized error handling (like Ktor StatusPages).
 */
class ValidationException(
    val validationResult: ValidationResult.Invalid
) : IllegalArgumentException(
    "Validation failed: ${validationResult.errors.joinToString { "${it.field}: ${it.message}" }}"
)
