package at.mocode.shared.entitaeten

import at.mocode.shared.enums.PlatzTyp
import com.benasher44.uuid.uuid4
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlatzTest {

    @Test
    fun testCreatePlatz() {
        // Create a Platz with minimal required parameters
        val platz = Platz(
            name = "Hauptplatz",
            dimension = null,
            boden = null,
            typ = PlatzTyp.AUSTRAGUNG
        )

        // Verify required fields
        assertEquals("Hauptplatz", platz.name)
        assertEquals(PlatzTyp.AUSTRAGUNG, platz.typ)

        // Verify default values
        assertNotNull(platz.id)
        assertEquals(null, platz.dimension)
        assertEquals(null, platz.boden)
    }

    @Test
    fun testCreatePlatzWithAllParameters() {
        // Create a Platz with all parameters
        val id = uuid4()

        val platz = Platz(
            id = id,
            name = "Vollständiger Platz",
            dimension = "60x20m",
            boden = "Sand",
            typ = PlatzTyp.VORBEREITUNG
        )

        // Verify all fields
        assertEquals(id, platz.id)
        assertEquals("Vollständiger Platz", platz.name)
        assertEquals("60x20m", platz.dimension)
        assertEquals("Sand", platz.boden)
        assertEquals(PlatzTyp.VORBEREITUNG, platz.typ)
    }

    @Test
    fun testModifyPlatz() {
        // Create a Platz
        val platz = Platz(
            name = "Original Platz",
            dimension = null,
            boden = null,
            typ = PlatzTyp.AUSTRAGUNG
        )

        // Modify properties
        platz.name = "Geänderter Platz"
        platz.dimension = "80x40m"
        platz.boden = "Gras"
        platz.typ = PlatzTyp.LONGIEREN

        // Verify modifications
        assertEquals("Geänderter Platz", platz.name)
        assertEquals("80x40m", platz.dimension)
        assertEquals("Gras", platz.boden)
        assertEquals(PlatzTyp.LONGIEREN, platz.typ)
    }

    @Test
    fun testSerializationDeserialization() {
        // Create a Platz
        val platz = Platz(
            name = "Serialisierter Platz",
            dimension = "70x30m",
            boden = "Kunstrasen",
            typ = PlatzTyp.SONSTIGES
        )

        // Serialize to JSON
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
        val jsonString = json.encodeToString(platz)

        // Verify JSON contains expected fields
        assertTrue(jsonString.contains("\"name\""), "JSON should contain name field")
        assertTrue(jsonString.contains("\"Serialisierter Platz\""), "JSON should contain name value")
        assertTrue(jsonString.contains("\"dimension\""), "JSON should contain dimension field")
        assertTrue(jsonString.contains("\"70x30m\""), "JSON should contain dimension value")
        assertTrue(jsonString.contains("\"boden\""), "JSON should contain boden field")
        assertTrue(jsonString.contains("\"Kunstrasen\""), "JSON should contain boden value")
        assertTrue(jsonString.contains("\"typ\""), "JSON should contain typ field")
        assertTrue(jsonString.contains("\"SONSTIGES\""), "JSON should contain typ value")

        // Deserialize from JSON
        val deserializedPlatz = json.decodeFromString<Platz>(jsonString)

        // Verify deserialized object matches original
        assertEquals(platz.id, deserializedPlatz.id)
        assertEquals(platz.name, deserializedPlatz.name)
        assertEquals(platz.dimension, deserializedPlatz.dimension)
        assertEquals(platz.boden, deserializedPlatz.boden)
        assertEquals(platz.typ, deserializedPlatz.typ)
    }

    @Test
    fun testCopyPlatz() {
        // Create a Platz
        val original = Platz(
            name = "Original Platz",
            dimension = "50x25m",
            boden = "Holzspäne",
            typ = PlatzTyp.VORBEREITUNG
        )

        // Create a copy with some modified properties
        val copy = original.copy(
            name = "Kopierter Platz",
            typ = PlatzTyp.LONGIEREN
        )

        // Verify copied properties
        assertEquals(original.id, copy.id)
        assertEquals(original.dimension, copy.dimension)
        assertEquals(original.boden, copy.boden)

        // Verify modified properties
        assertEquals("Kopierter Platz", copy.name)
        assertEquals(PlatzTyp.LONGIEREN, copy.typ)
    }
}
