package at.mocode.validation

import at.mocode.model.domaene.DomQualifikation
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Validator for DomQualifikation objects
 */
object DomQualifikationValidator {

    /**
     * Validates a DomQualifikation object and returns validation result
     */
    fun validate(qualifikation: DomQualifikation): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Length validations
        ValidationUtils.validateLength(qualifikation.bemerkung, "bemerkung", 500)?.let { errors.add(it) }

        // Date validations - basic date range validation (not birth date validation)
        qualifikation.gueltigVon?.let { gueltigVon ->
            // Only check that it's not too far in the past (reasonable minimum date)
            val minDate = kotlinx.datetime.LocalDate(1900, 1, 1)
            if (gueltigVon < minDate) {
                errors.add(ValidationError(
                    "gueltigVon",
                    "Start date cannot be before year 1900",
                    "INVALID_DATE"
                ))
            }
        }

        qualifikation.gueltigBis?.let { gueltigBis ->
            // Only check that it's not too far in the past (reasonable minimum date)
            val minDate = kotlinx.datetime.LocalDate(1900, 1, 1)
            if (gueltigBis < minDate) {
                errors.add(ValidationError(
                    "gueltigBis",
                    "End date cannot be before year 1900",
                    "INVALID_DATE"
                ))
            }
        }

        // Business logic validations
        validateBusinessRules(qualifikation)?.let { errors.addAll(it) }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates business-specific rules for DomQualifikation
     */
    private fun validateBusinessRules(qualifikation: DomQualifikation): List<ValidationError>? {
        val errors = mutableListOf<ValidationError>()

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Validity date range consistency check
        qualifikation.gueltigVon?.let { gueltigVon ->
            qualifikation.gueltigBis?.let { gueltigBis ->
                if (gueltigBis < gueltigVon) {
                    errors.add(ValidationError(
                        "gueltigBis",
                        "End date cannot be earlier than start date",
                        "INVALID_DATE_RANGE"
                    ))
                }

                // Check for unreasonably long qualification periods
                val daysBetween = gueltigBis.toEpochDays() - gueltigVon.toEpochDays()
                if (daysBetween > 365 * 20) { // More than 20 years
                    errors.add(ValidationError(
                        "gueltigBis",
                        "Qualification validity period seems unreasonably long (more than 20 years)",
                        "SUSPICIOUS_DATE_RANGE"
                    ))
                }
            }
        }

        // Start date should not be in the future for active qualifications
        qualifikation.gueltigVon?.let { gueltigVon ->
            if (qualifikation.istAktiv && gueltigVon > today) {
                errors.add(ValidationError(
                    "gueltigVon",
                    "Start date cannot be in the future for active qualifications",
                    "FUTURE_DATE"
                ))
            }
        }

        // Active qualifications with end date should not be expired
        if (qualifikation.istAktiv) {
            qualifikation.gueltigBis?.let { gueltigBis ->
                if (gueltigBis < today) {
                    errors.add(ValidationError(
                        "istAktiv",
                        "Qualification appears to be expired but is marked as active",
                        "EXPIRED_QUALIFICATION"
                    ))
                }
            }
        }

        // Inactive qualifications should have a reason (note)
        if (!qualifikation.istAktiv && qualifikation.bemerkung.isNullOrBlank()) {
            errors.add(ValidationError(
                "bemerkung",
                "Inactive qualifications should have a note explaining the status",
                "RECOMMENDED_FOR_INACTIVE"
            ))
        }

        // Active qualifications should have start date
        if (qualifikation.istAktiv && qualifikation.gueltigVon == null) {
            errors.add(ValidationError(
                "gueltigVon",
                "Active qualifications should have a start date",
                "RECOMMENDED_FOR_ACTIVE"
            ))
        }

        return if (errors.isEmpty()) null else errors
    }

    /**
     * Validates qualification expiry status
     */
    fun isQualificationExpired(qualifikation: DomQualifikation): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return qualifikation.gueltigBis?.let { it < today } ?: false
    }

    /**
     * Validates qualification validity for a specific date
     */
    fun isValidForDate(qualifikation: DomQualifikation, date: kotlinx.datetime.LocalDate): Boolean {
        val validFrom = qualifikation.gueltigVon?.let { date >= it } ?: true
        val validUntil = qualifikation.gueltigBis?.let { date <= it } ?: true
        return validFrom && validUntil && qualifikation.istAktiv
    }

    /**
     * Validates qualification validity for today
     */
    fun isCurrentlyValid(qualifikation: DomQualifikation): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return isValidForDate(qualifikation, today)
    }

    /**
     * Validates a DomQualifikation and throws ValidationException if invalid
     */
    fun validateAndThrow(qualifikation: DomQualifikation) {
        val result = validate(qualifikation)
        if (result.isInvalid()) {
            throw ValidationException(result as ValidationResult.Invalid)
        }
    }

    /**
     * Quick validation check - returns true if valid
     */
    fun isValid(qualifikation: DomQualifikation): Boolean {
        return validate(qualifikation).isValid()
    }

    /**
     * Validates multiple qualifications for a person to check for conflicts
     */
    fun validateQualificationSet(qualifikationen: List<DomQualifikation>): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Check for overlapping active qualifications of the same type
        val activeQualifications = qualifikationen.filter { it.istAktiv }

        for (i in activeQualifications.indices) {
            for (j in i + 1 until activeQualifications.size) {
                val qual1 = activeQualifications[i]
                val qual2 = activeQualifications[j]

                // Same qualification type
                if (qual1.qualTypId == qual2.qualTypId) {
                    // Check for overlapping periods
                    val overlap = checkDateOverlap(
                        qual1.gueltigVon, qual1.gueltigBis,
                        qual2.gueltigVon, qual2.gueltigBis
                    )

                    if (overlap) {
                        errors.add(ValidationError(
                            "qualifikationen",
                            "Overlapping active qualifications of the same type found",
                            "OVERLAPPING_QUALIFICATIONS"
                        ))
                    }
                }
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Helper function to check if two date ranges overlap
     */
    private fun checkDateOverlap(
        start1: kotlinx.datetime.LocalDate?, end1: kotlinx.datetime.LocalDate?,
        start2: kotlinx.datetime.LocalDate?, end2: kotlinx.datetime.LocalDate?
    ): Boolean {
        // If any qualification has no dates, assume no overlap
        if (start1 == null && end1 == null) return false
        if (start2 == null && end2 == null) return false

        // Use very early/late dates for missing bounds
        val earlyDate = kotlinx.datetime.LocalDate(1900, 1, 1)
        val lateDate = kotlinx.datetime.LocalDate(2100, 12, 31)

        val actualStart1 = start1 ?: earlyDate
        val actualEnd1 = end1 ?: lateDate
        val actualStart2 = start2 ?: earlyDate
        val actualEnd2 = end2 ?: lateDate

        // Check if ranges overlap
        return actualStart1 <= actualEnd2 && actualStart2 <= actualEnd1
    }
}
