package at.mocode.services

import at.mocode.stammdaten.Person
import at.mocode.repositories.PersonRepository
import com.benasher44.uuid.Uuid

/**
 * Service layer for Person business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class PersonService(private val personRepository: PersonRepository) {

    /**
     * Retrieve all persons
     */
    suspend fun getAllPersons(): List<Person> {
        return personRepository.findAll()
    }

    /**
     * Find a person by their unique identifier
     */
    suspend fun getPersonById(id: Uuid): Person? {
        return personRepository.findById(id)
    }

    /**
     * Find a person by their OEPS Satz number
     */
    suspend fun getPersonByOepsSatzNr(oepsSatzNr: String): Person? {
        if (oepsSatzNr.isBlank()) {
            throw IllegalArgumentException("OEPS Satz number cannot be blank")
        }
        return personRepository.findByOepsSatzNr(oepsSatzNr)
    }

    /**
     * Find persons by Verein (club) ID
     */
    suspend fun getPersonsByVereinId(vereinId: Uuid): List<Person> {
        return personRepository.findByVereinId(vereinId)
    }

    /**
     * Search for persons by query string
     */
    suspend fun searchPersons(query: String): List<Person> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return personRepository.search(query.trim())
    }

    /**
     * Create a new person with business validation
     */
    suspend fun createPerson(person: Person): Person {
        validatePerson(person)

        // Check if OEPS Satz number already exists
        person.oepsSatzNr?.let { oepsNr ->
            val existing = personRepository.findByOepsSatzNr(oepsNr)
            if (existing != null) {
                throw IllegalArgumentException("A person with OEPS Satz number '$oepsNr' already exists")
            }
        }

        return personRepository.create(person)
    }

    /**
     * Update an existing person
     */
    suspend fun updatePerson(id: Uuid, person: Person): Person? {
        validatePerson(person)

        // Check if OEPS Satz number conflicts with another person
        person.oepsSatzNr?.let { oepsNr ->
            val existing = personRepository.findByOepsSatzNr(oepsNr)
            if (existing != null && existing.id != id) {
                throw IllegalArgumentException("A person with OEPS Satz number '$oepsNr' already exists")
            }
        }

        return personRepository.update(id, person)
    }

    /**
     * Delete a person by ID
     */
    suspend fun deletePerson(id: Uuid): Boolean {
        return personRepository.delete(id)
    }

    /**
     * Validate person data according to business rules
     */
    private fun validatePerson(person: Person) {
        if (person.vorname.isBlank()) {
            throw IllegalArgumentException("Person first name cannot be blank")
        }

        if (person.nachname.isBlank()) {
            throw IllegalArgumentException("Person last name cannot be blank")
        }

        if (person.vorname.length > 100) {
            throw IllegalArgumentException("Person first name cannot exceed 100 characters")
        }

        if (person.nachname.length > 100) {
            throw IllegalArgumentException("Person last name cannot exceed 100 characters")
        }

        // Additional validation rules can be added here
        person.oepsSatzNr?.let { oepsNr ->
            if (oepsNr.isBlank()) {
                throw IllegalArgumentException("OEPS Satz number cannot be blank if provided")
            }
        }
    }
}
