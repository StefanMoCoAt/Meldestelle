package at.mocode.services

import at.mocode.stammdaten.Verein
import at.mocode.repositories.VereinRepository
import com.benasher44.uuid.Uuid

/**
 * Service layer for Verein (Club) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class VereinService(private val vereinRepository: VereinRepository) {

    /**
     * Retrieve all clubs
     */
    suspend fun getAllVereine(): List<Verein> {
        return vereinRepository.findAll()
    }

    /**
     * Find a club by its unique identifier
     */
    suspend fun getVereinById(id: Uuid): Verein? {
        return vereinRepository.findById(id)
    }

    /**
     * Find a club by its OEPS (Austrian Equestrian Federation) number
     */
    suspend fun getVereinByOepsNr(oepsVereinsNr: String): Verein? {
        if (oepsVereinsNr.isBlank()) {
            throw IllegalArgumentException("OEPS Vereins number cannot be blank")
        }
        return vereinRepository.findByOepsVereinsNr(oepsVereinsNr)
    }

    /**
     * Search for clubs by query string
     */
    suspend fun searchVereine(query: String): List<Verein> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return vereinRepository.search(query.trim())
    }

    /**
     * Find clubs by federal state (Bundesland)
     */
    suspend fun getVereineByBundesland(bundesland: String): List<Verein> {
        if (bundesland.isBlank()) {
            throw IllegalArgumentException("Bundesland cannot be blank")
        }
        return vereinRepository.findByBundesland(bundesland)
    }

    /**
     * Create a new club with business validation
     */
    suspend fun createVerein(verein: Verein): Verein {
        validateVerein(verein)

        // Check if OEPS number already exists
        verein.oepsVereinsNr.let { oepsNr ->
            val existing = vereinRepository.findByOepsVereinsNr(oepsNr)
            if (existing != null) {
                throw IllegalArgumentException("A club with OEPS number '$oepsNr' already exists")
            }
        }

        return vereinRepository.create(verein)
    }

    /**
     * Update an existing club
     */
    suspend fun updateVerein(id: Uuid, verein: Verein): Verein? {
        validateVerein(verein)

        // Check if the OEPS number conflicts with another club
        verein.oepsVereinsNr.let { oepsNr ->
            val existing = vereinRepository.findByOepsVereinsNr(oepsNr)
            if (existing != null && existing.id != id) {
                throw IllegalArgumentException("A club with OEPS number '$oepsNr' already exists")
            }
        }

        return vereinRepository.update(id, verein)
    }

    /**
     * Delete a club by ID
     */
    suspend fun deleteVerein(id: Uuid): Boolean {
        return vereinRepository.delete(id)
    }

    /**
     * Validate club data according to business rules
     */
    private fun validateVerein(verein: Verein) {
        if (verein.name.isBlank()) {
            throw IllegalArgumentException("Club name cannot be blank")
        }

        if (verein.name.length > 255) {
            throw IllegalArgumentException("Club name cannot exceed 255 characters")
        }

        // Additional validation rules can be added here
        verein.oepsVereinsNr.let { oepsNr ->
            if (oepsNr.isBlank()) {
                throw IllegalArgumentException("OEPS Vereins number cannot be blank if provided")
            }
        }
    }
}
