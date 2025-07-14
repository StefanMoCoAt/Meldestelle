package at.mocode.validation

import at.mocode.model.domaene.DomPferd
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Validator for DomPferd objects
 */
object DomPferdValidator {

    /**
     * Validates a DomPferd object and returns validation result
     */
    fun validate(pferd: DomPferd): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Required fields validation
        ValidationUtils.validateNotBlank(pferd.name, "name")?.let { errors.add(it) }

        // Length validations
        ValidationUtils.validateLength(pferd.name, "name", 100, 1)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.lebensnummer, "lebensnummer", 20)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.feiPassNr, "feiPassNr", 20)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.farbe, "farbe", 50)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.rasse, "rasse", 100)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.abstammungVaterName, "abstammungVaterName", 100)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.abstammungMutterName, "abstammungMutterName", 100)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.abstammungMutterVaterName, "abstammungMutterVaterName", 100)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.abstammungZusatzInfo, "abstammungZusatzInfo", 500)?.let { errors.add(it) }
        ValidationUtils.validateLength(pferd.notizenIntern, "notizenIntern", 1000)?.let { errors.add(it) }

        // OEPS Satznummer validation (10-digit number)
        pferd.oepsSatzNrPferd?.let { oepsSatzNr ->
            if (oepsSatzNr.isNotBlank()) {
                if (oepsSatzNr.length != 10 || !oepsSatzNr.all { it.isDigit() }) {
                    errors.add(ValidationError(
                        "oepsSatzNrPferd",
                        "OEPS Satznummer must be exactly 10 digits",
                        "INVALID_FORMAT"
                    ))
                }
            }
        }

        // OEPS Kopfnummer validation (4-digit number)
        pferd.oepsKopfNr?.let { oepsKopfNr ->
            if (oepsKopfNr.isNotBlank()) {
                if (oepsKopfNr.length != 4 || !oepsKopfNr.all { it.isDigit() }) {
                    errors.add(ValidationError(
                        "oepsKopfNr",
                        "OEPS Kopfnummer must be exactly 4 digits",
                        "INVALID_FORMAT"
                    ))
                }
            }
        }

        // Lebensnummer validation (UELN format - basic validation)
        pferd.lebensnummer?.let { lebensnummer ->
            if (lebensnummer.isNotBlank()) {
                // UELN should be 15 characters: 3-letter country code + 12 digits
                if (lebensnummer.length != 15 ||
                    !lebensnummer.substring(0, 3).all { it.isLetter() } ||
                    !lebensnummer.substring(3).all { it.isDigit() }) {
                    errors.add(ValidationError(
                        "lebensnummer",
                        "Lebensnummer (UELN) must be 15 characters: 3 letters + 12 digits",
                        "INVALID_FORMAT"
                    ))
                }
            }
        }

        // Birth year validation
        pferd.geburtsjahr?.let { geburtsjahr ->
            ValidationUtils.validateYear(geburtsjahr, "geburtsjahr", 1950)?.let { errors.add(it) }
        }

        // Payment year validation
        pferd.letzteZahlungPferdegebuehrJahrOeps?.let { zahlungsjahr ->
            ValidationUtils.validateYear(zahlungsjahr, "letzteZahlungPferdegebuehrJahrOeps", 1990)?.let { errors.add(it) }
        }

        // Stockmaß validation (reasonable range for horses)
        pferd.stockmassCm?.let { stockmass ->
            if (stockmass < 80 || stockmass > 220) {
                errors.add(ValidationError(
                    "stockmassCm",
                    "Stockmaß must be between 80 and 220 cm",
                    "INVALID_RANGE"
                ))
            }
        }

        // Business logic validations
        validateBusinessRules(pferd)?.let { errors.addAll(it) }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates business-specific rules for DomPferd
     */
    private fun validateBusinessRules(pferd: DomPferd): List<ValidationError>? {
        val errors = mutableListOf<ValidationError>()

        // OEPS horses should have OEPS numbers
        if (pferd.datenQuelle.name.contains("OEPS") && pferd.oepsSatzNrPferd.isNullOrBlank()) {
            errors.add(ValidationError(
                "oepsSatzNrPferd",
                "OEPS horses should have OEPS Satznummer",
                "REQUIRED_FOR_OEPS"
            ))
        }

        // Active horses should have birth year
        if (pferd.istAktiv && pferd.geburtsjahr == null) {
            errors.add(ValidationError(
                "geburtsjahr",
                "Birth year is recommended for active horses",
                "RECOMMENDED_FOR_ACTIVE"
            ))
        }

        // Active horses should have gender
        if (pferd.istAktiv && pferd.geschlecht == null) {
            errors.add(ValidationError(
                "geschlecht",
                "Gender is recommended for active horses",
                "RECOMMENDED_FOR_ACTIVE"
            ))
        }

        // Horses with payment info should have birth year for age verification
        pferd.letzteZahlungPferdegebuehrJahrOeps?.let { zahlungsjahr ->
            pferd.geburtsjahr?.let { geburtsjahr ->
                val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
                val age = currentYear - geburtsjahr

                // Horses should be at least 3 years old for competition
                if (age < 3) {
                    errors.add(ValidationError(
                        "geburtsjahr",
                        "Horse appears to be too young for competition (under 3 years)",
                        "AGE_WARNING"
                    ))
                }

                // Warning for very old horses
                if (age > 30) {
                    errors.add(ValidationError(
                        "geburtsjahr",
                        "Horse appears to be very old (over 30 years)",
                        "AGE_WARNING"
                    ))
                }
            }
        }

        return if (errors.isEmpty()) null else errors
    }

    /**
     * Validates a DomPferd and throws ValidationException if invalid
     */
    fun validateAndThrow(pferd: DomPferd) {
        val result = validate(pferd)
        if (result.isInvalid()) {
            throw ValidationException(result as ValidationResult.Invalid)
        }
    }

    /**
     * Quick validation check - returns true if valid
     */
    fun isValid(pferd: DomPferd): Boolean {
        return validate(pferd).isValid()
    }
}
