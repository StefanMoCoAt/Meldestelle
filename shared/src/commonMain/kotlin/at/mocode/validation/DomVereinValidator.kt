package at.mocode.validation

import at.mocode.model.domaene.DomVerein

/**
 * Validator for DomVerein objects
 */
object DomVereinValidator {

    /**
     * Validates a DomVerein object and returns validation result
     */
    fun validate(verein: DomVerein): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Required fields validation
        ValidationUtils.validateNotBlank(verein.name, "name")?.let { errors.add(it) }

        // Length validations
        ValidationUtils.validateLength(verein.name, "name", 100, 1)?.let { errors.add(it) }
        ValidationUtils.validateLength(verein.kuerzel, "kuerzel", 20)?.let { errors.add(it) }
        ValidationUtils.validateLength(verein.adresseStrasse, "adresseStrasse", 200)?.let { errors.add(it) }
        ValidationUtils.validateLength(verein.ort, "ort", 100)?.let { errors.add(it) }
        ValidationUtils.validateLength(verein.webseiteUrl, "webseiteUrl", 255)?.let { errors.add(it) }
        ValidationUtils.validateLength(verein.notizenIntern, "notizenIntern", 1000)?.let { errors.add(it) }

        // Format validations
        ValidationUtils.validateEmail(verein.emailAllgemein, "emailAllgemein")?.let { errors.add(it) }
        ValidationUtils.validatePhoneNumber(verein.telefonAllgemein, "telefonAllgemein")?.let { errors.add(it) }
        ValidationUtils.validatePostalCode(verein.plz, "plz")?.let { errors.add(it) }

        // OEPS Vereinsnummer validation (4-digit number)
        verein.oepsVereinsNr?.let { oepsNr ->
            if (oepsNr.isNotBlank()) {
                if (oepsNr.length != 4 || !oepsNr.all { it.isDigit() }) {
                    errors.add(ValidationError(
                        "oepsVereinsNr",
                        "OEPS Vereinsnummer must be exactly 4 digits",
                        "INVALID_FORMAT"
                    ))
                }
            }
        }

        // Website URL validation
        verein.webseiteUrl?.let { url ->
            if (url.isNotBlank()) {
                val urlRegex = "^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$".toRegex()
                if (!urlRegex.matches(url)) {
                    errors.add(ValidationError(
                        "webseiteUrl",
                        "Invalid website URL format",
                        "INVALID_FORMAT"
                    ))
                }
            }
        }

        // Business logic validations
        validateBusinessRules(verein)?.let { errors.addAll(it) }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates business-specific rules for DomVerein
     */
    private fun validateBusinessRules(verein: DomVerein): List<ValidationError>? {
        val errors = mutableListOf<ValidationError>()

        // OEPS clubs should have OEPS number
        if (verein.datenQuelle.name.contains("OEPS") && verein.oepsVereinsNr.isNullOrBlank()) {
            errors.add(ValidationError(
                "oepsVereinsNr",
                "OEPS clubs should have OEPS Vereinsnummer",
                "REQUIRED_FOR_OEPS"
            ))
        }

        return if (errors.isEmpty()) null else errors
    }

    /**
     * Validates a DomVerein and throws ValidationException if invalid
     */
    fun validateAndThrow(verein: DomVerein) {
        val result = validate(verein)
        if (result.isInvalid()) {
            throw ValidationException(result as ValidationResult.Invalid)
        }
    }

    /**
     * Quick validation check - returns true if valid
     */
    fun isValid(verein: DomVerein): Boolean {
        return validate(verein).isValid()
    }
}
