package at.mocode.services

import at.mocode.model.domaene.DomPferd
import at.mocode.repositories.DomPferdRepository
import com.benasher44.uuid.Uuid

/**
 * Service layer for DomPferd (Domain Horse) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class DomPferdService(private val domPferdRepository: DomPferdRepository) {

    /**
     * Retrieve all horses
     */
    suspend fun getAllPferde(): List<DomPferd> {
        return domPferdRepository.findAll()
    }

    /**
     * Find a horse by its unique identifier
     */
    suspend fun getPferdById(id: Uuid): DomPferd? {
        return domPferdRepository.findById(id)
    }

    /**
     * Find a horse by its OEPS Satz number
     */
    suspend fun getPferdByOepsSatzNr(oepsSatzNr: String): DomPferd? {
        if (oepsSatzNr.isBlank()) {
            throw IllegalArgumentException("OEPS Satz number cannot be blank")
        }
        return domPferdRepository.findByOepsSatzNr(oepsSatzNr)
    }

    /**
     * Find horses by name
     */
    suspend fun getPferdeByName(name: String): List<DomPferd> {
        if (name.isBlank()) {
            throw IllegalArgumentException("Horse name cannot be blank")
        }
        return domPferdRepository.findByName(name.trim())
    }

    /**
     * Find a horse by its life number (Lebensnummer)
     */
    suspend fun getPferdByLebensnummer(lebensnummer: String): DomPferd? {
        if (lebensnummer.isBlank()) {
            throw IllegalArgumentException("Life number cannot be blank")
        }
        return domPferdRepository.findByLebensnummer(lebensnummer)
    }

    /**
     * Find horses by owner ID
     */
    suspend fun getPferdeByBesitzerId(besitzerId: Uuid): List<DomPferd> {
        return domPferdRepository.findByBesitzerId(besitzerId)
    }

    /**
     * Find horses by responsible person ID
     */
    suspend fun getPferdeByVerantwortlichePersonId(personId: Uuid): List<DomPferd> {
        return domPferdRepository.findByVerantwortlichePersonId(personId)
    }

    /**
     * Find horses by home club ID
     */
    suspend fun getPferdeByHeimatVereinId(vereinId: Uuid): List<DomPferd> {
        return domPferdRepository.findByHeimatVereinId(vereinId)
    }

    /**
     * Find horses by breed
     */
    suspend fun getPferdeByRasse(rasse: String): List<DomPferd> {
        if (rasse.isBlank()) {
            throw IllegalArgumentException("Breed cannot be blank")
        }
        return domPferdRepository.findByRasse(rasse.trim())
    }

    /**
     * Find horses by birth year
     */
    suspend fun getPferdeByGeburtsjahr(geburtsjahr: Int): List<DomPferd> {
        if (geburtsjahr < 1900 || geburtsjahr > java.time.LocalDate.now().year) {
            throw IllegalArgumentException("Birth year must be between 1900 and current year")
        }
        return domPferdRepository.findByGeburtsjahr(geburtsjahr)
    }

    /**
     * Find all active horses
     */
    suspend fun getActivePferde(): List<DomPferd> {
        return domPferdRepository.findActiveHorses()
    }

    /**
     * Search for horses by query string
     */
    suspend fun searchPferde(query: String): List<DomPferd> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return domPferdRepository.search(query.trim())
    }

    /**
     * Create a new horse with business validation
     */
    suspend fun createPferd(domPferd: DomPferd): DomPferd {
        validatePferd(domPferd)

        // Check if OEPS Satz number already exists
        domPferd.oepsSatzNrPferd?.let { oepsNr ->
            val existing = domPferdRepository.findByOepsSatzNr(oepsNr)
            if (existing != null) {
                throw IllegalArgumentException("A horse with OEPS Satz number '$oepsNr' already exists")
            }
        }

        // Check if life number already exists
        domPferd.lebensnummer?.let { lebensnummer ->
            val existing = domPferdRepository.findByLebensnummer(lebensnummer)
            if (existing != null) {
                throw IllegalArgumentException("A horse with life number '$lebensnummer' already exists")
            }
        }

        return domPferdRepository.create(domPferd)
    }

    /**
     * Update an existing horse
     */
    suspend fun updatePferd(id: Uuid, domPferd: DomPferd): DomPferd? {
        validatePferd(domPferd)

        // Check if OEPS Satz number conflicts with another horse
        domPferd.oepsSatzNrPferd?.let { oepsNr ->
            val existing = domPferdRepository.findByOepsSatzNr(oepsNr)
            if (existing != null && existing.pferdId != id) {
                throw IllegalArgumentException("A horse with OEPS Satz number '$oepsNr' already exists")
            }
        }

        // Check if life number conflicts with another horse
        domPferd.lebensnummer?.let { lebensnummer ->
            val existing = domPferdRepository.findByLebensnummer(lebensnummer)
            if (existing != null && existing.pferdId != id) {
                throw IllegalArgumentException("A horse with life number '$lebensnummer' already exists")
            }
        }

        return domPferdRepository.update(id, domPferd)
    }

    /**
     * Delete a horse by ID
     */
    suspend fun deletePferd(id: Uuid): Boolean {
        return domPferdRepository.delete(id)
    }

    /**
     * Validate horse data according to business rules
     */
    private fun validatePferd(domPferd: DomPferd) {
        if (domPferd.name.isBlank()) {
            throw IllegalArgumentException("Horse name cannot be blank")
        }

        if (domPferd.name.length > 100) {
            throw IllegalArgumentException("Horse name cannot exceed 100 characters")
        }

        // Validate birth year if provided
        domPferd.geburtsjahr?.let { year ->
            if (year < 1900 || year > java.time.LocalDate.now().year) {
                throw IllegalArgumentException("Birth year must be between 1900 and current year")
            }
        }

        // Additional validation rules can be added here
        domPferd.oepsSatzNrPferd?.let { oepsNr ->
            if (oepsNr.isBlank()) {
                throw IllegalArgumentException("OEPS Satz number cannot be blank if provided")
            }
        }

        domPferd.lebensnummer?.let { lebensnummer ->
            if (lebensnummer.isBlank()) {
                throw IllegalArgumentException("Life number cannot be blank if provided")
            }
        }
    }
}
