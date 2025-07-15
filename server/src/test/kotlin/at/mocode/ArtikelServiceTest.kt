package at.mocode

import at.mocode.model.Artikel
import at.mocode.repositories.ArtikelRepository
import at.mocode.services.ArtikelService
import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.test.*

class ArtikelServiceTest {

    private lateinit var mockRepository: MockArtikelRepository
    private lateinit var artikelService: ArtikelService

    @BeforeTest
    fun setup() {
        mockRepository = MockArtikelRepository()
        artikelService = ArtikelService(mockRepository)
    }

    @Test
    fun testGetAllArtikel() = runBlocking {
        // Given
        val artikel1 = createTestArtikel("Test Artikel 1")
        val artikel2 = createTestArtikel("Test Artikel 2")
        mockRepository.articles = mutableListOf(artikel1, artikel2)

        // When
        val result = artikelService.getAllArtikel()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.contains(artikel1))
        assertTrue(result.contains(artikel2))
    }

    @Test
    fun testGetArtikelById() = runBlocking {
        // Given
        val artikel = createTestArtikel("Test Artikel")
        mockRepository.articles = mutableListOf(artikel)

        // When
        val result = artikelService.getArtikelById(artikel.id)

        // Then
        assertNotNull(result)
        assertEquals(artikel.id, result.id)
        assertEquals(artikel.bezeichnung, result.bezeichnung)
    }

    @Test
    fun testGetArtikelByIdNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()

        // When
        val result = artikelService.getArtikelById(nonExistentId)

        // Then
        assertNull(result)
    }

    @Test
    fun testGetArtikelByVerbandsabgabe() = runBlocking {
        // Given
        val verbandsArtikel = createTestArtikel("Verbands Artikel", istVerbandsabgabe = true)
        val normalArtikel = createTestArtikel("Normal Artikel", istVerbandsabgabe = false)
        mockRepository.articles = mutableListOf(verbandsArtikel, normalArtikel)

        // When
        val verbandsResult = artikelService.getArtikelByVerbandsabgabe(true)
        val normalResult = artikelService.getArtikelByVerbandsabgabe(false)

        // Then
        assertEquals(1, verbandsResult.size)
        assertEquals(verbandsArtikel.id, verbandsResult[0].id)

        assertEquals(1, normalResult.size)
        assertEquals(normalArtikel.id, normalResult[0].id)
    }

    @Test
    fun testGetVerbandsabgabeArtikel() = runBlocking {
        // Given
        val verbandsArtikel = createTestArtikel("Verbands Artikel", istVerbandsabgabe = true)
        val normalArtikel = createTestArtikel("Normal Artikel", istVerbandsabgabe = false)
        mockRepository.articles = mutableListOf(verbandsArtikel, normalArtikel)

        // When
        val result = artikelService.getVerbandsabgabeArtikel()

        // Then
        assertEquals(1, result.size)
        assertEquals(verbandsArtikel.id, result[0].id)
        assertTrue(result[0].istVerbandsabgabe)
    }

    @Test
    fun testGetNonVerbandsabgabeArtikel() = runBlocking {
        // Given
        val verbandsArtikel = createTestArtikel("Verbands Artikel", istVerbandsabgabe = true)
        val normalArtikel = createTestArtikel("Normal Artikel", istVerbandsabgabe = false)
        mockRepository.articles = mutableListOf(verbandsArtikel, normalArtikel)

        // When
        val result = artikelService.getNonVerbandsabgabeArtikel()

        // Then
        assertEquals(1, result.size)
        assertEquals(normalArtikel.id, result[0].id)
        assertFalse(result[0].istVerbandsabgabe)
    }

    @Test
    fun testSearchArtikel() = runBlocking {
        // Given
        val artikel1 = createTestArtikel("Test Artikel")
        val artikel2 = createTestArtikel("Another Item")
        mockRepository.articles = mutableListOf(artikel1, artikel2)

        // When
        val result = artikelService.searchArtikel("Test")

        // Then
        assertEquals(1, result.size)
        assertEquals(artikel1.id, result[0].id)
    }

    @Test
    fun testSearchArtikelBlankQuery() {
        // When & Then
        assertFailsWith<IllegalArgumentException> {
            runBlocking { artikelService.searchArtikel("") }
        }

        assertFailsWith<IllegalArgumentException> {
            runBlocking { artikelService.searchArtikel("   ") }
        }
    }

    @Test
    fun testCreateArtikel() = runBlocking {
        // Given
        val artikel = createTestArtikel("New Artikel")

        // When
        val result = artikelService.createArtikel(artikel)

        // Then
        assertEquals(artikel.bezeichnung, result.bezeichnung)
        assertEquals(artikel.preis, result.preis)
        assertEquals(artikel.einheit, result.einheit)
        assertTrue(mockRepository.articles.contains(result))
    }

    @Test
    fun testCreateArtikelValidation() {
        // Test blank bezeichnung
        assertFailsWith<IllegalArgumentException> {
            runBlocking { artikelService.createArtikel(createTestArtikel("")) }
        }

        // Test long bezeichnung
        assertFailsWith<IllegalArgumentException> {
            runBlocking { artikelService.createArtikel(createTestArtikel("a".repeat(256))) }
        }

        // Test negative price
        assertFailsWith<IllegalArgumentException> {
            runBlocking { artikelService.createArtikel(createTestArtikel("Test", preis = BigDecimal.fromInt(-1))) }
        }

        // Test blank einheit
        assertFailsWith<IllegalArgumentException> {
            runBlocking { artikelService.createArtikel(createTestArtikel("Test", einheit = "")) }
        }

        // Test long einheit
        assertFailsWith<IllegalArgumentException> {
            runBlocking { artikelService.createArtikel(createTestArtikel("Test", einheit = "a".repeat(51))) }
        }
    }

    @Test
    fun testUpdateArtikel() = runBlocking {
        // Given
        val originalArtikel = createTestArtikel("Original")
        mockRepository.articles = mutableListOf(originalArtikel)

        val updatedArtikel = originalArtikel.copy(bezeichnung = "Updated")

        // When
        val result = artikelService.updateArtikel(originalArtikel.id, updatedArtikel)

        // Then
        assertNotNull(result)
        assertEquals("Updated", result.bezeichnung)
    }

    @Test
    fun testUpdateArtikelNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()
        val artikel = createTestArtikel("Test")

        // When
        val result = artikelService.updateArtikel(nonExistentId, artikel)

        // Then
        assertNull(result)
    }

    @Test
    fun testUpdateArtikelValidation() {
        // Given
        val originalArtikel = createTestArtikel("Original")
        mockRepository.articles = mutableListOf(originalArtikel)

        // Test validation during update
        assertFailsWith<IllegalArgumentException> {
            runBlocking { artikelService.updateArtikel(originalArtikel.id, createTestArtikel("")) }
        }
    }

    @Test
    fun testDeleteArtikel() = runBlocking {
        // Given
        val artikel = createTestArtikel("To Delete")
        mockRepository.articles = mutableListOf(artikel)

        // When
        val result = artikelService.deleteArtikel(artikel.id)

        // Then
        assertTrue(result)
        assertFalse(mockRepository.articles.contains(artikel))
    }

    @Test
    fun testDeleteArtikelNotFound() = runBlocking {
        // Given
        val nonExistentId = uuid4()

        // When
        val result = artikelService.deleteArtikel(nonExistentId)

        // Then
        assertFalse(result)
    }

    // Helper function to create test articles
    private fun createTestArtikel(
        bezeichnung: String,
        preis: BigDecimal = BigDecimal.fromInt(100),
        einheit: String = "Stück",
        istVerbandsabgabe: Boolean = false
    ): Artikel {
        return Artikel(
            id = uuid4(),
            bezeichnung = bezeichnung,
            preis = preis,
            einheit = einheit,
            istVerbandsabgabe = istVerbandsabgabe,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    // Mock repository implementation for testing
    private class MockArtikelRepository : ArtikelRepository {
        var articles = mutableListOf<Artikel>()

        override suspend fun findAll(): List<Artikel> = articles

        override suspend fun findById(id: com.benasher44.uuid.Uuid): Artikel? =
            articles.find { it.id == id }

        override suspend fun findByVerbandsabgabe(istVerbandsabgabe: Boolean): List<Artikel> =
            articles.filter { it.istVerbandsabgabe == istVerbandsabgabe }

        override suspend fun search(query: String): List<Artikel> =
            articles.filter { it.bezeichnung.contains(query, ignoreCase = true) }

        override suspend fun create(artikel: Artikel): Artikel {
            articles.add(artikel)
            return artikel
        }

        override suspend fun update(id: com.benasher44.uuid.Uuid, artikel: Artikel): Artikel? {
            val index = articles.indexOfFirst { it.id == id }
            return if (index >= 0) {
                articles[index] = artikel.copy(id = id)
                articles[index]
            } else null
        }

        override suspend fun delete(id: com.benasher44.uuid.Uuid): Boolean {
            return articles.removeIf { it.id == id }
        }
    }
}
