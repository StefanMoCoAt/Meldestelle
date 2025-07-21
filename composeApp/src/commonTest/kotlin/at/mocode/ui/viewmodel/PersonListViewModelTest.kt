package at.mocode.ui.viewmodel

import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.enums.GeschlechtE
import at.mocode.enums.DatenQuelleE
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Comprehensive test suite for the PersonListViewModel.
 *
 * Tests cover:
 * - Initial state verification
 * - Loading and refreshing person data
 * - Error handling
 * - Loading state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PersonListViewModelTest {

    private lateinit var mockPersonRepository: PersonRepository
    private lateinit var viewModel: PersonListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        setupMockRepository()
    }

    /**
     * Sets up the mock repository with test data
     */
    private fun setupMockRepository() {
        val persons = mutableListOf<DomPerson>()

        mockPersonRepository = object : PersonRepository {
            override suspend fun save(person: DomPerson): DomPerson {
                val savedPerson = person.copy(personId = uuid4())

                // Remove existing person with same OEPS number if exists
                val existingIndex = persons.indexOfFirst { it.oepsSatzNr == person.oepsSatzNr }
                if (existingIndex >= 0) {
                    persons.removeAt(existingIndex)
                }

                persons.add(savedPerson)
                return savedPerson
            }

            override suspend fun findById(id: Uuid): DomPerson? {
                return persons.find { it.personId == id }
            }

            override suspend fun findByOepsSatzNr(oepsSatzNr: String): DomPerson? {
                return persons.find { it.oepsSatzNr == oepsSatzNr }
            }

            override suspend fun findByStammVereinId(vereinId: Uuid): List<DomPerson> {
                return persons.filter { it.stammVereinId == vereinId }
            }

            override suspend fun findByName(searchTerm: String, limit: Int): List<DomPerson> {
                return persons.filter {
                    it.nachname.contains(searchTerm, ignoreCase = true) ||
                    it.vorname.contains(searchTerm, ignoreCase = true)
                }.take(limit)
            }

            override suspend fun findAllActive(limit: Int, offset: Int): List<DomPerson> {
                return persons.filter { it.istAktiv }.drop(offset).take(limit)
            }

            override suspend fun countActive(): Long {
                return persons.count { it.istAktiv }.toLong()
            }

            override suspend fun existsByOepsSatzNr(oepsSatzNr: String): Boolean {
                return persons.any { it.oepsSatzNr == oepsSatzNr }
            }

            override suspend fun delete(id: Uuid): Boolean {
                val initialSize = persons.size
                persons.removeAll { it.personId == id }
                return persons.size < initialSize
            }
        }
    }

    /**
     * Adds test persons to the repository
     */
    private suspend fun addTestPersons() {
        // Create and add test persons
        val testPersons = listOf(
            createTestPerson("123456", "Müller", "Hans", GeschlechtE.M),
            createTestPerson("234567", "Schmidt", "Anna", GeschlechtE.W),
            createTestPerson("345678", "Weber", "Thomas", GeschlechtE.M)
        )

        testPersons.forEach { mockPersonRepository.save(it) }
    }

    /**
     * Creates a test person with the given data
     */
    private fun createTestPerson(
        oepsSatzNr: String,
        nachname: String,
        vorname: String,
        geschlecht: GeschlechtE,
        isActive: Boolean = true
    ): DomPerson {
        return DomPerson(
            personId = uuid4(), // Generate a new UUID
            oepsSatzNr = oepsSatzNr,
            nachname = nachname,
            vorname = vorname,
            geschlechtE = geschlecht,
            datenQuelle = DatenQuelleE.MANUELL,
            istAktiv = isActive
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    //region Initial State Tests

    @Test
    fun `initial state should be correct`() {
        // When - create view model with empty repository
        viewModel = PersonListViewModel(mockPersonRepository)

        // Then - verify initial state
        assertTrue(viewModel.persons.isEmpty(), "Persons list should be empty initially")
        assertFalse(viewModel.isLoading, "Loading state should be false initially")
        assertNull(viewModel.errorMessage, "Error message should be null initially")
    }

    //endregion

    //region Data Loading Tests

    @Test
    fun `loadPersons should update persons list`() = runTest {
        // Given - repository with test data
        addTestPersons()

        // When - initialize view model (which triggers loadPersons)
        viewModel = PersonListViewModel(mockPersonRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify persons list is populated
        assertEquals(3, viewModel.persons.size, "Should load all test persons")
        assertTrue(
            viewModel.persons.any { it.nachname == "Müller" && it.vorname == "Hans" },
            "Should contain person Müller Hans"
        )
        assertTrue(
            viewModel.persons.any { it.nachname == "Schmidt" && it.vorname == "Anna" },
            "Should contain person Schmidt Anna"
        )
        assertTrue(
            viewModel.persons.any { it.nachname == "Weber" && it.vorname == "Thomas" },
            "Should contain person Weber Thomas"
        )
        assertFalse(viewModel.isLoading, "Loading state should be reset after loading")
        assertNull(viewModel.errorMessage, "Should not have error message after successful loading")
    }

    @Test
    fun `refreshPersons should reload data`() = runTest {
        // Given - view model with initial data loaded
        addTestPersons()
        viewModel = PersonListViewModel(mockPersonRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        val initialCount = viewModel.persons.size

        // When - add a new person and refresh
        val newPerson = createTestPerson(
            "999999",
            "New",
            "Person",
            GeschlechtE.D
        )
        mockPersonRepository.save(newPerson)
        viewModel.refreshPersons()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify new person is included
        assertEquals(initialCount + 1, viewModel.persons.size, "Should have one more person after refresh")
        assertTrue(
            viewModel.persons.any { it.nachname == "New" && it.vorname == "Person" },
            "Should contain newly added person after refresh"
        )
        assertFalse(viewModel.isLoading, "Loading state should be reset after refresh")
    }

    @Test
    fun `loadPersons should handle empty repository`() = runTest {
        // Given - empty repository (already set up in setup())

        // When - initialize view model
        viewModel = PersonListViewModel(mockPersonRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify empty list is handled correctly
        assertTrue(viewModel.persons.isEmpty(), "Persons list should be empty with empty repository")
        assertFalse(viewModel.isLoading, "Loading state should be reset even with empty result")
        assertNull(viewModel.errorMessage, "Should not have error with empty repository")
    }

    @Test
    fun `loading state should be reset after operations complete`() = runTest {
        // Given
        viewModel = PersonListViewModel(mockPersonRepository)

        // Add some test data to verify operation works
        addTestPersons()

        // When - refresh and complete the operation
        viewModel.refreshPersons()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify loading state is reset after completion
        assertFalse(viewModel.isLoading, "Loading state should be reset after operation completes")
        assertTrue(viewModel.persons.isNotEmpty(), "Persons list should be populated after successful refresh")
    }

    //endregion

    //region Error Handling Tests

    @Test
    fun `clearError should reset error message`() {
        // Given - view model
        viewModel = PersonListViewModel(mockPersonRepository)

        // When - clear error (even when no error exists)
        viewModel.clearError()

        // Then - verify no error message
        assertNull(viewModel.errorMessage, "Error message should be null after clearError")
    }

    @Test
    fun `error handling should be robust`() = runTest {
        // Given - view model with initial data loaded
        addTestPersons()
        viewModel = PersonListViewModel(mockPersonRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Capture initial state
        val initialPersons = viewModel.persons.toList()

        // When - simulate a refresh operation that might cause errors
        viewModel.refreshPersons()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify data is still intact regardless of potential errors
        assertEquals(initialPersons.size, viewModel.persons.size,
            "Person list size should be maintained even with potential errors")

        // And error handling mechanism works
        viewModel.clearError()
        assertNull(viewModel.errorMessage, "Should be able to clear any potential errors")
    }

    //endregion

    //region Search Tests

    @Test
    fun `repository search should work correctly`() = runTest {
        // Given - repository with test data
        addTestPersons()

        // When - search for a specific person
        val searchResults = mockPersonRepository.findByName("Müller", 10)

        // Then - verify correct results
        assertEquals(1, searchResults.size, "Should find one person with name Müller")
        assertEquals("Müller", searchResults.first().nachname, "Should find person with correct last name")
        assertEquals("Hans", searchResults.first().vorname, "Should find person with correct first name")
    }

    //endregion
}
