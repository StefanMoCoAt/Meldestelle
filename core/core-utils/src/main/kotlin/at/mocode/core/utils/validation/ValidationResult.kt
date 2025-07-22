package at.mocode.core.utils.validation

import kotlinx.serialization.Serializable

/**
 * Represents the result of a validation operation
 */
@Serializable
sealed class ValidationResult {
    @Serializable
    object Valid : ValidationResult()

    @Serializable
    data class Invalid(val errors: List<ValidationError>) : ValidationResult()

    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid
}

/**
 * Represents a single validation error
 */
@Serializable
data class ValidationError(
    val field: String,
    val message: String,
    val code: String? = null
)

/**
 * Exception thrown when validation fails
 */
class ValidationException(
    val validationResult: ValidationResult.Invalid
) : IllegalArgumentException(
    "Validation failed: ${validationResult.errors.joinToString(", ") { "${it.field}: ${it.message}" }}"
)
