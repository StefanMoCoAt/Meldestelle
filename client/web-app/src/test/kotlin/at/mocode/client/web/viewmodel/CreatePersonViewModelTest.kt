package at.mocode.client.web.viewmodel

import at.mocode.core.domain.model.GeschlechtE
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

/**
 * Comprehensive test suite for the CreatePersonViewModel.
 *
 * Tests cover:
 * - Initial state verification
 * - Field update operations
 * - Form validation
 * - Person creation with various inputs
 * - Form reset functionality
 * - Error handling
 */
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

        // Initialize mock repositories and services
        setupMockRepositories()

        // Create the use case with mocks
        createPersonUseCase = CreatePersonUseCase(
            personRepository = mockPersonRepository,
            vereinRepository = mockVereinRepository,
            masterDataService = mockMasterDataService
        )

        // Initialize the view model
        viewModel = CreatePersonViewModel(createPersonUseCase)
    }

    /**
     * Sets up all mock repositories and services needed for testing
     */
    private fun setupMockRepositories() {
        // Mock person repository with in-memory storage
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

        // Mock verein repository (minimal implementation)
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

        // Mock master data service (minimal implementation)
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
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    //region Initial State Tests

    @Test
    fun `initial state should be correct`() {
        // Verify all fields are initialized to empty values
        assertEquals("", viewModel.nachname, "Nachname should be empty initially")
        assertEquals("", viewModel.vorname, "Vorname should be empty initially")
        assertEquals("", viewModel.titel, "Titel should be empty initially")
        assertEquals("", viewModel.oepsSatzNr, "OepsSatzNr should be empty initially")
        assertEquals("", viewModel.geburtsdatum, "Geburtsdatum should be empty initially")
        assertNull(viewModel.geschlecht, "Geschlecht should be null initially")
        assertEquals("", viewModel.telefon, "Telefon should be empty initially")
        assertEquals("", viewModel.email, "Email should be empty initially")
        assertEquals("", viewModel.strasse, "Strasse should be empty initially")
        assertEquals("", viewModel.plz, "PLZ should be empty initially")
        assertEquals("", viewModel.ort, "Ort should be empty initially")
        assertEquals("", viewModel.adresszusatz, "Adresszusatz should be empty initially")
        assertEquals("", viewModel.feiId, "FeiId should be empty initially")
        assertEquals("", viewModel.mitgliedsNummer, "MitgliedsNummer should be empty initially")
        assertEquals("", viewModel.notizen, "Notizen should be empty initially")

        // Verify flags are initialized correctly
        assertFalse(viewModel.istGesperrt, "IstGesperrt should be false initially")
        assertEquals("", viewModel.sperrGrund, "SperrGrund should be empty initially")
        assertFalse(viewModel.isLoading, "IsLoading should be false initially")
        assertNull(viewModel.errorMessage, "ErrorMessage should be null initially")
        assertFalse(viewModel.isSuccess, "IsSuccess should be false initially")
    }

    //endregion

    //region Update Method Tests

    @Test
    fun `update methods should change state correctly`() {
        // When - update multiple fields
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateTitel("Dr.")
        viewModel.updateGeschlecht(GeschlechtE.M)
        viewModel.updateEmail("max@example.com")
        viewModel.updateIstGesperrt(true)
        viewModel.updateSperrGrund("Test Sperrgrund")

        // Then - verify all fields were updated correctly
        assertEquals("Mustermann", viewModel.nachname, "Nachname should be updated")
        assertEquals("Max", viewModel.vorname, "Vorname should be updated")
        assertEquals("Dr.", viewModel.titel, "Titel should be updated")
        assertEquals(GeschlechtE.M, viewModel.geschlecht, "Geschlecht should be updated")
        assertEquals("max@example.com", viewModel.email, "Email should be updated")
        assertTrue(viewModel.istGesperrt, "IstGesperrt should be updated")
        assertEquals("Test Sperrgrund", viewModel.sperrGrund, "SperrGrund should be updated")
    }

    @Test
    fun `update methods should handle special characters`() {
        // When - update with special characters
        val nameWithSpecialChars = "Müller-Höß"
        viewModel.updateNachname(nameWithSpecialChars)

        // Then - verify special characters are preserved
        assertEquals(nameWithSpecialChars, viewModel.nachname, "Special characters should be preserved")
    }

    @Test
    fun `update methods should handle very long inputs`() {
        // When - update with very long input
        val longText = "A".repeat(500)
        viewModel.updateNotizen(longText)

        // Then - verify long text is preserved
        assertEquals(longText, viewModel.notizen, "Long text should be preserved")
    }

    //endregion

    //region Validation Tests

    @Test
    fun `createPerson should fail with empty nachname`() = runTest {
        // Given - empty nachname
        viewModel.updateVorname("Max")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Nachname ist erforderlich", viewModel.errorMessage, "Should show error for empty nachname")
        assertFalse(viewModel.isSuccess, "Should not be successful with validation error")
        assertFalse(viewModel.isLoading, "Loading state should be reset after validation")
    }

    @Test
    fun `createPerson should fail with empty vorname`() = runTest {
        // Given - empty vorname
        viewModel.updateNachname("Mustermann")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Vorname ist erforderlich", viewModel.errorMessage, "Should show error for empty vorname")
        assertFalse(viewModel.isSuccess, "Should not be successful with validation error")
        assertFalse(viewModel.isLoading, "Loading state should be reset after validation")
    }

    @Test
    fun `createPerson should handle invalid date format`() = runTest {
        // Given - invalid date format
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateGeburtsdatum("invalid-date")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Ungültiges Datumsformat. Verwenden Sie YYYY-MM-DD", viewModel.errorMessage,
            "Should show error for invalid date format")
        assertFalse(viewModel.isSuccess, "Should not be successful with validation error")
        assertFalse(viewModel.isLoading, "Loading state should be reset after validation")
    }

    //endregion

    //region Success Tests

    @Test
    fun `createPerson should succeed with valid data`() = runTest {
        // Given - valid data
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateGeschlecht(GeschlechtE.M)
        viewModel.updateEmail("max@example.com")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isSuccess, "Should be successful with valid data")
        assertNull(viewModel.errorMessage, "Should not have error message")
        assertFalse(viewModel.isLoading, "Loading state should be reset after success")
    }

    @Test
    fun `createPerson should handle valid date format`() = runTest {
        // Given - valid date format
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateGeburtsdatum("1990-05-15")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isSuccess, "Should be successful with valid date")
        assertNull(viewModel.errorMessage, "Should not have error message")
        assertFalse(viewModel.isLoading, "Loading state should be reset after success")
    }

    @Test
    fun `createPerson should succeed with minimal required data`() = runTest {
        // Given - only required fields
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")

        // When
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isSuccess, "Should be successful with minimal required data")
        assertNull(viewModel.errorMessage, "Should not have error message")
        assertFalse(viewModel.isLoading, "Loading state should be reset after success")
    }

    //endregion

    //region Form Management Tests

    @Test
    fun `resetForm should clear all fields`() {
        // Given - set some values
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")
        viewModel.updateEmail("max@example.com")
        viewModel.updateIstGesperrt(true)
        viewModel.updateSperrGrund("Test Sperrgrund")

        // When
        viewModel.resetForm()

        // Then - verify all fields are reset
        assertEquals("", viewModel.nachname, "Nachname should be reset")
        assertEquals("", viewModel.vorname, "Vorname should be reset")
        assertEquals("", viewModel.email, "Email should be reset")
        assertFalse(viewModel.istGesperrt, "IstGesperrt should be reset")
        assertEquals("", viewModel.sperrGrund, "SperrGrund should be reset")

        // Verify state flags are reset
        assertFalse(viewModel.isLoading, "IsLoading should be reset")
        assertNull(viewModel.errorMessage, "ErrorMessage should be reset")
        assertFalse(viewModel.isSuccess, "IsSuccess should be reset")
    }

    @Test
    fun `clearError should reset error message`() = runTest {
        // Given - simulate an error
        viewModel.updateNachname("") // This will cause validation error
        viewModel.updateVorname("Max")
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify error message exists
        assertNotNull(viewModel.errorMessage, "Should have error message")

        // When - clear the error
        viewModel.clearError()

        // Then - verify error message is cleared
        assertNull(viewModel.errorMessage, "Error message should be cleared")
    }

    @Test
    fun `loading state should be reset after createPerson completes`() = runTest {
        // Given
        viewModel.updateNachname("Mustermann")
        viewModel.updateVorname("Max")

        // When - start creation and complete the operation
        viewModel.createPerson()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify loading state is reset after completion
        assertFalse(viewModel.isLoading, "Loading state should be reset after operation completes")
        assertTrue(viewModel.isSuccess, "Operation should complete successfully")
    }

    //endregion
}
