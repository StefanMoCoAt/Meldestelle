package at.mocode

import at.mocode.model.Turnier
import at.mocode.repositories.TurnierRepository
import at.mocode.services.TurnierService
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.*

/**
 * Comprehensive test suite for TurnierService business logic.
 *
 * This test class verifies:
 * - CRUD operations (create, read, update, delete)
 * - Search functionality
 * - Business validation rules
 * - Error handling and edge cases
 * - Transaction management behavior
 * - Duplicate checking logic
 */
class TurnierServiceTest {

    private lateinit var mockRepository: MockTurnierRepository
    private lateinit var turnierService: TurnierService

    @BeforeTest
    fun setup() {
        mockRepository = MockTurnierRepository()
        turnierService = TurnierService(mockRepository)
    }

    @Test
    fun testGetAllTurniere() = runBlocking {
        // Given
        val turnier1 = createTestTurnier("Tournament 1", "T001")
        val turnier2 = createTestTurnier("Tournament 2", "T002")
        mockRepository.turniere.addAll(listOf(turnier1, turnier2))

        // When
        val result = turnierService.getAllTurniere()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(turnier1))
        assertTrue(result.contains(turnier2))
    }

    @Test
    fun testGetTurnierById() = runBlocking {
        // Given
        val turnier = createTestTurnier("Test Tournament", "T001")
        mockRepository.turniere.add(turnier)

        // When
        val result = turnierService.getTurnierById(turnier.id)

        // Then
        assertNotNull(result)
        assertEquals(turnier.id, result.id)
        assertEquals("Test Tournament", result.titel)
    }

    @Test
    fun testGetTurnierByIdNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()

        // When
        val result = turnierService.getTurnierById(nonExistentId)

        // Then
        assertNull(result)
    }

    @Test
    fun testGetTurniereByVeranstaltungId() = runBlocking {
        // Given
        val veranstaltungId = uuid4()
        val turnier1 = createTestTurnier("Tournament 1", "T001", veranstaltungId)
        val turnier2 = createTestTurnier("Tournament 2", "T002", veranstaltungId)
        val turnier3 = createTestTurnier("Tournament 3", "T003", uuid4()) // Different event
        mockRepository.turniere.addAll(listOf(turnier1, turnier2, turnier3))

        // When
        val result = turnierService.getTurniereByVeranstaltungId(veranstaltungId)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.veranstaltungId == veranstaltungId })
    }

    @Test
    fun testGetTurnierByOepsTurnierNr() = runBlocking {
        // Given
        val turnier = createTestTurnier("Test Tournament", "T001")
        mockRepository.turniere.add(turnier)

        // When
        val result = turnierService.getTurnierByOepsTurnierNr("T001")

        // Then
        assertNotNull(result)
        assertEquals("T001", result.oepsTurnierNr)
        assertEquals("Test Tournament", result.titel)
    }

    @Test
    fun testGetTurnierByOepsTurnierNrNotFound() = runBlocking {
        // When
        val result = turnierService.getTurnierByOepsTurnierNr("NONEXISTENT")

        // Then
        assertNull(result)
    }

    @Test
    fun testGetTurnierByOepsTurnierNrBlank() {
        runBlocking {
            // When & Then
            assertFailsWith<IllegalArgumentException> {
                turnierService.getTurnierByOepsTurnierNr("")
            }

            assertFailsWith<IllegalArgumentException> {
                turnierService.getTurnierByOepsTurnierNr("   ")
            }
        }
    }

    @Test
    fun testSearchTurniere() = runBlocking {
        // Given
        val turnier1 = createTestTurnier("Spring Tournament", "T001")
        val turnier2 = createTestTurnier("Summer Championship", "T002")
        val turnier3 = createTestTurnier("Winter Cup", "T003")
        mockRepository.turniere.addAll(listOf(turnier1, turnier2, turnier3))

        // When
        val result = turnierService.searchTurniere("Tournament")

        // Then
        assertEquals(1, result.size)
        assertEquals("Spring Tournament", result[0].titel)
    }

    @Test
    fun testSearchTurniereBlankQuery() {
        runBlocking {
            // When & Then
            assertFailsWith<IllegalArgumentException> {
                turnierService.searchTurniere("")
            }

            assertFailsWith<IllegalArgumentException> {
                turnierService.searchTurniere("   ")
            }
        }
    }

    @Test
    fun testCreateTurnier() = runBlocking {
        // Given
        val turnier = createTestTurnier("New Tournament", "T001")

        // When
        val result = turnierService.createTurnier(turnier)

        // Then
        assertNotNull(result)
        assertEquals("New Tournament", result.titel)
        assertEquals("T001", result.oepsTurnierNr)
        assertTrue(mockRepository.turniere.contains(result))
    }

    @Test
    fun testCreateTurnierValidation() {
        runBlocking {
            // Test blank title
            assertFailsWith<IllegalArgumentException> {
                turnierService.createTurnier(createTestTurnier("", "T001"))
            }

            // Test title too long
            val longTitle = "a".repeat(256)
            assertFailsWith<IllegalArgumentException> {
                turnierService.createTurnier(createTestTurnier(longTitle, "T001"))
            }

            // Test invalid date range
            assertFailsWith<IllegalArgumentException> {
                turnierService.createTurnier(
                    createTestTurnier(
                        "Test Tournament",
                        "T001",
                        datumVon = LocalDate(2024, 12, 31),
                        datumBis = LocalDate(2024, 1, 1)
                    )
                )
            }

            // Test blank OEPS number
            assertFailsWith<IllegalArgumentException> {
                turnierService.createTurnier(createTestTurnier("Test Tournament", ""))
            }
        }
    }

    @Test
    fun testCreateTurnierDuplicateOepsNr() {
        runBlocking {
            // Given
            val existingTurnier = createTestTurnier("Existing Tournament", "T001")
            mockRepository.turniere.add(existingTurnier)

            // When & Then
            assertFailsWith<IllegalArgumentException> {
                turnierService.createTurnier(createTestTurnier("New Tournament", "T001"))
            }
        }
    }

    @Test
    fun testUpdateTurnier() = runBlocking {
        // Given
        val originalTurnier = createTestTurnier("Original Tournament", "T001")
        mockRepository.turniere.add(originalTurnier)
        val updatedTurnier = originalTurnier.copy(titel = "Updated Tournament")

        // When
        val result = turnierService.updateTurnier(originalTurnier.id, updatedTurnier)

        // Then
        assertNotNull(result)
        assertEquals("Updated Tournament", result.titel)
        assertEquals("T001", result.oepsTurnierNr)
    }

    @Test
    fun testUpdateTurnierNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()
        val turnier = createTestTurnier("Test Tournament", "T001")

        // When
        val result = turnierService.updateTurnier(nonExistentId, turnier)

        // Then
        assertNull(result)
    }

    @Test
    fun testUpdateTurnierValidation() {
        runBlocking {
            // Given
            val originalTurnier = createTestTurnier("Original Tournament", "T001")
            mockRepository.turniere.add(originalTurnier)

            // Test blank title
            assertFailsWith<IllegalArgumentException> {
                turnierService.updateTurnier(originalTurnier.id, originalTurnier.copy(titel = ""))
            }

            // Test title too long
            val longTitle = "a".repeat(256)
            assertFailsWith<IllegalArgumentException> {
                turnierService.updateTurnier(originalTurnier.id, originalTurnier.copy(titel = longTitle))
            }
        }
    }

    @Test
    fun testUpdateTurnierDuplicateOepsNr() {
        runBlocking {
            // Given
            val turnier1 = createTestTurnier("Tournament 1", "T001")
            val turnier2 = createTestTurnier("Tournament 2", "T002")
            mockRepository.turniere.addAll(listOf(turnier1, turnier2))

            // When & Then - Try to update turnier2 with turnier1's OEPS number
            assertFailsWith<IllegalArgumentException> {
                turnierService.updateTurnier(turnier2.id, turnier2.copy(oepsTurnierNr = "T001"))
            }
        }
    }

    @Test
    fun testUpdateTurnierSameOepsNr() = runBlocking {
        // Given - Should allow updating with the same OEPS number
        val turnier = createTestTurnier("Tournament", "T001")
        mockRepository.turniere.add(turnier)

        // When
        val result = turnierService.updateTurnier(turnier.id, turnier.copy(titel = "Updated Tournament"))

        // Then
        assertNotNull(result)
        assertEquals("Updated Tournament", result.titel)
        assertEquals("T001", result.oepsTurnierNr)
    }

    @Test
    fun testDeleteTurnier() = runBlocking {
        // Given
        val turnier = createTestTurnier("Test Tournament", "T001")
        mockRepository.turniere.add(turnier)

        // When
        val result = turnierService.deleteTurnier(turnier.id)

        // Then
        assertTrue(result)
        assertFalse(mockRepository.turniere.contains(turnier))
    }

    @Test
    fun testDeleteTurnierNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()

        // When
        val result = turnierService.deleteTurnier(nonExistentId)

        // Then
        assertFalse(result)
    }

    @Test
    fun testGetTurniereForEvent() = runBlocking {
        // Given
        val veranstaltungId = uuid4()
        val turnier1 = createTestTurnier("Tournament 1", "T001", veranstaltungId)
        val turnier2 = createTestTurnier("Tournament 2", "T002", veranstaltungId)
        mockRepository.turniere.addAll(listOf(turnier1, turnier2))

        // When
        val result = turnierService.getTurniereForEvent(veranstaltungId)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.veranstaltungId == veranstaltungId })
    }

    private fun createTestTurnier(
        titel: String,
        oepsTurnierNr: String,
        veranstaltungId: com.benasher44.uuid.Uuid = uuid4(),
        datumVon: LocalDate = LocalDate(2024, 6, 1),
        datumBis: LocalDate = LocalDate(2024, 6, 3)
    ): Turnier {
        return Turnier(
            id = uuid4(),
            veranstaltungId = veranstaltungId,
            oepsTurnierNr = oepsTurnierNr,
            titel = titel,
            untertitel = null,
            datumVon = datumVon,
            datumBis = datumBis,
            nennungsschluss = null,
            nennungsArt = emptyList(),
            nennungsHinweis = null,
            eigenesNennsystemUrl = null,
            nenngeld = null,
            startgeldStandard = null,
            austragungsplaetze = emptyList(),
            vorbereitungsplaetze = emptyList(),
            turnierleiterId = null,
            turnierbeauftragterId = null,
            richterIds = emptyList(),
            parcoursbauerIds = emptyList(),
            parcoursAssistentIds = emptyList(),
            tierarztInfos = null,
            hufschmiedInfo = null,
            meldestelleVerantwortlicherId = null,
            meldestelleTelefon = null,
            meldestelleOeffnungszeiten = null,
            ergebnislistenUrl = null,
            verfuegbareArtikel = emptyList(),
            meisterschaftRefs = emptyList(),
            createdAt = kotlinx.datetime.Clock.System.now(),
            updatedAt = kotlinx.datetime.Clock.System.now()
        )
    }

    /**
     * Mock implementation of TurnierRepository for testing
     */
    class MockTurnierRepository : TurnierRepository {
        val turniere = mutableListOf<Turnier>()

        override suspend fun findAll(): List<Turnier> = turniere.toList()

        override suspend fun findById(id: com.benasher44.uuid.Uuid): Turnier? =
            turniere.find { it.id == id }

        override suspend fun findByVeranstaltungId(veranstaltungId: com.benasher44.uuid.Uuid): List<Turnier> =
            turniere.filter { it.veranstaltungId == veranstaltungId }

        override suspend fun findByOepsTurnierNr(oepsTurnierNr: String): Turnier? =
            turniere.find { it.oepsTurnierNr == oepsTurnierNr }

        override suspend fun search(query: String): List<Turnier> =
            turniere.filter { it.titel.contains(query, ignoreCase = true) }

        override suspend fun create(turnier: Turnier): Turnier {
            turniere.add(turnier)
            return turnier
        }

        override suspend fun update(id: com.benasher44.uuid.Uuid, turnier: Turnier): Turnier? {
            val index = turniere.indexOfFirst { it.id == id }
            return if (index >= 0) {
                val updated = turnier.copy(id = id)
                turniere[index] = updated
                updated
            } else {
                null
            }
        }

        override suspend fun delete(id: com.benasher44.uuid.Uuid): Boolean {
            return turniere.removeIf { it.id == id }
        }
    }
}
