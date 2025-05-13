package at.mocode.shared.entitaeten

import at.mocode.shared.model.MeisterschaftReferenz
import com.benasher44.uuid.uuid4
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MeisterschaftReferenzTest {

    @Test
    fun testCreateMeisterschaftReferenz() {
        // Create a MeisterschaftReferenz with minimal required parameters
        val meisterschaftId = uuid4()
        val meisterschaftReferenz = MeisterschaftReferenz(
            meisterschaftId = meisterschaftId,
            name = "Test Meisterschaft",
            betrifftBewerbNummern = listOf(1, 2, 3),
            berechnungsstrategie = null,
            reglementUrl = null
        )

        // Verify required fields
        assertEquals(meisterschaftId, meisterschaftReferenz.meisterschaftId)
        assertEquals("Test Meisterschaft", meisterschaftReferenz.name)
        assertEquals(listOf(1, 2, 3), meisterschaftReferenz.betrifftBewerbNummern)

        // Verify default values
        assertNotNull(meisterschaftReferenz.id)
        assertEquals(null, meisterschaftReferenz.berechnungsstrategie)
        assertEquals(null, meisterschaftReferenz.reglementUrl)
    }

    @Test
    fun testCreateMeisterschaftReferenzWithAllParameters() {
        // Create a MeisterschaftReferenz with all parameters
        val id = uuid4()
        val meisterschaftId = uuid4()

        val meisterschaftReferenz = MeisterschaftReferenz(
            id = id,
            meisterschaftId = meisterschaftId,
            name = "Vollständige Meisterschaft",
            betrifftBewerbNummern = listOf(10, 20, 30),
            berechnungsstrategie = "Punktesystem A",
            reglementUrl = "https://example.com/reglement"
        )

        // Verify all fields
        assertEquals(id, meisterschaftReferenz.id)
        assertEquals(meisterschaftId, meisterschaftReferenz.meisterschaftId)
        assertEquals("Vollständige Meisterschaft", meisterschaftReferenz.name)
        assertEquals(listOf(10, 20, 30), meisterschaftReferenz.betrifftBewerbNummern)
        assertEquals("Punktesystem A", meisterschaftReferenz.berechnungsstrategie)
        assertEquals("https://example.com/reglement", meisterschaftReferenz.reglementUrl)
    }

    @Test
    fun testModifyMeisterschaftReferenz() {
        // Create a MeisterschaftReferenz
        val meisterschaftId = uuid4()
        val meisterschaftReferenz = MeisterschaftReferenz(
            meisterschaftId = meisterschaftId,
            name = "Original Meisterschaft",
            betrifftBewerbNummern = listOf(1, 2, 3),
            berechnungsstrategie = null,
            reglementUrl = null
        )

        val newMeisterschaftId = uuid4()

        // Modify properties
        meisterschaftReferenz.meisterschaftId = newMeisterschaftId
        meisterschaftReferenz.name = "Geänderte Meisterschaft"
        meisterschaftReferenz.betrifftBewerbNummern = listOf(4, 5, 6)
        meisterschaftReferenz.berechnungsstrategie = "Neues Punktesystem"
        meisterschaftReferenz.reglementUrl = "https://example.com/neues-reglement"

        // Verify modifications
        assertEquals(newMeisterschaftId, meisterschaftReferenz.meisterschaftId)
        assertEquals("Geänderte Meisterschaft", meisterschaftReferenz.name)
        assertEquals(listOf(4, 5, 6), meisterschaftReferenz.betrifftBewerbNummern)
        assertEquals("Neues Punktesystem", meisterschaftReferenz.berechnungsstrategie)
        assertEquals("https://example.com/neues-reglement", meisterschaftReferenz.reglementUrl)
    }

    @Test
    fun testSerializationDeserialization() {
        // Create a MeisterschaftReferenz
        val meisterschaftId = uuid4()
        val meisterschaftReferenz = MeisterschaftReferenz(
            meisterschaftId = meisterschaftId,
            name = "Serialisierte Meisterschaft",
            betrifftBewerbNummern = listOf(7, 8, 9),
            berechnungsstrategie = "Punktesystem B",
            reglementUrl = "https://example.com/serialisiert"
        )

        // Serialize to JSON
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
        val jsonString = json.encodeToString(meisterschaftReferenz)

        // Verify JSON contains expected fields
        assertTrue(jsonString.contains("\"meisterschaftId\""), "JSON should contain meisterschaftId field")
        assertTrue(jsonString.contains(meisterschaftId.toString()), "JSON should contain meisterschaftId value")
        assertTrue(jsonString.contains("\"name\""), "JSON should contain name field")
        assertTrue(jsonString.contains("\"Serialisierte Meisterschaft\""), "JSON should contain name value")
        assertTrue(jsonString.contains("\"betrifftBewerbNummern\""), "JSON should contain betrifftBewerbNummern field")
        assertTrue(jsonString.contains("7"), "JSON should contain betrifftBewerbNummern value 7")
        assertTrue(jsonString.contains("8"), "JSON should contain betrifftBewerbNummern value 8")
        assertTrue(jsonString.contains("9"), "JSON should contain betrifftBewerbNummern value 9")
        assertTrue(jsonString.contains("\"berechnungsstrategie\""), "JSON should contain berechnungsstrategie field")
        assertTrue(jsonString.contains("\"Punktesystem B\""), "JSON should contain berechnungsstrategie value")
        assertTrue(jsonString.contains("\"reglementUrl\""), "JSON should contain reglementUrl field")
        assertTrue(jsonString.contains("\"https://example.com/serialisiert\""), "JSON should contain reglementUrl value")

        // Deserialize from JSON
        val deserializedMeisterschaftReferenz = json.decodeFromString<MeisterschaftReferenz>(jsonString)

        // Verify deserialized object matches original
        assertEquals(meisterschaftReferenz.id, deserializedMeisterschaftReferenz.id)
        assertEquals(meisterschaftReferenz.meisterschaftId, deserializedMeisterschaftReferenz.meisterschaftId)
        assertEquals(meisterschaftReferenz.name, deserializedMeisterschaftReferenz.name)
        assertEquals(meisterschaftReferenz.betrifftBewerbNummern, deserializedMeisterschaftReferenz.betrifftBewerbNummern)
        assertEquals(meisterschaftReferenz.berechnungsstrategie, deserializedMeisterschaftReferenz.berechnungsstrategie)
        assertEquals(meisterschaftReferenz.reglementUrl, deserializedMeisterschaftReferenz.reglementUrl)
    }

    @Test
    fun testCopyMeisterschaftReferenz() {
        // Create a MeisterschaftReferenz
        val meisterschaftId = uuid4()
        val original = MeisterschaftReferenz(
            meisterschaftId = meisterschaftId,
            name = "Original Meisterschaft",
            betrifftBewerbNummern = listOf(1, 2, 3),
            berechnungsstrategie = "Original Strategie",
            reglementUrl = null
        )

        val newMeisterschaftId = uuid4()

        // Create a copy with some modified properties
        val copy = original.copy(
            name = "Kopierte Meisterschaft",
            meisterschaftId = newMeisterschaftId,
            reglementUrl = "https://example.com/kopie"
        )

        // Verify copied properties
        assertEquals(original.id, copy.id)
        assertEquals(original.betrifftBewerbNummern, copy.betrifftBewerbNummern)
        assertEquals(original.berechnungsstrategie, copy.berechnungsstrategie)

        // Verify modified properties
        assertEquals("Kopierte Meisterschaft", copy.name)
        assertEquals(newMeisterschaftId, copy.meisterschaftId)
        assertEquals("https://example.com/kopie", copy.reglementUrl)
    }
}
