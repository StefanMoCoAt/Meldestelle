package at.mocode.shared.entitaeten

import com.benasher44.uuid.uuid4
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class ArtikelTest {

    @Test
    fun testCreateArtikel() {
        // Create an Artikel with minimal required parameters
        val artikel = Artikel(
            bezeichnung = "Test Artikel",
            preis = BigDecimal.parseString("10.50"),
            einheit = "Stück"
        )

        // Verify required fields
        assertEquals("Test Artikel", artikel.bezeichnung)
        assertEquals(BigDecimal.parseString("10.50"), artikel.preis)
        assertEquals("Stück", artikel.einheit)

        // Verify default values
        assertNotNull(artikel.id)
        assertEquals(false, artikel.istVerbandsabgabe)
        assertNotNull(artikel.createdAt)
        assertNotNull(artikel.updatedAt)
    }

    @Test
    fun testCreateArtikelWithAllParameters() {
        // Create an Artikel with all parameters
        val id = uuid4()
        val now = Clock.System.now()

        val artikel = Artikel(
            id = id,
            bezeichnung = "Vollständiger Artikel",
            preis = BigDecimal.parseString("99.99"),
            einheit = "Paket",
            istVerbandsabgabe = true,
            createdAt = now,
            updatedAt = now
        )

        // Verify all fields
        assertEquals(id, artikel.id)
        assertEquals("Vollständiger Artikel", artikel.bezeichnung)
        assertEquals(BigDecimal.parseString("99.99"), artikel.preis)
        assertEquals("Paket", artikel.einheit)
        assertEquals(true, artikel.istVerbandsabgabe)
        assertEquals(now, artikel.createdAt)
        assertEquals(now, artikel.updatedAt)
    }

    @Test
    fun testModifyArtikel() {
        // Create an Artikel
        val artikel = Artikel(
            bezeichnung = "Original Artikel",
            preis = BigDecimal.parseString("10.00"),
            einheit = "Stück"
        )

        val originalUpdatedAt = artikel.updatedAt.toString()

        // Modify properties
        artikel.bezeichnung = "Geänderter Artikel"
        artikel.preis = BigDecimal.parseString("15.00")
        artikel.einheit = "Box"
        artikel.istVerbandsabgabe = true
        artikel.updatedAt = Clock.System.now().plus(1.milliseconds)

        // Verify modifications
        assertEquals("Geänderter Artikel", artikel.bezeichnung)
        assertEquals(BigDecimal.parseString("15.00"), artikel.preis)
        assertEquals("Box", artikel.einheit)
        assertEquals(true, artikel.istVerbandsabgabe)
        assertNotEquals(originalUpdatedAt, artikel.updatedAt.toString())
    }

    @Test
    fun testSerializationDeserialization() {
        // Create an Artikel
        val artikel = Artikel(
            bezeichnung = "Serialisierter Artikel",
            preis = BigDecimal.parseString("25.75"),
            einheit = "Einheit",
            istVerbandsabgabe = true
        )

        // Serialize to JSON
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
        val jsonString = json.encodeToString(artikel)

        // Verify JSON contains expected fields
        assertTrue(jsonString.contains("\"bezeichnung\""), "JSON should contain bezeichnung field")
        assertTrue(jsonString.contains("\"Serialisierter Artikel\""), "JSON should contain value Serialisierter Artikel")
        assertTrue(jsonString.contains("\"preis\""), "JSON should contain preis field")
        assertTrue(jsonString.contains("\"25.75\""), "JSON should contain value 25.75")
        assertTrue(jsonString.contains("\"einheit\""), "JSON should contain einheit field")
        assertTrue(jsonString.contains("\"Einheit\""), "JSON should contain value Einheit")
        assertTrue(jsonString.contains("\"istVerbandsabgabe\""), "JSON should contain istVerbandsabgabe field")
        assertTrue(jsonString.contains("true"), "JSON should contain value true")

        // Deserialize from JSON
        val deserializedArtikel = json.decodeFromString<Artikel>(jsonString)

        // Verify deserialized object matches original
        assertEquals(artikel.id, deserializedArtikel.id)
        assertEquals(artikel.bezeichnung, deserializedArtikel.bezeichnung)
        assertEquals(artikel.preis, deserializedArtikel.preis)
        assertEquals(artikel.einheit, deserializedArtikel.einheit)
        assertEquals(artikel.istVerbandsabgabe, deserializedArtikel.istVerbandsabgabe)
        assertEquals(artikel.createdAt, deserializedArtikel.createdAt)
        assertEquals(artikel.updatedAt, deserializedArtikel.updatedAt)
    }

    @Test
    fun testCopyArtikel() {
        // Create an Artikel
        val original = Artikel(
            bezeichnung = "Original Artikel",
            preis = BigDecimal.parseString("10.00"),
            einheit = "Stück"
        )

        // Create a copy with some modified properties
        val copy = original.copy(
            bezeichnung = "Kopierter Artikel",
            preis = BigDecimal.parseString("12.50")
        )

        // Verify copied properties
        assertEquals(original.id, copy.id)
        assertEquals(original.einheit, copy.einheit)
        assertEquals(original.istVerbandsabgabe, copy.istVerbandsabgabe)
        assertEquals(original.createdAt, copy.createdAt)
        assertEquals(original.updatedAt, copy.updatedAt)

        // Verify modified properties
        assertEquals("Kopierter Artikel", copy.bezeichnung)
        assertEquals(BigDecimal.parseString("12.50"), copy.preis)
    }
}
