package at.mocode.services

import at.mocode.model.Turnier
import at.mocode.repositories.TurnierRepository
import com.benasher44.uuid.Uuid

/**
 * Service layer for Turnier (Tournament) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class TurnierService(private val turnierRepository: TurnierRepository) {

    /**
     * Retrieve all tournaments
     */
    suspend fun getAllTurniere(): List<Turnier> {
        return turnierRepository.findAll()
    }

    /**
     * Find a tournament by its unique identifier
     */
    suspend fun getTurnierById(id: Uuid): Turnier? {
        return turnierRepository.findById(id)
    }

    /**
     * Find tournaments by event (Veranstaltung) ID
     */
    suspend fun getTurniereByVeranstaltungId(veranstaltungId: Uuid): List<Turnier> {
        return turnierRepository.findByVeranstaltungId(veranstaltungId)
    }

    /**
     * Find a tournament by its OEPS tournament number
     */
    suspend fun getTurnierByOepsTurnierNr(oepsTurnierNr: String): Turnier? {
        if (oepsTurnierNr.isBlank()) {
            throw IllegalArgumentException("OEPS tournament number cannot be blank")
        }
        return turnierRepository.findByOepsTurnierNr(oepsTurnierNr)
    }

    /**
     * Search for tournaments by query string
     */
    suspend fun searchTurniere(query: String): List<Turnier> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return turnierRepository.search(query.trim())
    }

    /**
     * Create a new tournament with business validation
     */
    suspend fun createTurnier(turnier: Turnier): Turnier {
        validateTurnier(turnier)

        // Check if OEPS tournament number already exists
        turnier.oepsTurnierNr?.let { oepsNr ->
            val existing = turnierRepository.findByOepsTurnierNr(oepsNr)
            if (existing != null) {
                throw IllegalArgumentException("A tournament with OEPS number '$oepsNr' already exists")
            }
        }

        return turnierRepository.create(turnier)
    }

    /**
     * Update an existing tournament
     */
    suspend fun updateTurnier(id: Uuid, turnier: Turnier): Turnier? {
        validateTurnier(turnier)

        // Check if OEPS tournament number conflicts with another tournament
        turnier.oepsTurnierNr?.let { oepsNr ->
            val existing = turnierRepository.findByOepsTurnierNr(oepsNr)
            if (existing != null && existing.id != id) {
                throw IllegalArgumentException("A tournament with OEPS number '$oepsNr' already exists")
            }
        }

        return turnierRepository.update(id, turnier)
    }

    /**
     * Delete a tournament by ID
     */
    suspend fun deleteTurnier(id: Uuid): Boolean {
        return turnierRepository.delete(id)
    }

    /**
     * Get tournaments for a specific event
     */
    suspend fun getTurniereForEvent(veranstaltungId: Uuid): List<Turnier> {
        return getTurniereByVeranstaltungId(veranstaltungId)
    }

    /**
     * Validate tournament data according to business rules
     */
    private fun validateTurnier(turnier: Turnier) {
        if (turnier.titel.isBlank()) {
            throw IllegalArgumentException("Tournament title cannot be blank")
        }

        if (turnier.titel.length > 255) {
            throw IllegalArgumentException("Tournament title cannot exceed 255 characters")
        }

        // Validate dates
        if (turnier.datumVon > turnier.datumBis) {
            throw IllegalArgumentException("Tournament start date must be before or equal to end date")
        }

        // Additional validation rules can be added here
        if (turnier.oepsTurnierNr.isBlank()) {
            throw IllegalArgumentException("OEPS tournament number cannot be blank")
        }
    }
}
