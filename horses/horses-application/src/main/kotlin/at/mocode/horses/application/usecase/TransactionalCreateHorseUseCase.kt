package at.mocode.horses.application.usecase

import at.mocode.horses.domain.model.DomPferd
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.core.domain.model.PferdeGeschlechtE
import at.mocode.core.domain.model.DatenQuelleE
import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.core.utils.validation.ValidationResult
import at.mocode.core.utils.validation.ValidationError
import at.mocode.core.utils.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn

/**
 * Transactional version of CreateHorseUseCase that ensures all database operations
 * run within a single transaction to maintain data consistency.
 *
 * This use case handles the business logic for horse registration including
 * validation, uniqueness checks, and persistence - all within a single transaction.
 */
class TransactionalCreateHorseUseCase(
    private val horseRepository: HorseRepository
) {

    /**
     * Request data for creating a new horse.
     */
    data class CreateHorseRequest(
        val pferdeName: String,
        val geschlecht: PferdeGeschlechtE,
        val geburtsdatum: LocalDate? = null,
        val rasse: String? = null,
        val farbe: String? = null,
        val besitzerId: Uuid? = null,
        val verantwortlichePersonId: Uuid? = null,
        val zuechterName: String? = null,
        val zuchtbuchNummer: String? = null,
        val lebensnummer: String? = null,
        val chipNummer: String? = null,
        val passNummer: String? = null,
        val oepsNummer: String? = null,
        val feiNummer: String? = null,
        val vaterName: String? = null,
        val mutterName: String? = null,
        val mutterVaterName: String? = null,
        val stockmass: Int? = null,
        val bemerkungen: String? = null,
        val datenQuelle: DatenQuelleE = DatenQuelleE.MANUELL
    )

    /**
     * Executes the horse creation use case within a single transaction.
     *
     * @param request The horse creation request data
     * @return ApiResponse with the created horse or validation errors
     */
    suspend fun execute(request: CreateHorseRequest): ApiResponse<DomPferd> {
        println("[DEBUG_LOG] TransactionalCreateHorseUseCase.execute() called for horse: ${request.pferdeName}")

        // Wrap the entire use case logic in a single transaction
        return DatabaseFactory.dbQuery {
            println("[DEBUG_LOG] Inside transaction for horse: ${request.pferdeName}")
            // Create domain object
            val horse = DomPferd(
                pferdeName = request.pferdeName,
                geschlecht = request.geschlecht,
                geburtsdatum = request.geburtsdatum,
                rasse = request.rasse,
                farbe = request.farbe,
                besitzerId = request.besitzerId,
                verantwortlichePersonId = request.verantwortlichePersonId,
                zuechterName = request.zuechterName,
                zuchtbuchNummer = request.zuchtbuchNummer,
                lebensnummer = request.lebensnummer,
                chipNummer = request.chipNummer,
                passNummer = request.passNummer,
                oepsNummer = request.oepsNummer,
                feiNummer = request.feiNummer,
                vaterName = request.vaterName,
                mutterName = request.mutterName,
                mutterVaterName = request.mutterVaterName,
                stockmass = request.stockmass,
                bemerkungen = request.bemerkungen,
                datenQuelle = request.datenQuelle
            )

            // Validate the horse
            println("[DEBUG_LOG] Starting validation for horse: ${horse.pferdeName}")
            val validationResult = validateHorse(horse)
            if (!validationResult.isValid()) {
                val errors = (validationResult as ValidationResult.Invalid).errors
                println("[DEBUG_LOG] Validation failed for horse: ${horse.pferdeName}, errors: ${errors.map { "${it.field}: ${it.message}" }}")
                return@dbQuery ApiResponse(
                    success = false,
                    data = null,
                    error = ErrorDto(
                        code = "VALIDATION_ERROR",
                        message = "Horse validation failed",
                        details = errors.associate { it.field to it.message }
                    )
                )
            }
            println("[DEBUG_LOG] Validation passed for horse: ${horse.pferdeName}")

            // Check for uniqueness constraints - all within the same transaction
            println("[DEBUG_LOG] Starting uniqueness check for horse: ${horse.pferdeName}")
            val uniquenessResult = checkUniquenessConstraints(horse)
            if (!uniquenessResult.isValid()) {
                val errors = (uniquenessResult as ValidationResult.Invalid).errors
                println("[DEBUG_LOG] Uniqueness check failed for horse: ${horse.pferdeName}, errors: ${errors.map { "${it.field}: ${it.message}" }}")
                return@dbQuery ApiResponse(
                    success = false,
                    data = null,
                    error = ErrorDto(
                        code = "UNIQUENESS_ERROR",
                        message = "Horse uniqueness validation failed",
                        details = errors.associate { it.field to it.message }
                    )
                )
            }
            println("[DEBUG_LOG] Uniqueness check passed for horse: ${horse.pferdeName}")

            // Save the horse - still within the same transaction
            println("[DEBUG_LOG] Saving horse: ${horse.pferdeName}")
            try {
                val savedHorse = horseRepository.save(horse)
                println("[DEBUG_LOG] Horse saved successfully: ${savedHorse.pferdeName} with ID: ${savedHorse.pferdId}")

                ApiResponse(
                    success = true,
                    data = savedHorse,
                    message = "Horse created successfully"
                )
            } catch (e: Exception) {
                println("[DEBUG_LOG] Database constraint violation for horse: ${horse.pferdeName}, error: ${e.message}")

                // Handle database constraint violations (duplicate keys)
                if (e.message?.contains("unique", ignoreCase = true) == true ||
                    e.message?.contains("duplicate", ignoreCase = true) == true) {

                    // Determine which field caused the constraint violation
                    val constraintField = when {
                        e.message?.contains("lebensnummer", ignoreCase = true) == true -> "lebensnummer"
                        e.message?.contains("chip_nummer", ignoreCase = true) == true -> "chipNummer"
                        e.message?.contains("pass_nummer", ignoreCase = true) == true -> "passNummer"
                        e.message?.contains("oeps_nummer", ignoreCase = true) == true -> "oepsNummer"
                        e.message?.contains("fei_nummer", ignoreCase = true) == true -> "feiNummer"
                        else -> "identification"
                    }

                    ApiResponse(
                        success = false,
                        data = null,
                        error = ErrorDto(
                            code = "UNIQUENESS_ERROR",
                            message = "Horse uniqueness validation failed due to database constraint",
                            details = mapOf(constraintField to "A horse with this ${constraintField} already exists")
                        )
                    )
                } else {
                    // Re-throw other exceptions
                    throw e
                }
            }
        }
    }

    /**
     * Validates the horse data according to business rules.
     */
    private fun validateHorse(horse: DomPferd): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Use domain validation
        val domainErrors = horse.validateForRegistration()
        domainErrors.forEach { errorMessage ->
            errors.add(ValidationError("horse", errorMessage, "DOMAIN_VALIDATION"))
        }

        // Additional business validations
        horse.stockmass?.let { height ->
            if (height < 50 || height > 220) {
                errors.add(ValidationError("stockmass", "Horse height must be between 50 and 220 cm", "INVALID_RANGE"))
            }
        }

        horse.geburtsdatum?.let { birthDate ->
            val currentYear = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).year
            if (birthDate.year > currentYear) {
                errors.add(ValidationError("geburtsdatum", "Birth date cannot be in the future", "FUTURE_DATE"))
            }
            if (birthDate.year < (currentYear - 50)) {
                errors.add(ValidationError("geburtsdatum", "Birth date cannot be more than 50 years ago", "TOO_OLD"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Checks uniqueness constraints for identification numbers.
     * Note: This method is called within a transaction, so all repository calls
     * will use the same transaction context.
     */
    private suspend fun checkUniquenessConstraints(horse: DomPferd): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Check lebensnummer uniqueness
        horse.lebensnummer?.let { lebensnummer ->
            if (lebensnummer.isNotBlank() && horseRepository.existsByLebensnummer(lebensnummer)) {
                errors.add(ValidationError("lebensnummer", "A horse with this life number already exists", "DUPLICATE"))
            }
        }

        // Check chip number uniqueness
        horse.chipNummer?.let { chipNummer ->
            if (chipNummer.isNotBlank() && horseRepository.existsByChipNummer(chipNummer)) {
                errors.add(ValidationError("chipNummer", "A horse with this chip number already exists", "DUPLICATE"))
            }
        }

        // Check passport number uniqueness
        horse.passNummer?.let { passNummer ->
            if (passNummer.isNotBlank() && horseRepository.existsByPassNummer(passNummer)) {
                errors.add(ValidationError("passNummer", "A horse with this passport number already exists", "DUPLICATE"))
            }
        }

        // Check OEPS number uniqueness
        horse.oepsNummer?.let { oepsNummer ->
            if (oepsNummer.isNotBlank() && horseRepository.existsByOepsNummer(oepsNummer)) {
                errors.add(ValidationError("oepsNummer", "A horse with this OEPS number already exists", "DUPLICATE"))
            }
        }

        // Check FEI number uniqueness
        horse.feiNummer?.let { feiNummer ->
            if (feiNummer.isNotBlank() && horseRepository.existsByFeiNummer(feiNummer)) {
                errors.add(ValidationError("feiNummer", "A horse with this FEI number already exists", "DUPLICATE"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
