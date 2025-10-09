@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.horses.application.usecase

import at.mocode.horses.domain.model.DomPferd
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.core.domain.model.PferdeGeschlechtE
import at.mocode.core.domain.model.DatenQuelleE
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn

/**
 * Use case for updating an existing horse in the registry.
 *
 * This use case handles the business logic for horse updates including
 * validation, uniqueness checks, and persistence.
 */
class UpdateHorseUseCase(
    private val horseRepository: HorseRepository
) {

    /**
     * Request data for updating a horse.
     */
    data class UpdateHorseRequest(
        val pferdId: Uuid,
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
        val istAktiv: Boolean = true,
        val bemerkungen: String? = null,
        val datenQuelle: DatenQuelleE = DatenQuelleE.MANUELL
    )

    /**
     * Response data for horse update.
     */
    data class UpdateHorseResponse(
        val horse: DomPferd?,
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Executes the horse update use case.
     *
     * @param request The horse update request data
     * @return UpdateHorseResponse with the updated horse or validation errors
     */
    suspend fun execute(request: UpdateHorseRequest): UpdateHorseResponse {
        // Check if horse exists
        val existingHorse = horseRepository.findById(request.pferdId)
            ?: return UpdateHorseResponse(
                horse = null,
                success = false,
                errors = listOf("Horse not found")
            )

        // Create updated domain object
        val updatedHorse = existingHorse.copy(
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
            istAktiv = request.istAktiv,
            bemerkungen = request.bemerkungen,
            datenQuelle = request.datenQuelle
        )

        // Validate the updated horse
        val validationErrors = validateHorse(updatedHorse)
        if (validationErrors.isNotEmpty()) {
            return UpdateHorseResponse(
                horse = updatedHorse,
                success = false,
                errors = validationErrors
            )
        }

        // Check for uniqueness constraints (excluding current horse)
        val uniquenessErrors = checkUniquenessConstraints(updatedHorse, existingHorse)
        if (uniquenessErrors.isNotEmpty()) {
            return UpdateHorseResponse(
                horse = updatedHorse,
                success = false,
                errors = uniquenessErrors
            )
        }

        // Save the updated horse
        val savedHorse = horseRepository.save(updatedHorse)

        return UpdateHorseResponse(
            horse = savedHorse,
            success = true
        )
    }

    /**
     * Validates the horse data according to business rules.
     */
    private fun validateHorse(horse: DomPferd): List<String> {
        val errors = mutableListOf<String>()

        // Basic validation
        if (horse.pferdeName.isBlank()) {
            errors.add("Horse name is required")
        }

        // Height validation
        horse.stockmass?.let { height ->
            if (height < 50 || height > 220) {
                errors.add("Horse height must be between 50 and 220 cm")
            }
        }

        // Birth date validation
        horse.geburtsdatum?.let { birthDate ->
            val currentYear = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).year
            if (birthDate.year > currentYear) {
                errors.add("Birth date cannot be in the future")
            }
            if (birthDate.year < (currentYear - 50)) {
                errors.add("Birth date cannot be more than 50 years ago")
            }
        }

        return errors
    }

    /**
     * Checks uniqueness constraints for identification numbers, excluding the current horse.
     */
    private suspend fun checkUniquenessConstraints(updatedHorse: DomPferd, existingHorse: DomPferd): List<String> {
        val errors = mutableListOf<String>()

        // Check lebensnummer uniqueness (if changed)
        updatedHorse.lebensnummer?.let { lebensnummer ->
            if (lebensnummer.isNotBlank() &&
                lebensnummer != existingHorse.lebensnummer &&
                horseRepository.existsByLebensnummer(lebensnummer)) {
                errors.add("A horse with this life number already exists")
            }
        }

        // Check chip number uniqueness (if changed)
        updatedHorse.chipNummer?.let { chipNummer ->
            if (chipNummer.isNotBlank() &&
                chipNummer != existingHorse.chipNummer &&
                horseRepository.existsByChipNummer(chipNummer)) {
                errors.add("A horse with this chip number already exists")
            }
        }

        // Check passport number uniqueness (if changed)
        updatedHorse.passNummer?.let { passNummer ->
            if (passNummer.isNotBlank() &&
                passNummer != existingHorse.passNummer &&
                horseRepository.existsByPassNummer(passNummer)) {
                errors.add("A horse with this passport number already exists")
            }
        }

        // Check OEPS number uniqueness (if changed)
        updatedHorse.oepsNummer?.let { oepsNummer ->
            if (oepsNummer.isNotBlank() &&
                oepsNummer != existingHorse.oepsNummer &&
                horseRepository.existsByOepsNummer(oepsNummer)) {
                errors.add("A horse with this OEPS number already exists")
            }
        }

        // Check FEI number uniqueness (if changed)
        updatedHorse.feiNummer?.let { feiNummer ->
            if (feiNummer.isNotBlank() &&
                feiNummer != existingHorse.feiNummer &&
                horseRepository.existsByFeiNummer(feiNummer)) {
                errors.add("A horse with this FEI number already exists")
            }
        }

        return errors
    }
}
