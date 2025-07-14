package at.mocode.services

import at.mocode.model.Platz
import at.mocode.repositories.PlatzRepository
import at.mocode.enums.PlatzTypE
import com.benasher44.uuid.Uuid

/**
 * Service layer for Platz (Place) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class PlatzService(private val platzRepository: PlatzRepository) {

    /**
     * Retrieve all places
     */
    suspend fun getAllPlaetze(): List<Platz> {
        return platzRepository.findAll()
    }

    /**
     * Find a place by its unique identifier
     */
    suspend fun getPlatzById(id: Uuid): Platz? {
        return platzRepository.findById(id)
    }

    /**
     * Find places by tournament ID
     */
    suspend fun getPlaetzeByTurnierId(turnierId: Uuid): List<Platz> {
        return platzRepository.findByTurnierId(turnierId)
    }

    /**
     * Find places by type
     */
    suspend fun getPlaetzeByTyp(typ: PlatzTypE): List<Platz> {
        return platzRepository.findByTyp(typ)
    }

    /**
     * Search for places by query string
     */
    suspend fun searchPlaetze(query: String): List<Platz> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return platzRepository.search(query.trim())
    }

    /**
     * Create a new place with business validation
     */
    suspend fun createPlatz(platz: Platz): Platz {
        validatePlatz(platz)
        return platzRepository.create(platz)
    }

    /**
     * Update an existing place
     */
    suspend fun updatePlatz(id: Uuid, platz: Platz): Platz? {
        validatePlatz(platz)
        return platzRepository.update(id, platz)
    }

    /**
     * Delete a place by ID
     */
    suspend fun deletePlatz(id: Uuid): Boolean {
        return platzRepository.delete(id)
    }

    /**
     * Validate place data according to business rules
     */
    private fun validatePlatz(platz: Platz) {
        if (platz.name.isBlank()) {
            throw IllegalArgumentException("Place name cannot be blank")
        }

        if (platz.name.length > 100) {
            throw IllegalArgumentException("Place name cannot exceed 100 characters")
        }

        platz.dimension?.let { dimension ->
            if (dimension.length > 50) {
                throw IllegalArgumentException("Place dimension cannot exceed 50 characters")
            }
        }

        platz.boden?.let { boden ->
            if (boden.length > 100) {
                throw IllegalArgumentException("Place boden cannot exceed 100 characters")
            }
        }
    }
}
