package at.mocode.validation

import at.mocode.model.domaene.DomLizenz
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Validator for DomLizenz objects
 */
object DomLizenzValidator {

    /**
     * Validates a DomLizenz object and returns validation result
     */
    fun validate(lizenz: DomLizenz): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Length validations
        ValidationUtils.validateLength(lizenz.notiz, "notiz", 500)?.let { errors.add(it) }

        // Validity year validation
        lizenz.gueltigBisJahr?.let { gueltigBisJahr ->
            ValidationUtils.validateYear(gueltigBisJahr, "gueltigBisJahr", 2000)?.let { errors.add(it) }
        }

        // Issue date validation
        lizenz.ausgestelltAm?.let { ausgestelltAm ->
            ValidationUtils.validateBirthDate(ausgestelltAm, "ausgestelltAm")?.let { errors.add(it) }
        }

        // Business logic validations
        validateBusinessRules(lizenz)?.let { errors.addAll(it) }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates business-specific rules for DomLizenz
     */
    private fun validateBusinessRules(lizenz: DomLizenz): List<ValidationError>? {
        val errors = mutableListOf<ValidationError>()

        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

        // Active/paid licenses should have validity year
        if (lizenz.istAktivBezahltOeps && lizenz.gueltigBisJahr == null) {
            errors.add(ValidationError(
                "gueltigBisJahr",
                "Active/paid licenses should have validity year",
                "REQUIRED_FOR_ACTIVE"
            ))
        }

        // Validity year should not be too far in the past for active licenses
        lizenz.gueltigBisJahr?.let { gueltigBisJahr ->
            if (lizenz.istAktivBezahltOeps && gueltigBisJahr < currentYear - 1) {
                errors.add(ValidationError(
                    "gueltigBisJahr",
                    "Active license appears to be expired (validity year is more than 1 year in the past)",
                    "EXPIRED_LICENSE"
                ))
            }
        }

        // Issue date should not be in the future
        lizenz.ausgestelltAm?.let { ausgestelltAm ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            if (ausgestelltAm > today) {
                errors.add(ValidationError(
                    "ausgestelltAm",
                    "Issue date cannot be in the future",
                    "FUTURE_DATE"
                ))
            }
        }

        // Issue date and validity year consistency check
        lizenz.ausgestelltAm?.let { ausgestelltAm ->
            lizenz.gueltigBisJahr?.let { gueltigBisJahr ->
                val issueYear = ausgestelltAm.year

                // Validity year should be same or later than issue year
                if (gueltigBisJahr < issueYear) {
                    errors.add(ValidationError(
                        "gueltigBisJahr",
                        "Validity year cannot be earlier than issue year",
                        "INVALID_DATE_RANGE"
                    ))
                }

                // Validity year should not be too far from issue year (reasonable range)
                if (gueltigBisJahr > issueYear + 10) {
                    errors.add(ValidationError(
                        "gueltigBisJahr",
                        "Validity year seems too far from issue year (more than 10 years)",
                        "SUSPICIOUS_DATE_RANGE"
                    ))
                }
            }
        }

        // Inactive licenses should have a reason (note) if they were previously active
        if (!lizenz.istAktivBezahltOeps && lizenz.notiz.isNullOrBlank()) {
            // This is more of a recommendation than a hard error
            errors.add(ValidationError(
                "notiz",
                "Inactive licenses should have a note explaining the status",
                "RECOMMENDED_FOR_INACTIVE"
            ))
        }

        return if (errors.isEmpty()) null else errors
    }

    /**
     * Validates license expiry status
     */
    fun isLicenseExpired(lizenz: DomLizenz): Boolean {
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        return lizenz.gueltigBisJahr?.let { it < currentYear } ?: false
    }

    /**
     * Validates license validity for a specific year
     */
    fun isValidForYear(lizenz: DomLizenz, year: Int): Boolean {
        return lizenz.gueltigBisJahr?.let { it >= year } ?: false
    }

    /**
     * Validates a DomLizenz and throws ValidationException if invalid
     */
    fun validateAndThrow(lizenz: DomLizenz) {
        val result = validate(lizenz)
        if (result.isInvalid()) {
            throw ValidationException(result as ValidationResult.Invalid)
        }
    }

    /**
     * Quick validation check - returns true if valid
     */
    fun isValid(lizenz: DomLizenz): Boolean {
        return validate(lizenz).isValid()
    }

    /**
     * Validates multiple licenses for a person to check for conflicts
     */
    fun validateLicenseSet(lizenzen: List<DomLizenz>): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Check for duplicate license types for the same person and year
        val licenseTypeYearCombinations = mutableSetOf<Pair<String, Int?>>()

        lizenzen.forEachIndexed { index, lizenz ->
            val combination = Pair(lizenz.lizenzTypGlobalId.toString(), lizenz.gueltigBisJahr)

            if (combination in licenseTypeYearCombinations) {
                errors.add(ValidationError(
                    "lizenzen[$index]",
                    "Duplicate license type for the same validity year",
                    "DUPLICATE_LICENSE"
                ))
            } else {
                licenseTypeYearCombinations.add(combination)
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
