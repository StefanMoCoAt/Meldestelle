package at.mocode.core.utils.validation

/**
 * Represents a single validation error.
 * @param field The name of the field that failed validation.
 * @param message A user-friendly error message.
 */
data class ValidationError(
    val field: String,
    val message: String
)

/**
 * Represents the result of a validation process.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    companion object {
        fun valid() = ValidationResult(true)
        fun invalid(errors: List<ValidationError>) = ValidationResult(false, errors)
        fun invalid(field: String, message: String) = ValidationResult(false, listOf(ValidationError(field, message)))
    }
}
