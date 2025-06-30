package at.mocode.services

import at.mocode.model.Veranstaltung
import at.mocode.repositories.VeranstaltungRepository
import com.benasher44.uuid.Uuid

/**
 * Service layer for Veranstaltung (Event) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class VeranstaltungService(private val veranstaltungRepository: VeranstaltungRepository) {

    /**
     * Retrieve all events
     */
    suspend fun getAllVeranstaltungen(): List<Veranstaltung> {
        return veranstaltungRepository.findAll()
    }

    /**
     * Find an event by its unique identifier
     */
    suspend fun getVeranstaltungById(id: Uuid): Veranstaltung? {
        return veranstaltungRepository.findById(id)
    }

    /**
     * Find events by name
     */
    suspend fun getVeranstaltungenByName(name: String): List<Veranstaltung> {
        if (name.isBlank()) {
            throw IllegalArgumentException("Event name cannot be blank")
        }
        return veranstaltungRepository.findByName(name.trim())
    }

    /**
     * Find events by organizer OEPS number
     */
    suspend fun getVeranstaltungenByVeranstalterOepsNummer(oepsNummer: String): List<Veranstaltung> {
        if (oepsNummer.isBlank()) {
            throw IllegalArgumentException("Organizer OEPS number cannot be blank")
        }
        return veranstaltungRepository.findByVeranstalterOepsNummer(oepsNummer.trim())
    }

    /**
     * Search for events by query string
     */
    suspend fun searchVeranstaltungen(query: String): List<Veranstaltung> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return veranstaltungRepository.search(query.trim())
    }

    /**
     * Create a new event with business validation
     */
    suspend fun createVeranstaltung(veranstaltung: Veranstaltung): Veranstaltung {
        validateVeranstaltung(veranstaltung)
        return veranstaltungRepository.create(veranstaltung)
    }

    /**
     * Update an existing event
     */
    suspend fun updateVeranstaltung(id: Uuid, veranstaltung: Veranstaltung): Veranstaltung? {
        validateVeranstaltung(veranstaltung)
        return veranstaltungRepository.update(id, veranstaltung)
    }

    /**
     * Delete an event by ID
     */
    suspend fun deleteVeranstaltung(id: Uuid): Boolean {
        return veranstaltungRepository.delete(id)
    }

    /**
     * Get events happening in a specific year
     */
    suspend fun getVeranstaltungenByYear(year: Int): List<Veranstaltung> {
        if (year < 1900 || year > 2100) {
            throw IllegalArgumentException("Year must be between 1900 and 2100")
        }

        val allEvents = getAllVeranstaltungen()
        return allEvents.filter { event ->
            event.datumVon.year == year || event.datumBis.year == year ||
            (event.datumVon.year < year && event.datumBis.year > year)
        }
    }

    /**
     * Get current events (happening now or in the future)
     */
    suspend fun getCurrentAndFutureVeranstaltungen(): List<Veranstaltung> {
        val currentJavaDate = java.time.LocalDate.now()
        val currentLocalDate = kotlinx.datetime.LocalDate(currentJavaDate.year, currentJavaDate.monthValue, currentJavaDate.dayOfMonth)

        val allEvents = getAllVeranstaltungen()
        return allEvents.filter { event ->
            event.datumBis >= currentLocalDate
        }.sortedBy { it.datumVon }
    }

    /**
     * Get past events
     */
    suspend fun getPastVeranstaltungen(): List<Veranstaltung> {
        val currentJavaDate = java.time.LocalDate.now()
        val currentLocalDate = kotlinx.datetime.LocalDate(currentJavaDate.year, currentJavaDate.monthValue, currentJavaDate.dayOfMonth)

        val allEvents = getAllVeranstaltungen()
        return allEvents.filter { event ->
            event.datumBis < currentLocalDate
        }.sortedByDescending { it.datumVon }
    }

    /**
     * Get events by organizer name
     */
    suspend fun getVeranstaltungenByVeranstalterName(veranstalterName: String): List<Veranstaltung> {
        if (veranstalterName.isBlank()) {
            throw IllegalArgumentException("Organizer name cannot be blank")
        }

        val allEvents = getAllVeranstaltungen()
        return allEvents.filter { event ->
            event.veranstalterName.contains(veranstalterName.trim(), ignoreCase = true)
        }
    }

    /**
     * Get events by venue name
     */
    suspend fun getVeranstaltungenByVenanstaltungsort(veranstaltungsortName: String): List<Veranstaltung> {
        if (veranstaltungsortName.isBlank()) {
            throw IllegalArgumentException("Venue name cannot be blank")
        }

        val allEvents = getAllVeranstaltungen()
        return allEvents.filter { event ->
            event.veranstaltungsortName.contains(veranstaltungsortName.trim(), ignoreCase = true)
        }
    }

    /**
     * Validate event data according to business rules
     */
    private fun validateVeranstaltung(veranstaltung: Veranstaltung) {
        if (veranstaltung.name.isBlank()) {
            throw IllegalArgumentException("Event name cannot be blank")
        }

        if (veranstaltung.name.length > 255) {
            throw IllegalArgumentException("Event name cannot exceed 255 characters")
        }

        if (veranstaltung.veranstalterName.isBlank()) {
            throw IllegalArgumentException("Organizer name cannot be blank")
        }

        if (veranstaltung.veranstalterName.length > 255) {
            throw IllegalArgumentException("Organizer name cannot exceed 255 characters")
        }

        if (veranstaltung.veranstaltungsortName.isBlank()) {
            throw IllegalArgumentException("Venue name cannot be blank")
        }

        if (veranstaltung.veranstaltungsortName.length > 255) {
            throw IllegalArgumentException("Venue name cannot exceed 255 characters")
        }

        if (veranstaltung.veranstaltungsortAdresse.isBlank()) {
            throw IllegalArgumentException("Venue address cannot be blank")
        }

        if (veranstaltung.veranstaltungsortAdresse.length > 500) {
            throw IllegalArgumentException("Venue address cannot exceed 500 characters")
        }

        // Validate date range
        if (veranstaltung.datumVon > veranstaltung.datumBis) {
            throw IllegalArgumentException("Event start date must be before or equal to end date")
        }

        // Validate optional fields
        veranstaltung.veranstalterOepsNummer?.let { oepsNr ->
            if (oepsNr.isBlank()) {
                throw IllegalArgumentException("Organizer OEPS number cannot be blank if provided")
            }
        }

        veranstaltung.kontaktpersonName?.let { name ->
            if (name.length > 255) {
                throw IllegalArgumentException("Contact person name cannot exceed 255 characters")
            }
        }

        veranstaltung.kontaktTelefon?.let { telefon ->
            if (telefon.length > 50) {
                throw IllegalArgumentException("Contact phone cannot exceed 50 characters")
            }
        }

        veranstaltung.kontaktEmail?.let { email ->
            if (email.length > 255) {
                throw IllegalArgumentException("Contact email cannot exceed 255 characters")
            }
            // Basic email validation
            if (!email.contains("@") || !email.contains(".")) {
                throw IllegalArgumentException("Contact email must be a valid email address")
            }
        }

        veranstaltung.webseite?.let { webseite ->
            if (webseite.length > 500) {
                throw IllegalArgumentException("Website URL cannot exceed 500 characters")
            }
        }

        veranstaltung.dsgvoText?.let { text ->
            if (text.length > 2000) {
                throw IllegalArgumentException("DSGVO text cannot exceed 2000 characters")
            }
        }

        veranstaltung.haftungsText?.let { text ->
            if (text.length > 2000) {
                throw IllegalArgumentException("Liability text cannot exceed 2000 characters")
            }
        }

        veranstaltung.sonstigeBesondereBestimmungen?.let { text ->
            if (text.length > 2000) {
                throw IllegalArgumentException("Special provisions text cannot exceed 2000 characters")
            }
        }

        // Additional validation rules can be added here
    }
}
