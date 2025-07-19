package at.mocode.validation

import at.mocode.dto.base.ApiResponse
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import kotlinx.datetime.LocalDate

/**
 * API-specific validation utilities for all modules.
 * Provides comprehensive validation for all API endpoints.
 */
object ApiValidationUtils {

    /**
     * Validates UUID string and returns UUID or null if invalid
     */
    fun validateUuidString(uuidString: String?): Uuid? {
        if (uuidString.isNullOrBlank()) return null

        return try {
            uuidFrom(uuidString)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Validates query parameters with common validation rules
     */
    fun validateQueryParameters(
        limit: String? = null,
        offset: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        search: String? = null,
        q: String? = null
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // Validate limit parameter
        limit?.let { limitStr ->
            try {
                val limitValue = limitStr.toInt()
                if (limitValue < 1 || limitValue > 1000) {
                    errors.add(ValidationError("limit", "Limit must be between 1 and 1000", "INVALID_RANGE"))
                }
            } catch (e: NumberFormatException) {
                errors.add(ValidationError("limit", "Limit must be a valid integer", "INVALID_FORMAT"))
            }
        }

        // Validate offset parameter
        offset?.let { offsetStr ->
            try {
                val offsetValue = offsetStr.toInt()
                if (offsetValue < 0) {
                    errors.add(ValidationError("offset", "Offset must be non-negative", "INVALID_RANGE"))
                }
            } catch (e: NumberFormatException) {
                errors.add(ValidationError("offset", "Offset must be a valid integer", "INVALID_FORMAT"))
            }
        }

        // Validate date parameters
        startDate?.let { dateStr ->
            try {
                LocalDate.parse(dateStr)
            } catch (e: Exception) {
                errors.add(ValidationError("startDate", "Invalid date format. Use YYYY-MM-DD", "INVALID_FORMAT"))
            }
        }

        endDate?.let { dateStr ->
            try {
                LocalDate.parse(dateStr)
            } catch (e: Exception) {
                errors.add(ValidationError("endDate", "Invalid date format. Use YYYY-MM-DD", "INVALID_FORMAT"))
            }
        }

        // Validate search term length
        search?.let { searchTerm ->
            ValidationUtils.validateLength(searchTerm, "search", 100, 2)?.let { error ->
                errors.add(error)
            }
        }

        q?.let { searchTerm ->
            ValidationUtils.validateLength(searchTerm, "q", 100, 2)?.let { error ->
                errors.add(error)
            }
        }

        return errors
    }

    /**
     * Validates authentication request data
     */
    fun validateLoginRequest(username: String?, password: String?): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        ValidationUtils.validateNotBlank(username, "username")?.let { errors.add(it) }
        ValidationUtils.validateNotBlank(password, "password")?.let { errors.add(it) }

        username?.let {
            ValidationUtils.validateLength(it, "username", 50, 3)?.let { error -> errors.add(error) }
            // Check if it's an email format
            if (it.contains("@")) {
                ValidationUtils.validateEmail(it, "username")?.let { error -> errors.add(error) }
            }
        }

        password?.let {
            ValidationUtils.validateLength(it, "password", 128, 8)?.let { error -> errors.add(error) }
        }

        return errors
    }

    /**
     * Validates password change request data
     */
    fun validateChangePasswordRequest(
        currentPassword: String?,
        newPassword: String?,
        confirmPassword: String?
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        ValidationUtils.validateNotBlank(currentPassword, "currentPassword")?.let { errors.add(it) }
        ValidationUtils.validateNotBlank(newPassword, "newPassword")?.let { errors.add(it) }
        ValidationUtils.validateNotBlank(confirmPassword, "confirmPassword")?.let { errors.add(it) }

        newPassword?.let {
            ValidationUtils.validateLength(it, "newPassword", 128, 8)?.let { error -> errors.add(error) }

            // Password strength validation
            if (!it.any { char -> char.isUpperCase() }) {
                errors.add(ValidationError("newPassword", "Password must contain at least one uppercase letter", "WEAK_PASSWORD"))
            }
            if (!it.any { char -> char.isLowerCase() }) {
                errors.add(ValidationError("newPassword", "Password must contain at least one lowercase letter", "WEAK_PASSWORD"))
            }
            if (!it.any { char -> char.isDigit() }) {
                errors.add(ValidationError("newPassword", "Password must contain at least one digit", "WEAK_PASSWORD"))
            }
        }

        if (newPassword != null && confirmPassword != null && newPassword != confirmPassword) {
            errors.add(ValidationError("confirmPassword", "Password confirmation does not match", "MISMATCH"))
        }

        return errors
    }

    /**
     * Validates country creation/update request
     */
    fun validateCountryRequest(
        isoAlpha2Code: String?,
        isoAlpha3Code: String?,
        nameDeutsch: String?,
        nameEnglisch: String?
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        ValidationUtils.validateNotBlank(isoAlpha2Code, "isoAlpha2Code")?.let { errors.add(it) }
        ValidationUtils.validateNotBlank(isoAlpha3Code, "isoAlpha3Code")?.let { errors.add(it) }
        ValidationUtils.validateNotBlank(nameDeutsch, "nameDeutsch")?.let { errors.add(it) }

        isoAlpha2Code?.let {
            if (it.length != 2 || !it.all { char -> char.isLetter() }) {
                errors.add(ValidationError("isoAlpha2Code", "ISO Alpha-2 code must be exactly 2 letters", "INVALID_FORMAT"))
            }
        }

        isoAlpha3Code?.let {
            if (it.length != 3 || !it.all { char -> char.isLetter() }) {
                errors.add(ValidationError("isoAlpha3Code", "ISO Alpha-3 code must be exactly 3 letters", "INVALID_FORMAT"))
            }
        }

        nameDeutsch?.let {
            ValidationUtils.validateLength(it, "nameDeutsch", 100, 2)?.let { error -> errors.add(error) }
        }

        nameEnglisch?.let {
            ValidationUtils.validateLength(it, "nameEnglisch", 100, 2)?.let { error -> errors.add(error) }
        }

        return errors
    }

    /**
     * Validates horse creation/update request
     */
    fun validateHorseRequest(
        pferdeName: String?,
        lebensnummer: String?,
        chipNummer: String?,
        oepsNummer: String?,
        feiNummer: String?
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        ValidationUtils.validateNotBlank(pferdeName, "pferdeName")?.let { errors.add(it) }

        pferdeName?.let {
            ValidationUtils.validateLength(it, "pferdeName", 100, 2)?.let { error -> errors.add(error) }
        }

        lebensnummer?.let {
            ValidationUtils.validateLength(it, "lebensnummer", 20, 5)?.let { error -> errors.add(error) }
        }

        chipNummer?.let {
            ValidationUtils.validateLength(it, "chipNummer", 20, 10)?.let { error -> errors.add(error) }
        }

        oepsNummer?.let {
            ValidationUtils.validateOepsSatzNr(it, "oepsNummer")?.let { error -> errors.add(error) }
        }

        feiNummer?.let {
            ValidationUtils.validateLength(it, "feiNummer", 20, 5)?.let { error -> errors.add(error) }
        }

        return errors
    }

    /**
     * Validates event creation/update request
     */
    fun validateEventRequest(
        name: String?,
        ort: String?,
        startDatum: LocalDate?,
        endDatum: LocalDate?,
        maxTeilnehmer: Int?
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        ValidationUtils.validateNotBlank(name, "name")?.let { errors.add(it) }
        ValidationUtils.validateNotBlank(ort, "ort")?.let { errors.add(it) }

        name?.let {
            ValidationUtils.validateLength(it, "name", 200, 3)?.let { error -> errors.add(error) }
        }

        ort?.let {
            ValidationUtils.validateLength(it, "ort", 100, 2)?.let { error -> errors.add(error) }
        }

        if (startDatum != null && endDatum != null && startDatum > endDatum) {
            errors.add(ValidationError("endDatum", "End date must be after start date", "INVALID_DATE_RANGE"))
        }

        maxTeilnehmer?.let {
            if (it < 1 || it > 10000) {
                errors.add(ValidationError("maxTeilnehmer", "Maximum participants must be between 1 and 10000", "INVALID_RANGE"))
            }
        }

        return errors
    }

    /**
     * Creates error messages from validation errors
     */
    fun createErrorMessage(errors: List<ValidationError>): String {
        val errorMessages = errors.map { "${it.field}: ${it.message}" }
        return "Validation failed: ${errorMessages.joinToString(", ")}"
    }

    /**
     * Checks if validation passed
     */
    fun isValid(errors: List<ValidationError>): Boolean {
        return errors.isEmpty()
    }
}
