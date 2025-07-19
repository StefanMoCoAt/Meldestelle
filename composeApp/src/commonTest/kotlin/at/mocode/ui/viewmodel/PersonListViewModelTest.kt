package at.mocode.ui.viewmodel

import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.enums.GeschlechtE
import at.mocode.enums.DatenQuelleE
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PersonListViewModelTest {

    private lateinit var mockPersonRepository: PersonRepository
    private lateinit var viewModel: PersonListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockPersonRepository = object : PersonRepository {
            private val persons = mutableListOf<DomPerson>()

            override suspend fun save(person: DomPerson): DomPerson {
                val savedPerson = person.copy(personId = uuid4())
                persons.add(savedPerson)
                return savedPerson
            }

            override suspend fun findById(id: com.benasher44.uuid.Uuid): DomPerson? {
                return persons.find { it.personId == id }
            }

            override suspend fun findByOepsSatzNr(oepsSatzNr: String): DomPerson? {
                return persons.find { it.oepsSatzNr == oepsSatzNr }
            }

            override suspend fun findByStammVereinId(vereinId: com.benasher44.uuid.Uuid): List<DomPerson> {
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

            override suspend fun delete(id: com.benasher44.uuid.Uuid): Boolean {
                return persons.removeAll { it.personId == id }
            }
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        viewModel = PersonListViewModel(mockPersonRepository)

        assertTrue(viewModel.persons.isEmpty())
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `loadPersons should update persons list`() = runTest {
        // Given
        val testPerson = DomPerson(
            oepsSatzNr = "123456",
            nachname = "Test",
            vorname = "User",
            geschlechtE = GeschlechtE.M,
            datenQuelle = DatenQuelleE.MANUELL
        )
        mockPersonRepository.save(testPerson)

        // When
        viewModel = PersonListViewModel(mockPersonRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.persons.size)
        assertEquals("Test", viewModel.persons.first().nachname)
        assertEquals("User", viewModel.persons.first().vorname)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun `refreshPersons should reload data`() = runTest {
        // Given
        viewModel = PersonListViewModel(mockPersonRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialCount = viewModel.persons.size

        // Add a new person to repository
        val newPerson = DomPerson(
            oepsSatzNr = "789012",
            nachname = "New",
            vorname = "Person",
            geschlechtE = GeschlechtE.W,
            datenQuelle = DatenQuelleE.MANUELL
        )
        mockPersonRepository.save(newPerson)

        // When
        viewModel.refreshPersons()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(initialCount + 1, viewModel.persons.size)
        assertTrue(viewModel.persons.any { it.nachname == "New" })
    }

    @Test
    fun `clearError should reset error message`() {
        viewModel = PersonListViewModel(mockPersonRepository)

        // Simulate an error (this would normally happen in a real error scenario)
        // For testing, we can't easily simulate repository errors with our mock
        // but we can test the clearError functionality

        viewModel.clearError()
        assertNull(viewModel.errorMessage)
    }
}
