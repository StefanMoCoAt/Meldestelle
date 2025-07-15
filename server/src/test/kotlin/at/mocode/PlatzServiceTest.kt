package at.mocode

import at.mocode.model.Platz
import at.mocode.repositories.PlatzRepository
import at.mocode.services.PlatzService
import at.mocode.enums.PlatzTypE
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class PlatzServiceTest {

    private lateinit var mockRepository: MockPlatzRepository
    private lateinit var platzService: PlatzService

    @BeforeTest
    fun setup() {
        mockRepository = MockPlatzRepository()
        platzService = PlatzService(mockRepository)
    }

    @Test
    fun testGetAllPlaetze() = runBlocking {
        // Given
        val platz1 = createTestPlatz("Platz 1")
        val platz2 = createTestPlatz("Platz 2")
        mockRepository.plaetze = mutableListOf(platz1, platz2)

        // When
        val result = platzService.getAllPlaetze()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(platz1))
        assertTrue(result.contains(platz2))
    }

    @Test
    fun testGetPlatzById() = runBlocking {
        // Given
        val platz = createTestPlatz("Test Platz")
        mockRepository.plaetze = mutableListOf(platz)

        // When
        val result = platzService.getPlatzById(platz.id)

        // Then
        assertNotNull(result)
        assertEquals(platz.id, result.id)
        assertEquals(platz.name, result.name)
    }

    @Test
    fun testGetPlatzByIdNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()

        // When
        val result = platzService.getPlatzById(nonExistentId)

        // Then
        assertNull(result)
    }

    @Test
    fun testGetPlaetzeByTurnierId() = runBlocking {
        // Given
        val turnierId1 = uuid4()
        val turnierId2 = uuid4()
        val platz1 = createTestPlatz("Platz 1", turnierId = turnierId1)
        val platz2 = createTestPlatz("Platz 2", turnierId = turnierId1)
        val platz3 = createTestPlatz("Platz 3", turnierId = turnierId2)
        mockRepository.plaetze = mutableListOf(platz1, platz2, platz3)

        // When
        val result = platzService.getPlaetzeByTurnierId(turnierId1)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.turnierId == turnierId1 })
        assertTrue(result.contains(platz1))
        assertTrue(result.contains(platz2))
        assertFalse(result.contains(platz3))
    }

    @Test
    fun testGetPlaetzeByTyp() = runBlocking {
        // Given
        val platz1 = createTestPlatz("Austragung Platz", typ = PlatzTypE.AUSTRAGUNG)
        val platz2 = createTestPlatz("Vorbereitung Platz", typ = PlatzTypE.VORBEREITUNG)
        val platz3 = createTestPlatz("Another Austragung", typ = PlatzTypE.AUSTRAGUNG)
        mockRepository.plaetze = mutableListOf(platz1, platz2, platz3)

        // When
        val austragungResult = platzService.getPlaetzeByTyp(PlatzTypE.AUSTRAGUNG)
        val vorbereitungResult = platzService.getPlaetzeByTyp(PlatzTypE.VORBEREITUNG)

        // Then
        assertEquals(2, austragungResult.size)
        assertTrue(austragungResult.all { it.typ == PlatzTypE.AUSTRAGUNG })

        assertEquals(1, vorbereitungResult.size)
        assertEquals(platz2.id, vorbereitungResult[0].id)
    }

    @Test
    fun testSearchPlaetze() = runBlocking {
        // Given
        val platz1 = createTestPlatz("Hauptplatz")
        val platz2 = createTestPlatz("Nebenplatz")
        val platz3 = createTestPlatz("Trainingsplatz")
        mockRepository.plaetze = mutableListOf(platz1, platz2, platz3)

        // When
        val result = platzService.searchPlaetze("Haupt")

        // Then
        assertEquals(1, result.size)
        assertEquals(platz1.id, result[0].id)
    }

    @Test
    fun testSearchPlaetzeBlankQuery() {
        // When & Then
        assertFailsWith<IllegalArgumentException> {
            runBlocking { platzService.searchPlaetze("") }
        }

        assertFailsWith<IllegalArgumentException> {
            runBlocking { platzService.searchPlaetze("   ") }
        }
    }

    @Test
    fun testCreatePlatz() = runBlocking {
        // Given
        val platz = createTestPlatz("New Platz")

        // When
        val result = platzService.createPlatz(platz)

        // Then
        assertEquals(platz.name, result.name)
        assertEquals(platz.turnierId, result.turnierId)
        assertEquals(platz.typ, result.typ)
        assertTrue(mockRepository.plaetze.contains(result))
    }

    @Test
    fun testCreatePlatzValidation() {
        // Test blank name
        assertFailsWith<IllegalArgumentException> {
            runBlocking { platzService.createPlatz(createTestPlatz("")) }
        }

        // Test long name
        assertFailsWith<IllegalArgumentException> {
            runBlocking { platzService.createPlatz(createTestPlatz("a".repeat(101))) }
        }

        // Test long dimension
        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                platzService.createPlatz(createTestPlatz("Test", dimension = "a".repeat(51)))
            }
        }

        // Test long boden
        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                platzService.createPlatz(createTestPlatz("Test", boden = "a".repeat(101)))
            }
        }
    }

    @Test
    fun testUpdatePlatz() = runBlocking {
        // Given
        val originalPlatz = createTestPlatz("Original")
        mockRepository.plaetze = mutableListOf(originalPlatz)

        val updatedPlatz = originalPlatz.copy(name = "Updated")

        // When
        val result = platzService.updatePlatz(originalPlatz.id, updatedPlatz)

        // Then
        assertNotNull(result)
        assertEquals("Updated", result.name)
    }

    @Test
    fun testUpdatePlatzNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()
        val platz = createTestPlatz("Test")

        // When
        val result = platzService.updatePlatz(nonExistentId, platz)

        // Then
        assertNull(result)
    }

    @Test
    fun testUpdatePlatzValidation() {
        // Given
        val originalPlatz = createTestPlatz("Original")
        mockRepository.plaetze = mutableListOf(originalPlatz)

        // Test validation during update
        assertFailsWith<IllegalArgumentException> {
            runBlocking { platzService.updatePlatz(originalPlatz.id, createTestPlatz("")) }
        }
    }

    @Test
    fun testDeletePlatz() = runBlocking {
        // Given
        val platz = createTestPlatz("To Delete")
        mockRepository.plaetze = mutableListOf(platz)

        // When
        val result = platzService.deletePlatz(platz.id)

        // Then
        assertTrue(result)
        assertFalse(mockRepository.plaetze.contains(platz))
    }

    @Test
    fun testDeletePlatzNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()

        // When
        val result = platzService.deletePlatz(nonExistentId)

        // Then
        assertFalse(result)
    }

    @Test
    fun testValidationWithOptionalFields() = runBlocking {
        // Test valid platz with all optional fields
        val platzWithOptionals = createTestPlatz(
            name = "Test Platz",
            dimension = "20x40m",
            boden = "Sand"
        )

        // Should not throw exception
        val result = platzService.createPlatz(platzWithOptionals)
        assertEquals("Test Platz", result.name)
        assertEquals("20x40m", result.dimension)
        assertEquals("Sand", result.boden)

        // Test valid platz without optional fields
        val platzWithoutOptionals = createTestPlatz(
            name = "Simple Platz",
            dimension = null,
            boden = null
        )

        // Should not throw exception
        val result2 = platzService.createPlatz(platzWithoutOptionals)
        assertEquals("Simple Platz", result2.name)
        assertNull(result2.dimension)
        assertNull(result2.boden)
    }

    // Helper function to create test places
    private fun createTestPlatz(
        name: String,
        turnierId: com.benasher44.uuid.Uuid = uuid4(),
        dimension: String? = "20x40m",
        boden: String? = "Sand",
        typ: PlatzTypE = PlatzTypE.AUSTRAGUNG
    ): Platz {
        return Platz(
            id = uuid4(),
            turnierId = turnierId,
            name = name,
            dimension = dimension,
            boden = boden,
            typ = typ
        )
    }

    // Mock repository implementation for testing
    private class MockPlatzRepository : PlatzRepository {
        var plaetze = mutableListOf<Platz>()

        override suspend fun findAll(): List<Platz> = plaetze

        override suspend fun findById(id: com.benasher44.uuid.Uuid): Platz? =
            plaetze.find { it.id == id }

        override suspend fun findByTurnierId(turnierId: com.benasher44.uuid.Uuid): List<Platz> =
            plaetze.filter { it.turnierId == turnierId }

        override suspend fun findByTyp(typ: PlatzTypE): List<Platz> =
            plaetze.filter { it.typ == typ }

        override suspend fun search(query: String): List<Platz> =
            plaetze.filter { it.name.contains(query, ignoreCase = true) }

        override suspend fun create(platz: Platz): Platz {
            plaetze.add(platz)
            return platz
        }

        override suspend fun update(id: com.benasher44.uuid.Uuid, platz: Platz): Platz? {
            val index = plaetze.indexOfFirst { it.id == id }
            return if (index >= 0) {
                plaetze[index] = platz.copy(id = id)
                plaetze[index]
            } else null
        }

        override suspend fun delete(id: com.benasher44.uuid.Uuid): Boolean {
            return plaetze.removeIf { it.id == id }
        }
    }
}
