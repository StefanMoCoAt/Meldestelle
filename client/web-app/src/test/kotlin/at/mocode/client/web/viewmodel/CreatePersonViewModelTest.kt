package at.mocode.client.web.viewmodel

import at.mocode.client.common.repository.Person
import at.mocode.client.common.repository.PersonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

/**
 * Simplified test suite for client-side Person functionality.
 *
 * This test focuses on the client-layer PersonRepository without domain dependencies.
 * Tests cover basic CRUD operations through the client repository interface.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreatePersonViewModelTest {

    private lateinit var mockPersonRepository: PersonRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        setupMockRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Sets up mock repository for testing
     */
    private fun setupMockRepository() {
        mockPersonRepository = object : PersonRepository {
            private val persons = mutableListOf<Person>()

            override suspend fun save(person: Person): Person {
                val savedPerson = if (person.id.isBlank()) {
                    person.copy(id = "test-id-${persons.size + 1}")
                } else {
                    person
                }
                persons.removeIf { it.id == savedPerson.id }
                persons.add(savedPerson)
                return savedPerson
            }

            override suspend fun findById(id: String): Person? {
                return persons.find { it.id == id }
            }

            override suspend fun findByName(searchTerm: String, limit: Int): List<Person> {
                return persons.filter {
                    it.vorname.contains(searchTerm, ignoreCase = true) ||
                    it.nachname.contains(searchTerm, ignoreCase = true)
                }.take(limit)
            }

            override suspend fun findAllActive(limit: Int, offset: Int): List<Person> {
                return persons.filter { !it.istGesperrt }.drop(offset).take(limit)
            }

            override suspend fun delete(id: String): Boolean {
                return persons.removeIf { it.id == id }
            }

            override suspend fun countActive(): Long {
                return persons.filter { !it.istGesperrt }.size.toLong()
            }
        }
    }

    @Test
    fun `test person repository save creates new person`() = runTest {
        // Given
        val newPerson = Person(
            nachname = "Mustermann",
            vorname = "Max",
            email = "max@example.com"
        )

        // When
        val savedPerson = mockPersonRepository.save(newPerson)

        // Then
        assertNotNull(savedPerson.id)
        assertTrue(savedPerson.id.isNotBlank())
        assertEquals("Mustermann", savedPerson.nachname)
        assertEquals("Max", savedPerson.vorname)
        assertEquals("max@example.com", savedPerson.email)
    }

    @Test
    fun `test person repository save updates existing person`() = runTest {
        // Given
        val person = Person(
            id = "existing-id",
            nachname = "Mustermann",
            vorname = "Max",
            email = "max@example.com"
        )
        mockPersonRepository.save(person)

        // When
        val updatedPerson = person.copy(email = "max.updated@example.com")
        val savedPerson = mockPersonRepository.save(updatedPerson)

        // Then
        assertEquals("existing-id", savedPerson.id)
        assertEquals("max.updated@example.com", savedPerson.email)
    }

    @Test
    fun `test person repository findById returns correct person`() = runTest {
        // Given
        val person = Person(
            nachname = "Mustermann",
            vorname = "Max",
            email = "max@example.com"
        )
        val savedPerson = mockPersonRepository.save(person)

        // When
        val foundPerson = mockPersonRepository.findById(savedPerson.id)

        // Then
        assertNotNull(foundPerson)
        assertEquals(savedPerson.id, foundPerson.id)
        assertEquals("Mustermann", foundPerson.nachname)
        assertEquals("Max", foundPerson.vorname)
    }

    @Test
    fun `test person repository findById returns null for non-existent id`() = runTest {
        // When
        val foundPerson = mockPersonRepository.findById("non-existent-id")

        // Then
        assertNull(foundPerson)
    }

    @Test
    fun `test person repository findByName returns matching persons`() = runTest {
        // Given
        val person1 = Person(nachname = "Mustermann", vorname = "Max")
        val person2 = Person(nachname = "Schmidt", vorname = "Anna")
        val person3 = Person(nachname = "Mueller", vorname = "Max")

        mockPersonRepository.save(person1)
        mockPersonRepository.save(person2)
        mockPersonRepository.save(person3)

        // When
        val foundPersons = mockPersonRepository.findByName("Max", 10)

        // Then
        assertEquals(2, foundPersons.size)
        assertTrue(foundPersons.any { it.vorname == "Max" && it.nachname == "Mustermann" })
        assertTrue(foundPersons.any { it.vorname == "Max" && it.nachname == "Mueller" })
    }

    @Test
    fun `test person repository findAllActive returns only active persons`() = runTest {
        // Given
        val activePerson = Person(nachname = "Active", vorname = "Person", istGesperrt = false)
        val blockedPerson = Person(nachname = "Blocked", vorname = "Person", istGesperrt = true)

        mockPersonRepository.save(activePerson)
        mockPersonRepository.save(blockedPerson)

        // When
        val activePersons = mockPersonRepository.findAllActive(10, 0)

        // Then
        assertEquals(1, activePersons.size)
        assertEquals("Active", activePersons.first().nachname)
        assertFalse(activePersons.first().istGesperrt)
    }

    @Test
    fun `test person repository delete removes person`() = runTest {
        // Given
        val person = Person(nachname = "ToDelete", vorname = "Person")
        val savedPerson = mockPersonRepository.save(person)

        // When
        val deleted = mockPersonRepository.delete(savedPerson.id)

        // Then
        assertTrue(deleted)
        assertNull(mockPersonRepository.findById(savedPerson.id))
    }

    @Test
    fun `test person repository countActive returns correct count`() = runTest {
        // Given
        val activePerson1 = Person(nachname = "Active1", vorname = "Person", istGesperrt = false)
        val activePerson2 = Person(nachname = "Active2", vorname = "Person", istGesperrt = false)
        val blockedPerson = Person(nachname = "Blocked", vorname = "Person", istGesperrt = true)

        mockPersonRepository.save(activePerson1)
        mockPersonRepository.save(activePerson2)
        mockPersonRepository.save(blockedPerson)

        // When
        val count = mockPersonRepository.countActive()

        // Then
        assertEquals(2L, count)
    }

    @Test
    fun `test person getFullName method`() {
        // Given
        val personWithTitle = Person(
            nachname = "Mustermann",
            vorname = "Max",
            titel = "Dr."
        )
        val personWithoutTitle = Person(
            nachname = "Schmidt",
            vorname = "Anna"
        )

        // When & Then
        assertEquals("Dr. Max Mustermann", personWithTitle.getFullName())
        assertEquals("Anna Schmidt", personWithoutTitle.getFullName())
    }

    @Test
    fun `test person getFormattedAddress method`() {
        // Given
        val personWithCompleteAddress = Person(
            nachname = "Mustermann",
            vorname = "Max",
            strasse = "Musterstraße 123",
            plz = "12345",
            ort = "Musterstadt",
            adresszusatz = "2. Stock"
        )
        val personWithIncompleteAddress = Person(
            nachname = "Schmidt",
            vorname = "Anna",
            strasse = "Teststraße 456"
            // Missing PLZ and Ort
        )

        // When & Then
        assertEquals("Musterstraße 123, 2. Stock, 12345 Musterstadt", personWithCompleteAddress.getFormattedAddress())
        assertNull(personWithIncompleteAddress.getFormattedAddress())
    }
}
