package at.mocode.services

import at.mocode.model.Abteilung
import at.mocode.repositories.AbteilungRepository
import com.benasher44.uuid.Uuid

/**
 * Service layer for Abteilung (Division/Section) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class AbteilungService(private val abteilungRepository: AbteilungRepository) {

    /**
     * Retrieve all divisions
     */
    suspend fun getAllAbteilungen(): List<Abteilung> {
        return abteilungRepository.findAll()
    }

    /**
     * Find a division by its unique identifier
     */
    suspend fun getAbteilungById(id: Uuid): Abteilung? {
        return abteilungRepository.findById(id)
    }

    /**
     * Find divisions by competition (Bewerb) ID
     */
    suspend fun getAbteilungenByBewerbId(bewerbId: Uuid): List<Abteilung> {
        return abteilungRepository.findByBewerbId(bewerbId)
    }

    /**
     * Find divisions by active status
     */
    suspend fun getAbteilungenByAktiv(istAktiv: Boolean): List<Abteilung> {
        return abteilungRepository.findByAktiv(istAktiv)
    }

    /**
     * Get all active divisions
     */
    suspend fun getActiveAbteilungen(): List<Abteilung> {
        return getAbteilungenByAktiv(true)
    }

    /**
     * Get all inactive divisions
     */
    suspend fun getInactiveAbteilungen(): List<Abteilung> {
        return getAbteilungenByAktiv(false)
    }

    /**
     * Search for divisions by query string
     */
    suspend fun searchAbteilungen(query: String): List<Abteilung> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return abteilungRepository.search(query.trim())
    }

    /**
     * Create a new division with business validation
     */
    suspend fun createAbteilung(abteilung: Abteilung): Abteilung {
        validateAbteilung(abteilung)
        return abteilungRepository.create(abteilung)
    }

    /**
     * Update an existing division
     */
    suspend fun updateAbteilung(id: Uuid, abteilung: Abteilung): Abteilung? {
        validateAbteilung(abteilung)
        return abteilungRepository.update(id, abteilung)
    }

    /**
     * Delete a division by ID
     */
    suspend fun deleteAbteilung(id: Uuid): Boolean {
        return abteilungRepository.delete(id)
    }

    /**
     * Deactivate a division (soft delete)
     */
    suspend fun deactivateAbteilung(id: Uuid): Abteilung? {
        val abteilung = getAbteilungById(id)
        return if (abteilung != null) {
            val updatedAbteilung = abteilung.copy(istAktiv = false)
            updateAbteilung(id, updatedAbteilung)
        } else {
            null
        }
    }

    /**
     * Activate a division
     */
    suspend fun activateAbteilung(id: Uuid): Abteilung? {
        val abteilung = getAbteilungById(id)
        return if (abteilung != null) {
            val updatedAbteilung = abteilung.copy(istAktiv = true)
            updateAbteilung(id, updatedAbteilung)
        } else {
            null
        }
    }

    /**
     * Get divisions for a specific competition ordered by sequence
     */
    suspend fun getAbteilungenForBewerbOrdered(bewerbId: Uuid): List<Abteilung> {
        val abteilungen = getAbteilungenByBewerbId(bewerbId)
        // Sort by abteilungsKennzeichen for basic ordering
        return abteilungen.sortedBy { it.abteilungsKennzeichen }
    }

    /**
     * Validate division data according to business rules
     */
    private fun validateAbteilung(abteilung: Abteilung) {
        if (abteilung.abteilungsKennzeichen.isBlank()) {
            throw IllegalArgumentException("Division identifier (abteilungsKennzeichen) cannot be blank")
        }

        if (abteilung.abteilungsKennzeichen.length > 50) {
            throw IllegalArgumentException("Division identifier cannot exceed 50 characters")
        }

        // Validate participant count constraints
        if (abteilung.teilungsKriteriumAnzahlMin != null && abteilung.teilungsKriteriumAnzahlMin!! < 0) {
            throw IllegalArgumentException("Minimum participant count cannot be negative")
        }

        if (abteilung.teilungsKriteriumAnzahlMax != null && abteilung.teilungsKriteriumAnzahlMax!! < 0) {
            throw IllegalArgumentException("Maximum participant count cannot be negative")
        }

        if (abteilung.teilungsKriteriumAnzahlMin != null && abteilung.teilungsKriteriumAnzahlMax != null) {
            if (abteilung.teilungsKriteriumAnzahlMin!! > abteilung.teilungsKriteriumAnzahlMax!!) {
                throw IllegalArgumentException("Minimum participant count cannot be greater than maximum")
            }
        }

        // Validate timing constraints
        if (abteilung.dauerProStartGeschaetztSek != null && abteilung.dauerProStartGeschaetztSek!! < 0) {
            throw IllegalArgumentException("Estimated duration per start cannot be negative")
        }

        if (abteilung.umbauzeitNachAbteilungMin != null && abteilung.umbauzeitNachAbteilungMin!! < 0) {
            throw IllegalArgumentException("Setup time after division cannot be negative")
        }

        if (abteilung.besichtigungszeitVorAbteilungMin != null && abteilung.besichtigungszeitVorAbteilungMin!! < 0) {
            throw IllegalArgumentException("Inspection time before division cannot be negative")
        }

        if (abteilung.stechzeitZusaetzlichMin != null && abteilung.stechzeitZusaetzlichMin!! < 0) {
            throw IllegalArgumentException("Additional jump-off time cannot be negative")
        }

        if (abteilung.anzahlStarter < 0) {
            throw IllegalArgumentException("Number of starters cannot be negative")
        }

        // Validate text field lengths
        abteilung.bezeichnungIntern?.let { bezeichnung ->
            if (bezeichnung.length > 255) {
                throw IllegalArgumentException("Internal designation cannot exceed 255 characters")
            }
        }

        abteilung.bezeichnungAufStartliste?.let { bezeichnung ->
            if (bezeichnung.length > 255) {
                throw IllegalArgumentException("Start list designation cannot exceed 255 characters")
            }
        }

        abteilung.teilungsKriteriumFreiText?.let { freiText ->
            if (freiText.length > 500) {
                throw IllegalArgumentException("Free text division criterion cannot exceed 500 characters")
            }
        }

        // Additional validation rules can be added here
    }
}
