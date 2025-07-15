package at.mocode.services

import at.mocode.events.EventPublisher
import at.mocode.events.TurnierCreatedEvent
import at.mocode.events.TurnierDeletedEvent
import at.mocode.events.TurnierUpdatedEvent
import at.mocode.model.Turnier
import at.mocode.repositories.TurnierRepository
import at.mocode.utils.TransactionManager
import com.benasher44.uuid.Uuid

/**
 * Service layer for Turnier (Tournament) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class TurnierService(
    private val turnierRepository: TurnierRepository,
    private val eventPublisher: EventPublisher = EventPublisher.getInstance()
) {

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
    suspend fun createTurnier(turnier: Turnier): Turnier = TransactionManager.withTransaction {
        validateTurnier(turnier)

        // Check if OEPS tournament number already exists
        turnier.oepsTurnierNr?.let { oepsNr ->
            val existing = turnierRepository.findByOepsTurnierNr(oepsNr)
            if (existing != null) {
                throw IllegalArgumentException("A tournament with OEPS number '$oepsNr' already exists")
            }
        }

        val createdTurnier = turnierRepository.create(turnier)

        // Publish tournament created event
        eventPublisher.publish(
            TurnierCreatedEvent(
                aggregateId = createdTurnier.id,
                turnier = createdTurnier
            )
        )

        return@withTransaction createdTurnier
    }

    /**
     * Update an existing tournament
     */
    suspend fun updateTurnier(id: Uuid, turnier: Turnier): Turnier? = TransactionManager.withTransaction {
        validateTurnier(turnier)

        // Get the previous tournament for the event
        val previousTurnier = turnierRepository.findById(id)
            ?: throw IllegalArgumentException("Tournament with ID $id not found")

        // Check if the OEPS tournament number conflicts with another tournament
        turnier.oepsTurnierNr?.let { oepsNr ->
            val existing = turnierRepository.findByOepsTurnierNr(oepsNr)
            if (existing != null && existing.id != id) {
                throw IllegalArgumentException("A tournament with OEPS number '$oepsNr' already exists")
            }
        }

        val updatedTurnier = turnierRepository.update(id, turnier)

        if (updatedTurnier != null) {
            // Publish tournament updated event
            eventPublisher.publish(
                TurnierUpdatedEvent(
                    aggregateId = updatedTurnier.id,
                    previousTurnier = previousTurnier,
                    updatedTurnier = updatedTurnier
                )
            )
        }

        return@withTransaction updatedTurnier
    }

    /**
     * Delete a tournament by ID
     */
    suspend fun deleteTurnier(id: Uuid): Boolean {
        // Get the tournament before deletion for the event
        val tournamentToDelete = turnierRepository.findById(id)

        val deleted = turnierRepository.delete(id)

        if (deleted && tournamentToDelete != null) {
            // Publish tournament deleted event
            eventPublisher.publish(
                TurnierDeletedEvent(
                    aggregateId = tournamentToDelete.id,
                    deletedTurnier = tournamentToDelete
                )
            )
        }

        return deleted
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
