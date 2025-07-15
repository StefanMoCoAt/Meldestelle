package at.mocode.services

import at.mocode.model.Bewerb
import at.mocode.repositories.BewerbRepository
import at.mocode.utils.TransactionManager
import com.benasher44.uuid.Uuid

/**
 * Service layer for Bewerb (Competition) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class BewerbService(private val bewerbRepository: BewerbRepository) {

    /**
     * Retrieve all competitions
     */
    suspend fun getAllBewerbe(): List<Bewerb> {
        return bewerbRepository.findAll()
    }

    /**
     * Find a competition by its unique identifier
     */
    suspend fun getBewerbById(id: Uuid): Bewerb? {
        return bewerbRepository.findById(id)
    }

    /**
     * Find competitions by tournament ID
     */
    suspend fun getBewerbeByTurnierId(turnierId: Uuid): List<Bewerb> {
        return bewerbRepository.findByTurnierId(turnierId)
    }

    /**
     * Find competitions by sport category (Sparte)
     */
    suspend fun getBewerbeBySparte(sparte: String): List<Bewerb> {
        if (sparte.isBlank()) {
            throw IllegalArgumentException("Sparte cannot be blank")
        }
        return bewerbRepository.findBySparte(sparte.trim())
    }

    /**
     * Find competitions by class
     */
    suspend fun getBewerbeByKlasse(klasse: String): List<Bewerb> {
        if (klasse.isBlank()) {
            throw IllegalArgumentException("Klasse cannot be blank")
        }
        return bewerbRepository.findByKlasse(klasse.trim())
    }

    /**
     * Find competitions by start list finalization status
     */
    suspend fun getBewerbeByStartlisteFinal(istFinal: Boolean): List<Bewerb> {
        return bewerbRepository.findByStartlisteFinal(istFinal)
    }

    /**
     * Find competitions by result list finalization status
     */
    suspend fun getBewerbeByErgebnislisteFinal(istFinal: Boolean): List<Bewerb> {
        return bewerbRepository.findByErgebnislisteFinal(istFinal)
    }

    /**
     * Get competitions with finalized start lists
     */
    suspend fun getBewerbeWithFinalStartliste(): List<Bewerb> {
        return getBewerbeByStartlisteFinal(true)
    }

    /**
     * Get competitions with finalized result lists
     */
    suspend fun getBewerbeWithFinalErgebnisliste(): List<Bewerb> {
        return getBewerbeByErgebnislisteFinal(true)
    }

    /**
     * Search for competitions by query string
     */
    suspend fun searchBewerbe(query: String): List<Bewerb> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return bewerbRepository.search(query.trim())
    }

    /**
     * Create a new competition with business validation
     */
    suspend fun createBewerb(bewerb: Bewerb): Bewerb {
        validateBewerb(bewerb)
        return bewerbRepository.create(bewerb)
    }

    /**
     * Update an existing competition
     */
    suspend fun updateBewerb(id: Uuid, bewerb: Bewerb): Bewerb? {
        validateBewerb(bewerb)
        return bewerbRepository.update(id, bewerb)
    }

    /**
     * Delete a competition by ID
     */
    suspend fun deleteBewerb(id: Uuid): Boolean {
        return bewerbRepository.delete(id)
    }

    /**
     * Finalize start list for a competition
     */
    suspend fun finalizeStartliste(id: Uuid): Bewerb? = TransactionManager.withTransaction {
        val bewerb = getBewerbById(id)
        return@withTransaction if (bewerb != null) {
            val updatedBewerb = bewerb.copy(istStartlisteFinal = true)
            updateBewerb(id, updatedBewerb)
        } else {
            null
        }
    }

    /**
     * Finalize the result list for a competition
     */
    suspend fun finalizeErgebnisliste(id: Uuid): Bewerb? = TransactionManager.withTransaction {
        val bewerb = getBewerbById(id)
        return@withTransaction if (bewerb != null) {
            val updatedBewerb = bewerb.copy(istErgebnislisteFinal = true)
            updateBewerb(id, updatedBewerb)
        } else {
            null
        }
    }

    /**
     * Reopen the start list for a competition
     */
    suspend fun reopenStartliste(id: Uuid): Bewerb? = TransactionManager.withTransaction {
        val bewerb = getBewerbById(id)
        return@withTransaction if (bewerb != null) {
            val updatedBewerb = bewerb.copy(istStartlisteFinal = false)
            updateBewerb(id, updatedBewerb)
        } else {
            null
        }
    }

    /**
     * Reopen the result list for a competition
     */
    suspend fun reopenErgebnisliste(id: Uuid): Bewerb? = TransactionManager.withTransaction {
        val bewerb = getBewerbById(id)
        return@withTransaction if (bewerb != null) {
            val updatedBewerb = bewerb.copy(istErgebnislisteFinal = false)
            updateBewerb(id, updatedBewerb)
        } else {
            null
        }
    }

    /**
     * Get competitions for a specific tournament ordered by number
     */
    suspend fun getBewerbeForTurnierOrdered(turnierId: Uuid): List<Bewerb> {
        val bewerbe = getBewerbeByTurnierId(turnierId)
        return bewerbe.sortedBy { it.nummer }
    }

    /**
     * Validate competition data according to business rules
     */
    private fun validateBewerb(bewerb: Bewerb) {
        if (bewerb.nummer.isBlank()) {
            throw IllegalArgumentException("Competition number cannot be blank")
        }

        if (bewerb.nummer.length > 50) {
            throw IllegalArgumentException("Competition number cannot exceed 50 characters")
        }

        if (bewerb.bezeichnungOffiziell.isBlank()) {
            throw IllegalArgumentException("Official designation cannot be blank")
        }

        if (bewerb.bezeichnungOffiziell.length > 255) {
            throw IllegalArgumentException("Official designation cannot exceed 255 characters")
        }

        // Validate participant constraints
        if (bewerb.maxPferdeProReiter != null && bewerb.maxPferdeProReiter!! < 1) {
            throw IllegalArgumentException("Maximum horses per rider must be at least 1")
        }

        if (bewerb.anzahlRichterGeplant != null && bewerb.anzahlRichterGeplant!! < 1) {
            throw IllegalArgumentException("Number of planned judges must be at least 1")
        }

        // Validate timing constraints
        if (bewerb.standardDauerProStartGeschaetztSek != null && bewerb.standardDauerProStartGeschaetztSek!! < 0) {
            throw IllegalArgumentException("Estimated duration per start cannot be negative")
        }

        if (bewerb.standardUmbauzeitNachBewerbMin != null && bewerb.standardUmbauzeitNachBewerbMin!! < 0) {
            throw IllegalArgumentException("Setup time after competition cannot be negative")
        }

        if (bewerb.standardBesichtigungszeitVorBewerbMin != null && bewerb.standardBesichtigungszeitVorBewerbMin!! < 0) {
            throw IllegalArgumentException("Inspection time before competition cannot be negative")
        }

        if (bewerb.standardStechzeitZusaetzlichMin != null && bewerb.standardStechzeitZusaetzlichMin!! < 0) {
            throw IllegalArgumentException("Additional jump-off time cannot be negative")
        }

        // Validate text field lengths
        bewerb.internerName?.let { name ->
            if (name.length > 255) {
                throw IllegalArgumentException("Internal name cannot exceed 255 characters")
            }
        }

        bewerb.klasse?.let { klasse ->
            if (klasse.length > 100) {
                throw IllegalArgumentException("Class cannot exceed 100 characters")
            }
        }

        bewerb.kategorieOetoDesBewerbs?.let { kategorie ->
            if (kategorie.length > 100) {
                throw IllegalArgumentException("ÖTO category cannot exceed 100 characters")
            }
        }

        bewerb.teilnahmebedingungenText?.let { text ->
            if (text.length > 1000) {
                throw IllegalArgumentException("Participation conditions text cannot exceed 1000 characters")
            }
        }

        bewerb.notizenIntern?.let { notizen ->
            if (notizen.length > 1000) {
                throw IllegalArgumentException("Internal notes cannot exceed 1000 characters")
            }
        }

        // Additional validation rules can be added here
    }
}
