package at.mocode.core.utils.validation

import kotlinx.serialization.Serializable

/**
 * Repräsentiert das Ergebnis einer Validierungsoperation als versiegelte Klasse.
 * Stellt sicher, dass ein Ergebnis entweder 'Valid' oder 'Invalid' ist.
 */
@Serializable
sealed class ValidationResult {
    /**
     * Repräsentiert eine erfolgreiche Validierung.
     */
    @Serializable
    object Valid : ValidationResult()

    /**
     * Repräsentiert eine fehlgeschlagene Validierung mit einer Liste von spezifischen Fehlern.
     */
    @Serializable
    data class Invalid(val errors: List<ValidationError>) : ValidationResult()

    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid
}

/**
 * Repräsentiert einen einzelnen Validierungsfehler.
 *
 * @param field Das Feld, dessen Validierung fehlschlug.
 * @param message Eine menschenlesbare Fehlermeldung.
 * @param code Ein maschinenlesbarer Fehlercode für Clients.
 */
@Serializable
data class ValidationError(
    val field: String,
    val message: String,
    val code: String? = null
)

/**
 * Eine Exception, die eine fehlgeschlagene Validierung repräsentiert.
 * Kann von zentralen Fehlerbehandlungs-Mechanismen abgefangen werden.
 */
class ValidationException(
    val validationResult: ValidationResult.Invalid
) : IllegalArgumentException(
    "Validation failed: ${validationResult.errors.joinToString { "${it.field}: ${it.message}" }}"
)
