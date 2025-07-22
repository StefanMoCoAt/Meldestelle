package at.mocode.horses.application.usecase

import at.mocode.horses.domain.model.DomPferd
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.core.domain.model.PferdeGeschlechtE
import com.benasher44.uuid.Uuid
import kotlinx.datetime.todayIn

/**
 * Use case for retrieving horse information.
 *
 * This use case encapsulates the business logic for fetching horse data
 * and provides a clean interface for the application layer.
 */
class GetHorseUseCase(
    private val horseRepository: HorseRepository
) {

    /**
     * Retrieves a horse by its unique ID.
     *
     * @param horseId The unique identifier of the horse
     * @return The horse if found, null otherwise
     */
    suspend fun getById(horseId: Uuid): DomPferd? {
        return horseRepository.findById(horseId)
    }

    /**
     * Retrieves a horse by its life number.
     *
     * @param lebensnummer The life number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun getByLebensnummer(lebensnummer: String): DomPferd? {
        require(lebensnummer.isNotBlank()) { "Life number cannot be blank" }
        return horseRepository.findByLebensnummer(lebensnummer.trim())
    }

    /**
     * Retrieves a horse by its chip number.
     *
     * @param chipNummer The chip number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun getByChipNummer(chipNummer: String): DomPferd? {
        require(chipNummer.isNotBlank()) { "Chip number cannot be blank" }
        return horseRepository.findByChipNummer(chipNummer.trim())
    }

    /**
     * Retrieves a horse by its passport number.
     *
     * @param passNummer The passport number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun getByPassNummer(passNummer: String): DomPferd? {
        require(passNummer.isNotBlank()) { "Passport number cannot be blank" }
        return horseRepository.findByPassNummer(passNummer.trim())
    }

    /**
     * Retrieves a horse by its OEPS number.
     *
     * @param oepsNummer The OEPS number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun getByOepsNummer(oepsNummer: String): DomPferd? {
        require(oepsNummer.isNotBlank()) { "OEPS number cannot be blank" }
        return horseRepository.findByOepsNummer(oepsNummer.trim())
    }

    /**
     * Retrieves a horse by its FEI number.
     *
     * @param feiNummer The FEI number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun getByFeiNummer(feiNummer: String): DomPferd? {
        require(feiNummer.isNotBlank()) { "FEI number cannot be blank" }
        return horseRepository.findByFeiNummer(feiNummer.trim())
    }

    /**
     * Searches for horses by name (partial match).
     *
     * @param searchTerm The search term to match against horse names
     * @param limit Maximum number of results to return (default: 50)
     * @return List of matching horses
     */
    suspend fun searchByName(searchTerm: String, limit: Int = 50): List<DomPferd> {
        require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
        require(limit > 0) { "Limit must be positive" }
        return horseRepository.findByName(searchTerm.trim(), limit)
    }

    /**
     * Retrieves all horses owned by a specific person.
     *
     * @param ownerId The ID of the owner
     * @param activeOnly Whether to return only active horses (default: true)
     * @return List of horses owned by the person
     */
    suspend fun getByOwnerId(ownerId: Uuid, activeOnly: Boolean = true): List<DomPferd> {
        return horseRepository.findByOwnerId(ownerId, activeOnly)
    }

    /**
     * Retrieves all horses for which a person is responsible.
     *
     * @param responsiblePersonId The ID of the responsible person
     * @param activeOnly Whether to return only active horses (default: true)
     * @return List of horses for which the person is responsible
     */
    suspend fun getByResponsiblePersonId(responsiblePersonId: Uuid, activeOnly: Boolean = true): List<DomPferd> {
        return horseRepository.findByResponsiblePersonId(responsiblePersonId, activeOnly)
    }

    /**
     * Retrieves horses by gender.
     *
     * @param geschlecht The gender to filter by
     * @param activeOnly Whether to return only active horses (default: true)
     * @param limit Maximum number of results to return (default: 100)
     * @return List of horses with the specified gender
     */
    suspend fun getByGeschlecht(geschlecht: PferdeGeschlechtE, activeOnly: Boolean = true, limit: Int = 100): List<DomPferd> {
        require(limit > 0) { "Limit must be positive" }
        return horseRepository.findByGeschlecht(geschlecht, activeOnly, limit)
    }

    /**
     * Retrieves horses by breed.
     *
     * @param rasse The breed to filter by
     * @param activeOnly Whether to return only active horses (default: true)
     * @param limit Maximum number of results to return (default: 100)
     * @return List of horses of the specified breed
     */
    suspend fun getByRasse(rasse: String, activeOnly: Boolean = true, limit: Int = 100): List<DomPferd> {
        require(rasse.isNotBlank()) { "Breed cannot be blank" }
        require(limit > 0) { "Limit must be positive" }
        return horseRepository.findByRasse(rasse.trim(), activeOnly, limit)
    }

    /**
     * Retrieves horses by birth year.
     *
     * @param birthYear The birth year to filter by
     * @param activeOnly Whether to return only active horses (default: true)
     * @return List of horses born in the specified year
     */
    suspend fun getByBirthYear(birthYear: Int, activeOnly: Boolean = true): List<DomPferd> {
        require(birthYear > 1900) { "Birth year must be after 1900" }
        require(birthYear <= kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).year) {
            "Birth year cannot be in the future"
        }
        return horseRepository.findByBirthYear(birthYear, activeOnly)
    }

    /**
     * Retrieves horses by birth year range.
     *
     * @param fromYear The start year (inclusive)
     * @param toYear The end year (inclusive)
     * @param activeOnly Whether to return only active horses (default: true)
     * @return List of horses born within the specified year range
     */
    suspend fun getByBirthYearRange(fromYear: Int, toYear: Int, activeOnly: Boolean = true): List<DomPferd> {
        require(fromYear > 1900) { "From year must be after 1900" }
        require(toYear >= fromYear) { "To year must be greater than or equal to from year" }
        val currentYear = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).year
        require(toYear <= currentYear) { "To year cannot be in the future" }

        return horseRepository.findByBirthYearRange(fromYear, toYear, activeOnly)
    }

    /**
     * Retrieves all active horses.
     *
     * @param limit Maximum number of results to return (default: 1000)
     * @return List of active horses
     */
    suspend fun getAllActive(limit: Int = 1000): List<DomPferd> {
        require(limit > 0) { "Limit must be positive" }
        return horseRepository.findAllActive(limit)
    }

    /**
     * Retrieves horses with OEPS registration.
     *
     * @param activeOnly Whether to return only active horses (default: true)
     * @return List of OEPS registered horses
     */
    suspend fun getOepsRegistered(activeOnly: Boolean = true): List<DomPferd> {
        return horseRepository.findOepsRegistered(activeOnly)
    }

    /**
     * Retrieves horses with FEI registration.
     *
     * @param activeOnly Whether to return only active horses (default: true)
     * @return List of FEI registered horses
     */
    suspend fun getFeiRegistered(activeOnly: Boolean = true): List<DomPferd> {
        return horseRepository.findFeiRegistered(activeOnly)
    }

    /**
     * Checks if a horse with the given life number exists.
     *
     * @param lebensnummer The life number to check
     * @return true if a horse with this life number exists, false otherwise
     */
    suspend fun existsByLebensnummer(lebensnummer: String): Boolean {
        require(lebensnummer.isNotBlank()) { "Life number cannot be blank" }
        return horseRepository.existsByLebensnummer(lebensnummer.trim())
    }

    /**
     * Checks if a horse with the given chip number exists.
     *
     * @param chipNummer The chip number to check
     * @return true if a horse with this chip number exists, false otherwise
     */
    suspend fun existsByChipNummer(chipNummer: String): Boolean {
        require(chipNummer.isNotBlank()) { "Chip number cannot be blank" }
        return horseRepository.existsByChipNummer(chipNummer.trim())
    }

    /**
     * Checks if a horse with the given passport number exists.
     *
     * @param passNummer The passport number to check
     * @return true if a horse with this passport number exists, false otherwise
     */
    suspend fun existsByPassNummer(passNummer: String): Boolean {
        require(passNummer.isNotBlank()) { "Passport number cannot be blank" }
        return horseRepository.existsByPassNummer(passNummer.trim())
    }

    /**
     * Checks if a horse with the given OEPS number exists.
     *
     * @param oepsNummer The OEPS number to check
     * @return true if a horse with this OEPS number exists, false otherwise
     */
    suspend fun existsByOepsNummer(oepsNummer: String): Boolean {
        require(oepsNummer.isNotBlank()) { "OEPS number cannot be blank" }
        return horseRepository.existsByOepsNummer(oepsNummer.trim())
    }

    /**
     * Checks if a horse with the given FEI number exists.
     *
     * @param feiNummer The FEI number to check
     * @return true if a horse with this FEI number exists, false otherwise
     */
    suspend fun existsByFeiNummer(feiNummer: String): Boolean {
        require(feiNummer.isNotBlank()) { "FEI number cannot be blank" }
        return horseRepository.existsByFeiNummer(feiNummer.trim())
    }

    /**
     * Counts the total number of active horses.
     *
     * @return The total count of active horses
     */
    suspend fun countActive(): Long {
        return horseRepository.countActive()
    }

    /**
     * Counts horses by owner.
     *
     * @param ownerId The ID of the owner
     * @param activeOnly Whether to count only active horses (default: true)
     * @return The count of horses owned by the person
     */
    suspend fun countByOwnerId(ownerId: Uuid, activeOnly: Boolean = true): Long {
        return horseRepository.countByOwnerId(ownerId, activeOnly)
    }
}
