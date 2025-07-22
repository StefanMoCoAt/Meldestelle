package at.mocode.core.utils.validation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Common validation utilities
 */
object ValidationUtils {

    /**
     * Validates that a string is not blank
     */
    fun validateNotBlank(value: String?, fieldName: String): ValidationError? {
        return if (value.isNullOrBlank()) {
            ValidationError(fieldName, "$fieldName cannot be blank", "REQUIRED")
        } else null
    }

    /**
     * Validates string length
     */
    fun validateLength(value: String?, fieldName: String, maxLength: Int, minLength: Int = 0): ValidationError? {
        if (value == null) return null

        return when {
            value.length < minLength -> ValidationError(
                fieldName,
                "$fieldName must be at least $minLength characters long",
                "MIN_LENGTH"
            )
            value.length > maxLength -> ValidationError(
                fieldName,
                "$fieldName cannot exceed $maxLength characters",
                "MAX_LENGTH"
            )
            else -> null
        }
    }

    /**
     * Validates email format
     */
    fun validateEmail(email: String?, fieldName: String = "email"): ValidationError? {
        if (email.isNullOrBlank()) return null

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return if (!emailRegex.matches(email)) {
            ValidationError(fieldName, "Invalid email format", "INVALID_FORMAT")
        } else null
    }

    /**
     * Validates phone number format (basic validation)
     */
    fun validatePhoneNumber(phone: String?, fieldName: String = "telefon"): ValidationError? {
        if (phone.isNullOrBlank()) return null

        // Remove common separators and spaces
        val cleanPhone = phone.replace(Regex("[\\s\\-\\(\\)\\+]"), "")

        return if (cleanPhone.length < 6 || cleanPhone.length > 20 || !cleanPhone.all { it.isDigit() }) {
            ValidationError(fieldName, "Invalid phone number format", "INVALID_FORMAT")
        } else null
    }

    /**
     * Validates postal code format (basic validation for various countries)
     */
    fun validatePostalCode(postalCode: String?, fieldName: String = "plz"): ValidationError? {
        if (postalCode.isNullOrBlank()) return null

        // Basic validation: 3-10 alphanumeric characters
        return if (postalCode.length < 3 || postalCode.length > 10 || !postalCode.all { it.isLetterOrDigit() }) {
            ValidationError(fieldName, "Invalid postal code format", "INVALID_FORMAT")
        } else null
    }

    /**
     * Validates 3-letter country code
     */
    fun validateCountryCode(countryCode: String?, fieldName: String = "nationalitaet"): ValidationError? {
        if (countryCode.isNullOrBlank()) return null

        return if (countryCode.length != 3 || !countryCode.all { it.isLetter() }) {
            ValidationError(fieldName, "Country code must be exactly 3 letters", "INVALID_FORMAT")
        } else null
    }

    /**
     * Validates birth date
     */
    fun validateBirthDate(birthDate: LocalDate?, fieldName: String = "geburtsdatum"): ValidationError? {
        if (birthDate == null) return null

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val minDate = LocalDate(1900, 1, 1)

        return when {
            birthDate > today -> ValidationError(
                fieldName,
                "Birth date cannot be in the future",
                "FUTURE_DATE"
            )
            birthDate < minDate -> ValidationError(
                fieldName,
                "Birth date cannot be before year 1900",
                "INVALID_DATE"
            )
            else -> null
        }
    }

    /**
     * Validates year value
     */
    fun validateYear(year: Int?, fieldName: String, minYear: Int = 1900): ValidationError? {
        if (year == null) return null

        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

        return when {
            year < minYear -> ValidationError(
                fieldName,
                "Year cannot be before $minYear",
                "INVALID_YEAR"
            )
            year > currentYear + 10 -> ValidationError(
                fieldName,
                "Year cannot be more than 10 years in the future",
                "FUTURE_YEAR"
            )
            else -> null
        }
    }

    /**
     * Validates OEPS Satz number format (Austrian specific)
     */
    fun validateOepsSatzNr(oepsSatzNr: String?, fieldName: String = "oepsSatzNr"): ValidationError? {
        if (oepsSatzNr.isNullOrBlank()) return null

        // Basic validation: should be numeric and reasonable length
        return if (oepsSatzNr.length < 3 || oepsSatzNr.length > 20 || !oepsSatzNr.all { it.isDigit() }) {
            ValidationError(fieldName, "Invalid OEPS Satz number format", "INVALID_FORMAT")
        } else null
    }
}
