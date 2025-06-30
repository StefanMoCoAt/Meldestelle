package at.mocode.services

import at.mocode.model.domaene.DomQualifikation
import at.mocode.repositories.DomQualifikationRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate

/**
 * Service layer for DomQualifikation (Domain Qualification) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class DomQualifikationService(private val domQualifikationRepository: DomQualifikationRepository) {

    /**
     * Retrieve all qualifications
     */
    suspend fun getAllQualifikationen(): List<DomQualifikation> {
        return domQualifikationRepository.findAll()
    }

    /**
     * Find a qualification by its unique identifier
     */
    suspend fun getQualifikationById(id: Uuid): DomQualifikation? {
        return domQualifikationRepository.findById(id)
    }

    /**
     * Find qualifications by person ID
     */
    suspend fun getQualifikationenByPersonId(personId: Uuid): List<DomQualifikation> {
        return domQualifikationRepository.findByPersonId(personId)
    }

    /**
     * Find qualifications by qualification type ID
     */
    suspend fun getQualifikationenByQualTypId(qualTypId: Uuid): List<DomQualifikation> {
        return domQualifikationRepository.findByQualTypId(qualTypId)
    }

    /**
     * Find active qualifications by person ID
     */
    suspend fun getActiveQualifikationenByPersonId(personId: Uuid): List<DomQualifikation> {
        return domQualifikationRepository.findActiveByPersonId(personId)
    }

    /**
     * Find qualifications by validity period
     */
    suspend fun getQualifikationenByValidityPeriod(fromDate: LocalDate?, toDate: LocalDate?): List<DomQualifikation> {
        // Validate date range if both dates are provided
        if (fromDate != null && toDate != null && fromDate > toDate) {
            throw IllegalArgumentException("From date must be before or equal to to date")
        }
        return domQualifikationRepository.findByValidityPeriod(fromDate, toDate)
    }

    /**
     * Search for qualifications by query string
     */
    suspend fun searchQualifikationen(query: String): List<DomQualifikation> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return domQualifikationRepository.search(query.trim())
    }

    /**
     * Create a new qualification with business validation
     */
    suspend fun createQualifikation(domQualifikation: DomQualifikation): DomQualifikation {
        validateQualifikation(domQualifikation)
        return domQualifikationRepository.create(domQualifikation)
    }

    /**
     * Update an existing qualification
     */
    suspend fun updateQualifikation(id: Uuid, domQualifikation: DomQualifikation): DomQualifikation? {
        validateQualifikation(domQualifikation)
        return domQualifikationRepository.update(id, domQualifikation)
    }

    /**
     * Delete a qualification by ID
     */
    suspend fun deleteQualifikation(id: Uuid): Boolean {
        return domQualifikationRepository.delete(id)
    }

    /**
     * Check if a person has an active qualification of a specific type
     */
    suspend fun hasActiveQualification(personId: Uuid, qualTypId: Uuid): Boolean {
        val activeQualifications = getActiveQualifikationenByPersonId(personId)
        return activeQualifications.any { it.qualTypId == qualTypId }
    }

    /**
     * Get current valid qualifications for a person
     */
    suspend fun getCurrentValidQualifikationenByPersonId(personId: Uuid): List<DomQualifikation> {
        val currentJavaDate = java.time.LocalDate.now()
        val currentLocalDate = kotlinx.datetime.LocalDate(currentJavaDate.year, currentJavaDate.monthValue, currentJavaDate.dayOfMonth)

        val allPersonQualifications = getQualifikationenByPersonId(personId)
        return allPersonQualifications.filter { qualification ->
            qualification.istAktiv &&
            (qualification.gueltigVon == null || qualification.gueltigVon!! <= currentLocalDate) &&
            (qualification.gueltigBis == null || qualification.gueltigBis!! >= currentLocalDate)
        }
    }

    /**
     * Deactivate a qualification (soft delete)
     */
    suspend fun deactivateQualifikation(id: Uuid): DomQualifikation? {
        val qualification = getQualifikationById(id)
        return if (qualification != null) {
            val updatedQualification = qualification.copy(istAktiv = false)
            updateQualifikation(id, updatedQualification)
        } else {
            null
        }
    }

    /**
     * Validate qualification data according to business rules
     */
    private fun validateQualifikation(domQualifikation: DomQualifikation) {
        // Validate validity date range if both dates are provided
        if (domQualifikation.gueltigVon != null && domQualifikation.gueltigBis != null) {
            if (domQualifikation.gueltigVon!! > domQualifikation.gueltigBis!!) {
                throw IllegalArgumentException("Qualification validity start date must be before or equal to end date")
            }
        }

        // Validate that gueltigBis is not in the past for new active qualifications
        if (domQualifikation.istAktiv && domQualifikation.gueltigBis != null) {
            val currentJavaDate = java.time.LocalDate.now()
            val currentLocalDate = kotlinx.datetime.LocalDate(currentJavaDate.year, currentJavaDate.monthValue, currentJavaDate.dayOfMonth)

            if (domQualifikation.gueltigBis!! < currentLocalDate) {
                throw IllegalArgumentException("Cannot create active qualification with end date in the past")
            }
        }

        // Additional validation rules can be added here
        domQualifikation.bemerkung?.let { bemerkung ->
            if (bemerkung.length > 1000) {
                throw IllegalArgumentException("Qualification remark cannot exceed 1000 characters")
            }
        }
    }
}
