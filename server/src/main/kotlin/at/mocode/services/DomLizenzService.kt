package at.mocode.services

import at.mocode.model.domaene.DomLizenz
import at.mocode.repositories.DomLizenzRepository
import com.benasher44.uuid.Uuid

/**
 * Service layer for DomLizenz (Domain License) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class DomLizenzService(private val domLizenzRepository: DomLizenzRepository) {

    /**
     * Retrieve all licenses
     */
    suspend fun getAllLizenzen(): List<DomLizenz> {
        return domLizenzRepository.findAll()
    }

    /**
     * Find a license by its unique identifier
     */
    suspend fun getLizenzById(id: Uuid): DomLizenz? {
        return domLizenzRepository.findById(id)
    }

    /**
     * Find licenses by person ID
     */
    suspend fun getLizenzenByPersonId(personId: Uuid): List<DomLizenz> {
        return domLizenzRepository.findByPersonId(personId)
    }

    /**
     * Find licenses by license type global ID
     */
    suspend fun getLizenzenByLizenzTypGlobalId(lizenzTypGlobalId: Uuid): List<DomLizenz> {
        return domLizenzRepository.findByLizenzTypGlobalId(lizenzTypGlobalId)
    }

    /**
     * Find active licenses by person ID
     */
    suspend fun getActiveLizenzenByPersonId(personId: Uuid): List<DomLizenz> {
        return domLizenzRepository.findActiveByPersonId(personId)
    }

    /**
     * Find licenses by validity year
     */
    suspend fun getLizenzenByValidityYear(year: Int): List<DomLizenz> {
        if (year < 1900 || year > 2100) {
            throw IllegalArgumentException("Year must be between 1900 and 2100")
        }
        return domLizenzRepository.findByValidityYear(year)
    }

    /**
     * Search for licenses by query string
     */
    suspend fun searchLizenzen(query: String): List<DomLizenz> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return domLizenzRepository.search(query.trim())
    }

    /**
     * Create a new license with business validation
     */
    suspend fun createLizenz(domLizenz: DomLizenz): DomLizenz {
        validateLizenz(domLizenz)
        return domLizenzRepository.create(domLizenz)
    }

    /**
     * Update an existing license
     */
    suspend fun updateLizenz(id: Uuid, domLizenz: DomLizenz): DomLizenz? {
        validateLizenz(domLizenz)
        return domLizenzRepository.update(id, domLizenz)
    }

    /**
     * Delete a license by ID
     */
    suspend fun deleteLizenz(id: Uuid): Boolean {
        return domLizenzRepository.delete(id)
    }

    /**
     * Check if a person has an active license of a specific type
     */
    suspend fun hasActiveLicense(personId: Uuid, lizenzTypGlobalId: Uuid): Boolean {
        val activeLicenses = getActiveLizenzenByPersonId(personId)
        return activeLicenses.any { it.lizenzTypGlobalId == lizenzTypGlobalId }
    }

    /**
     * Get current year licenses for a person
     */
    suspend fun getCurrentYearLizenzenByPersonId(personId: Uuid): List<DomLizenz> {
        val currentYear = java.time.LocalDate.now().year
        val allPersonLicenses = getLizenzenByPersonId(personId)
        return allPersonLicenses.filter { license ->
            license.gueltigBisJahr == currentYear || license.ausgestelltAm?.year == currentYear
        }
    }

    /**
     * Validate license data according to business rules
     */
    private fun validateLizenz(domLizenz: DomLizenz) {
        // Validate that gueltigBisJahr is reasonable if provided
        domLizenz.gueltigBisJahr?.let { year ->
            if (year < 1900 || year > 2100) {
                throw IllegalArgumentException("License validity year must be between 1900 and 2100")
            }
        }

        // Validate that ausgestelltAm is not in the future if provided
        domLizenz.ausgestelltAm?.let { date ->
            val currentYear = java.time.LocalDate.now().year
            if (date.year > currentYear) {
                throw IllegalArgumentException("License issue date cannot be in the future")
            }
        }

        // Additional validation rules can be added here
        // For example, checking if the license type is valid, person exists, etc.
    }
}
