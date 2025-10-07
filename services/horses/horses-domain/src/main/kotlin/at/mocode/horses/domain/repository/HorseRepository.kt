@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.horses.domain.repository

import at.mocode.horses.domain.model.DomPferd
import at.mocode.core.domain.model.PferdeGeschlechtE
import kotlin.uuid.Uuid

/**
 * Repository interface for DomPferd (Horse) domain operations.
 *
 * This interface defines the contract for horse data access operations
 * without depending on specific implementation details (database, etc.).
 * Following the hexagonal architecture pattern, this interface belongs
 * to the domain layer and will be implemented in the infrastructure layer.
 */
interface HorseRepository {

    /**
     * Finds a horse by its unique ID.
     *
     * @param id The unique identifier of the horse
     * @return The horse if found, null otherwise
     */
    suspend fun findById(id: Uuid): DomPferd?

    /**
     * Finds a horse by its life number (Lebensnummer).
     *
     * @param lebensnummer The life number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun findByLebensnummer(lebensnummer: String): DomPferd?

    /**
     * Finds a horse by its chip number.
     *
     * @param chipNummer The chip number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun findByChipNummer(chipNummer: String): DomPferd?

    /**
     * Finds a horse by its passport number.
     *
     * @param passNummer The passport number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun findByPassNummer(passNummer: String): DomPferd?

    /**
     * Finds a horse by its OEPS number.
     *
     * @param oepsNummer The OEPS number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun findByOepsNummer(oepsNummer: String): DomPferd?

    /**
     * Finds a horse by its FEI number.
     *
     * @param feiNummer The FEI number to search for
     * @return The horse if found, null otherwise
     */
    suspend fun findByFeiNummer(feiNummer: String): DomPferd?

    /**
     * Finds horses by name (partial match).
     *
     * @param searchTerm The search term to match against horse names
     * @param limit Maximum number of results to return
     * @return List of matching horses
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<DomPferd>

    /**
     * Finds all horses owned by a specific person.
     *
     * @param ownerId The ID of the owner (from member-management context)
     * @param activeOnly Whether to return only active horses
     * @return List of horses owned by the person
     */
    suspend fun findByOwnerId(ownerId: Uuid, activeOnly: Boolean = true): List<DomPferd>

    /**
     * Finds all horses for which a person is responsible.
     *
     * @param responsiblePersonId The ID of the responsible person
     * @param activeOnly Whether to return only active horses
     * @return List of horses for which the person is responsible
     */
    suspend fun findByResponsiblePersonId(responsiblePersonId: Uuid, activeOnly: Boolean = true): List<DomPferd>

    /**
     * Finds horses by gender.
     *
     * @param geschlecht The gender to filter by
     * @param activeOnly Whether to return only active horses
     * @param limit Maximum number of results to return
     * @return List of horses with the specified gender
     */
    suspend fun findByGeschlecht(geschlecht: PferdeGeschlechtE, activeOnly: Boolean = true, limit: Int = 100): List<DomPferd>

    /**
     * Finds horses by breed.
     *
     * @param rasse The breed to filter by
     * @param activeOnly Whether to return only active horses
     * @param limit Maximum number of results to return
     * @return List of horses of the specified breed
     */
    suspend fun findByRasse(rasse: String, activeOnly: Boolean = true, limit: Int = 100): List<DomPferd>

    /**
     * Finds horses by birth year.
     *
     * @param birthYear The birth year to filter by
     * @param activeOnly Whether to return only active horses
     * @return List of horses born in the specified year
     */
    suspend fun findByBirthYear(birthYear: Int, activeOnly: Boolean = true): List<DomPferd>

    /**
     * Finds horses by birth year range.
     *
     * @param fromYear The start year (inclusive)
     * @param toYear The end year (inclusive)
     * @param activeOnly Whether to return only active horses
     * @return List of horses born within the specified year range
     */
    suspend fun findByBirthYearRange(fromYear: Int, toYear: Int, activeOnly: Boolean = true): List<DomPferd>

    /**
     * Finds all active horses.
     *
     * @param limit Maximum number of results to return
     * @return List of active horses
     */
    suspend fun findAllActive(limit: Int = 1000): List<DomPferd>

    /**
     * Finds horses with OEPS registration.
     *
     * @param activeOnly Whether to return only active horses
     * @return List of OEPS registered horses
     */
    suspend fun findOepsRegistered(activeOnly: Boolean = true): List<DomPferd>

    /**
     * Finds horses with FEI registration.
     *
     * @param activeOnly Whether to return only active horses
     * @return List of FEI registered horses
     */
    suspend fun findFeiRegistered(activeOnly: Boolean = true): List<DomPferd>

    /**
     * Saves a horse (create or update).
     *
     * @param horse The horse to save
     * @return The saved horse with updated timestamps
     */
    suspend fun save(horse: DomPferd): DomPferd

    /**
     * Deletes a horse by ID.
     *
     * @param id The unique identifier of the horse to delete
     * @return true if the horse was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Checks if a horse with the given life number exists.
     *
     * @param lebensnummer The life number to check
     * @return true if a horse with this life number exists, false otherwise
     */
    suspend fun existsByLebensnummer(lebensnummer: String): Boolean

    /**
     * Checks if a horse with the given chip number exists.
     *
     * @param chipNummer The chip number to check
     * @return true if a horse with this chip number exists, false otherwise
     */
    suspend fun existsByChipNummer(chipNummer: String): Boolean

    /**
     * Checks if a horse with the given passport number exists.
     *
     * @param passNummer The passport number to check
     * @return true if a horse with this passport number exists, false otherwise
     */
    suspend fun existsByPassNummer(passNummer: String): Boolean

    /**
     * Checks if a horse with the given OEPS number exists.
     *
     * @param oepsNummer The OEPS number to check
     * @return true if a horse with this OEPS number exists, false otherwise
     */
    suspend fun existsByOepsNummer(oepsNummer: String): Boolean

    /**
     * Checks if a horse with the given FEI number exists.
     *
     * @param feiNummer The FEI number to check
     * @return true if a horse with this FEI number exists, false otherwise
     */
    suspend fun existsByFeiNummer(feiNummer: String): Boolean

    /**
     * Counts the total number of active horses.
     *
     * @return The total count of active horses
     */
    suspend fun countActive(): Long

    /**
     * Counts horses by owner.
     *
     * @param ownerId The ID of the owner
     * @param activeOnly Whether to count only active horses
     * @return The count of horses owned by the person
     */
    suspend fun countByOwnerId(ownerId: Uuid, activeOnly: Boolean = true): Long

    /**
     * Counts horses with OEPS registration.
     *
     * @param activeOnly Whether to count only active horses
     * @return The count of OEPS registered horses
     */
    suspend fun countOepsRegistered(activeOnly: Boolean = true): Long

    /**
     * Counts horses with FEI registration.
     *
     * @param activeOnly Whether to count only active horses
     * @return The count of FEI registered horses
     */
    suspend fun countFeiRegistered(activeOnly: Boolean = true): Long
}
