package at.mocode.validation

import at.mocode.stammdaten.Person

/**
 * Validator for Person objects
 */
object PersonValidator {

    /**
     * Validates a Person object and returns validation result
     */
    fun validate(person: Person): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Required fields validation
        ValidationUtils.validateNotBlank(person.vorname, "vorname")?.let { errors.add(it) }
        ValidationUtils.validateNotBlank(person.nachname, "nachname")?.let { errors.add(it) }

        // Length validations
        ValidationUtils.validateLength(person.vorname, "vorname", 100, 1)?.let { errors.add(it) }
        ValidationUtils.validateLength(person.nachname, "nachname", 100, 1)?.let { errors.add(it) }
        ValidationUtils.validateLength(person.titel, "titel", 50)?.let { errors.add(it) }
        ValidationUtils.validateLength(person.adresse, "adresse", 500)?.let { errors.add(it) }
        ValidationUtils.validateLength(person.ort, "ort", 100)?.let { errors.add(it) }
        ValidationUtils.validateLength(person.mitgliedsNummerIntern, "mitgliedsNummerIntern", 50)?.let { errors.add(it) }
        ValidationUtils.validateLength(person.feiId, "feiId", 50)?.let { errors.add(it) }
        ValidationUtils.validateLength(person.sperrGrund, "sperrGrund", 500)?.let { errors.add(it) }

        // Format validations
        ValidationUtils.validateEmail(person.email)?.let { errors.add(it) }
        ValidationUtils.validatePhoneNumber(person.telefon)?.let { errors.add(it) }
        ValidationUtils.validatePostalCode(person.plz)?.let { errors.add(it) }
        ValidationUtils.validateCountryCode(person.nationalitaet)?.let { errors.add(it) }
        ValidationUtils.validateOepsSatzNr(person.oepsSatzNr)?.let { errors.add(it) }

        // Date validations
        ValidationUtils.validateBirthDate(person.geburtsdatum)?.let { errors.add(it) }
        ValidationUtils.validateYear(person.letzteZahlungJahr, "letzteZahlungJahr", 1990)?.let { errors.add(it) }

        // Business logic validations
        validateBusinessRules(person)?.let { errors.addAll(it) }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates business-specific rules for Person
     */
    private fun validateBusinessRules(person: Person): List<ValidationError>? {
        val errors = mutableListOf<ValidationError>()

        // If person is blocked, there must be a reason
        if (person.istGesperrt && person.sperrGrund.isNullOrBlank()) {
            errors.add(ValidationError(
                "sperrGrund",
                "Block reason is required when person is blocked",
                "REQUIRED_WHEN_BLOCKED"
            ))
        }

        // If person is not blocked, there shouldn't be a block reason
        if (!person.istGesperrt && !person.sperrGrund.isNullOrBlank()) {
            errors.add(ValidationError(
                "sperrGrund",
                "Block reason should be empty when person is not blocked",
                "INVALID_WHEN_NOT_BLOCKED"
            ))
        }

        // Email is required for active persons (business rule example)
        if (person.istAktiv && person.email.isNullOrBlank()) {
            errors.add(ValidationError(
                "email",
                "Email is required for active persons",
                "REQUIRED_FOR_ACTIVE"
            ))
        }

        // Validate license information consistency
        person.lizenzen.forEachIndexed { index, lizenz ->
            // Validate license level if provided
            lizenz.stufe?.let { stufe ->
                if (stufe.isBlank()) {
                    errors.add(ValidationError(
                        "lizenzen[$index].stufe",
                        "License level cannot be blank if provided",
                        "REQUIRED"
                    ))
                }
                if (stufe.length > 50) {
                    errors.add(ValidationError(
                        "lizenzen[$index].stufe",
                        "License level cannot exceed 50 characters",
                        "MAX_LENGTH"
                    ))
                }
            }

            // Validate license validity year
            lizenz.gueltigBisJahr?.let { jahr ->
                ValidationUtils.validateYear(jahr, "lizenzen[$index].gueltigBisJahr", 2000)?.let {
                    errors.add(it)
                }
            }
        }

        return if (errors.isEmpty()) null else errors
    }

    /**
     * Validates a Person and throws ValidationException if invalid
     */
    fun validateAndThrow(person: Person) {
        val result = validate(person)
        if (result.isInvalid()) {
            throw ValidationException(result as ValidationResult.Invalid)
        }
    }

    /**
     * Quick validation check - returns true if valid
     */
    fun isValid(person: Person): Boolean {
        return validate(person).isValid()
    }
}
