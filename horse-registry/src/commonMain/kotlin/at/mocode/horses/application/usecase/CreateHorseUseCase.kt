package at.mocode.horses.application.usecase

import at.mocode.horses.domain.model.DomPferd
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.enums.PferdeGeschlechtE
import at.mocode.enums.DatenQuelleE
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate

/**
 * Use case for creating a new horse in the registry.
 *
 * This use case handles the business logic for horse registration including
 * validation, uniqueness checks, and persistence.
 */
class CreateHorseUseCase(
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
        val datenQuelle: DatenQuelleE = DatenQuelleE.MANUAL
    )

    /**
     * Response data for horse creation.
     */
    data class CreateHorseResponse(
        val horse: DomPferd,
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Executes the horse creation use case.
     *
     * @param request The horse creation request data
     * @return CreateHorseResponse with the created horse or validation errors
     */
    suspend fun execute(request: CreateHorseRequest): CreateHorseResponse {
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
        val validationErrors = validateHorse(horse)
        if (validationErrors.isNotEmpty()) {
            return CreateHorseResponse(
                horse = horse,
                success = false,
                errors = validationErrors
            )
        }

        // Check for uniqueness constraints
        val uniquenessErrors = checkUniquenessConstraints(horse)
        if (uniquenessErrors.isNotEmpty()) {
            return CreateHorseResponse(
                horse = horse,
                success = false,
                errors = uniquenessErrors
            )
        }

        // Save the horse
        val savedHorse = horseRepository.save(horse)

        return CreateHorseResponse(
            horse = savedHorse,
            success = true
        )
    }

    /**
     * Validates the horse data according to business rules.
     */
    private fun validateHorse(horse: DomPferd): List<String> {
        val errors = mutableListOf<String>()

        // Use domain validation
        errors.addAll(horse.validateForRegistration())

        // Additional business validations
        if (horse.stockmass != null && (horse.stockmass!! < 50 || horse.stockmass!! > 220)) {
            errors.add("Horse height must be between 50 and 220 cm")
        }

        if (horse.geburtsdatum != null) {
            val currentYear = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).year
            if (horse.geburtsdatum!!.year > currentYear) {
                errors.add("Birth date cannot be in the future")
            }
            if (horse.geburtsdatum!!.year < (currentYear - 50)) {
                errors.add("Birth date cannot be more than 50 years ago")
            }
        }

        return errors
    }

    /**
     * Checks uniqueness constraints for identification numbers.
     */
    private suspend fun checkUniquenessConstraints(horse: DomPferd): List<String> {
        val errors = mutableListOf<String>()

        // Check lebensnummer uniqueness
        horse.lebensnummer?.let { lebensnummer ->
            if (lebensnummer.isNotBlank() && horseRepository.existsByLebensnummer(lebensnummer)) {
                errors.add("A horse with this life number already exists")
            }
        }

        // Check chip number uniqueness
        horse.chipNummer?.let { chipNummer ->
            if (chipNummer.isNotBlank() && horseRepository.existsByChipNummer(chipNummer)) {
                errors.add("A horse with this chip number already exists")
            }
        }

        // Check passport number uniqueness
        horse.passNummer?.let { passNummer ->
            if (passNummer.isNotBlank() && horseRepository.existsByPassNummer(passNummer)) {
                errors.add("A horse with this passport number already exists")
            }
        }

        // Check OEPS number uniqueness
        horse.oepsNummer?.let { oepsNummer ->
            if (oepsNummer.isNotBlank() && horseRepository.existsByOepsNummer(oepsNummer)) {
                errors.add("A horse with this OEPS number already exists")
            }
        }

        // Check FEI number uniqueness
        horse.feiNummer?.let { feiNummer ->
            if (feiNummer.isNotBlank() && horseRepository.existsByFeiNummer(feiNummer)) {
                errors.add("A horse with this FEI number already exists")
            }
        }

        return errors
    }
}
