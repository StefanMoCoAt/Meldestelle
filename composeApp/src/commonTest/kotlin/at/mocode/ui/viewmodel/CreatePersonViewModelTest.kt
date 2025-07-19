package at.mocode.ui.viewmodel

import at.mocode.enums.GeschlechtE
import at.mocode.members.application.usecase.CreatePersonUseCase
import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.members.domain.repository.VereinRepository
import at.mocode.members.domain.service.MasterDataService
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePersonViewModelTest {

    private lateinit var mockPersonRepository: PersonRepository
    private lateinit var mockVereinRepository: VereinRepository
    private lateinit var mockMasterDataService: MasterDataService
    private lateinit var createPersonUseCase: CreatePersonUseCase
    private lateinit var viewModel: CreatePersonViewModel
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
                    it.vorname.contains(searchTerm, ignoreCase = true) ||
                    it.nachname.contains(searchTerm, ignoreCase = true)
                }.take(limit)
            }

            override suspend fun findAllActive(limit: Int, offset: Int): List<DomPerson> {
                return persons.filter { !it.istGesperrt }.drop(offset).take(limit)
            }

            override suspend fun existsByOepsSatzNr(oepsSatzNr: String): Boolean {
                return persons.any { it.oepsSatzNr == oepsSatzNr }
            }

            override suspend fun countActive(): Long {
                return persons.count { !it.istGesperrt }.toLong()
            }

            override suspend fun delete(id: com.benasher44.uuid.Uuid): Boolean {
                return persons.removeAll { it.personId == id }
            }
        }

        mockVereinRepository = object : VereinRepository {
            override suspend fun findById(id: com.benasher44.uuid.Uuid): at.mocode.members.domain.model.DomVerein? {
                return null
            }

            override suspend fun findByOepsVereinsNr(oepsVereinsNr: String): at.mocode.members.domain.model.DomVerein? {
                return null
            }

            override suspend fun findByName(searchTerm: String, limit: Int): List<at.mocode.members.domain.model.DomVerein> {
                return emptyList()
            }

            override suspend fun findByBundeslandId(bundeslandId: com.benasher44.uuid.Uuid): List<at.mocode.members.domain.model.DomVerein> {
                return emptyList()
            }

            override suspend fun findByLandId(landId: com.benasher44.uuid.Uuid): List<at.mocode.members.domain.model.DomVerein> {
                return emptyList()
            }

            override suspend fun findAllActive(limit: Int, offset: Int): List<at.mocode.members.domain.model.DomVerein> {
                return emptyList()
            }

            override suspend fun findByLocation(searchTerm: String, limit: Int): List<at.mocode.members.domain.model.DomVerein> {
                return emptyList()
            }

            override suspend fun save(verein: at.mocode.members.domain.model.DomVerein): at.mocode.members.domain.model.DomVerein {
                return verein
            }

            override suspend fun delete(id: com.benasher44.uuid.Uuid): Boolean {
                return true
            }

            override suspend fun existsByOepsVereinsNr(oepsVereinsNr: String): Boolean {
                return false
            }

            override suspend fun countActive(): Long {
                return 0L
            }

            override suspend fun countActiveByBundeslandId(bundeslandId: com.benasher44.uuid.Uuid): Long {
                return 0L
            }
        }

        mockMasterDataService = object : MasterDataService {
            override suspend fun countryExists(countryId: com.benasher44.uuid.Uuid): Boolean {
                return true
            }

            override suspend fun stateExists(stateId: com.benasher44.uuid.Uuid): Boolean {
                return true
            }

            override suspend fun getCountryById(countryId: com.benasher44.uuid.Uuid): MasterDataService.CountryInfo? {
                return null
            }

            override suspend fun getStateById(stateId: com.benasher44.uuid.Uuid): MasterDataService.StateInfo? {
                return null
            }

            override suspend fun getAllCountries(): List<MasterDataService.CountryInfo> {
                return emptyList()
            }

            override suspend fun getStatesByCountry(countryId: com.benasher44.uuid.Uuid): List<MasterDataService.StateInfo> {
                return emptyList()
            }
        }

        createPersonUseCase = CreatePersonUseCase(
            personRepository = mockPersonRepository,
            vereinRepository = mockVereinRepository,
            masterDataService = mockMasterDataService
        )

        viewModel = CreatePersonViewModel(createPersonUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        assertEquals("", viewModel.nachname)
        assertEquals("", viewModel.vorname)
        assertEquals("", viewModel.titel)
        assertEquals("", viewModel.oepsSatzNr)
        assertEquals("", viewModel.geburtsdatum)
        assertNull(viewModel.geschlecht)
        assertEquals("", viewModel.telefon)
        assertEquals("", viewModel.email)
        assertEquals("", viewModel.strasse)
        assertEquals("", viewModel.plz)
        assertEquals("", viewModel.ort)
        assertEquals("", viewModel.adresszusatz)
        assertEquals("", viewModel.feiId)
        assertEquals("", viewModel.mitgliedsNummer)
        assertEquals("", viewModel.notizen)
        assertFalse(viewModel.istGesperrt)
        assertEquals("", viewModel.sperrGrund)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun `update methods should change state correctly`() {
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateTitel("Dr.")
        viewModel.updateGeschlecht(GeschlechtE.M)
        viewModel.updateEmail("max@example.com")
        viewModel.updateIstGesperrt(true)

        assertEquals("Mustermann", viewModel.nachname)
        assertEquals("Max", viewModel.vorname)
        assertEquals("Dr.", viewModel.titel)
        assertEquals(GeschlechtE.M, viewModel.geschlecht)
        assertEquals("max@example.com", viewModel.email)
        assertTrue(viewModel.istGesperrt)
    }

    @Test
    fun `createPerson should fail with empty nachname`() = runTest {
        // Given - empty nachname
        viewModel.updateVorname("Max")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Nachname ist erforderlich", viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `createPerson should fail with empty vorname`() = runTest {
        // Given - empty vorname
        viewModel.updateNachname("Mustermann")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Vorname ist erforderlich", viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `createPerson should succeed with valid data`() = runTest {
        // Given
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateGeschlecht(GeschlechtE.M)
        viewModel.updateEmail("max@example.com")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isSuccess)
        assertNull(viewModel.errorMessage)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `createPerson should handle invalid date format`() = runTest {
        // Given
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateGeburtsdatum("invalid-date")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Ungültiges Datumsformat. Verwenden Sie YYYY-MM-DD", viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `createPerson should handle valid date format`() = runTest {
        // Given
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateGeburtsdatum("1990-05-15")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isSuccess)
        assertNull(viewModel.errorMessage)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `resetForm should clear all fields`() {
        // Given - set some values
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateEmail("max@example.com")
        viewModel.updateIstGesperrt(true)

        // When
        viewModel.resetForm()

        // Then
        assertEquals("", viewModel.nachname)
        assertEquals("", viewModel.vorname)
        assertEquals("", viewModel.email)
        assertFalse(viewModel.istGesperrt)
        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun `clearError should reset error message`() {
        // Given - simulate an error
        viewModel.updateNachname("") // This will cause validation error
        viewModel.updateVorname("Max")

        runTest {
            viewModel.createPerson()
            testDispatcher.scheduler.advanceUntilIdle()
        }

        assertNotNull(viewModel.errorMessage)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.errorMessage)
    }
}
