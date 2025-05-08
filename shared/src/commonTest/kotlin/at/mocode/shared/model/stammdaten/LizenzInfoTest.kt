package at.mocode.shared.model.stammdaten

import at.mocode.shared.model.enums.LizenzTyp
import at.mocode.shared.model.enums.Sparte
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LizenzInfoTest {

    @Test
    fun testCreateLizenzInfoWithMinimalParameters() {
        // Create a LizenzInfo with minimal required parameters
        val lizenzInfo = LizenzInfo(
            lizenzTyp = LizenzTyp.REITER,
            stufe = null,
            sparte = null,
            gueltigBisJahr = null,
            ausgestelltAm = null
        )

        // Verify required fields
        assertEquals(LizenzTyp.REITER, lizenzInfo.lizenzTyp)

        // Verify optional fields are null
        assertEquals(null, lizenzInfo.stufe)
        assertEquals(null, lizenzInfo.sparte)
        assertEquals(null, lizenzInfo.gueltigBisJahr)
        assertEquals(null, lizenzInfo.ausgestelltAm)
    }

    @Test
    fun testCreateLizenzInfoWithAllParameters() {
        // Create a LizenzInfo with all parameters
        val ausgestelltAm = LocalDate(2023, 1, 15)

        val lizenzInfo = LizenzInfo(
            lizenzTyp = LizenzTyp.FAHRER,
            stufe = "A",
            sparte = Sparte.DRESSUR,
            gueltigBisJahr = 2024,
            ausgestelltAm = ausgestelltAm
        )

        // Verify all fields
        assertEquals(LizenzTyp.FAHRER, lizenzInfo.lizenzTyp)
        assertEquals("A", lizenzInfo.stufe)
        assertEquals(Sparte.DRESSUR, lizenzInfo.sparte)
        assertEquals(2024, lizenzInfo.gueltigBisJahr)
        assertEquals(ausgestelltAm, lizenzInfo.ausgestelltAm)
    }

    @Test
    fun testSerializationDeserialization() {
        // Create a LizenzInfo with all parameters
        val ausgestelltAm = LocalDate(2023, 5, 20)

        val lizenzInfo = LizenzInfo(
            lizenzTyp = LizenzTyp.VOLTIGIERER,
            stufe = "B",
            sparte = Sparte.VOLTIGIEREN,
            gueltigBisJahr = 2025,
            ausgestelltAm = ausgestelltAm
        )

        // Serialize to JSON
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
        val jsonString = json.encodeToString(lizenzInfo)

        // Verify JSON contains expected fields
        assertTrue(jsonString.contains("\"lizenzTyp\""), "JSON should contain lizenzTyp field")
        assertTrue(jsonString.contains("\"VOLTIGIERER\""), "JSON should contain value VOLTIGIERER")
        assertTrue(jsonString.contains("\"stufe\""), "JSON should contain stufe field")
        assertTrue(jsonString.contains("\"B\""), "JSON should contain value B")
        assertTrue(jsonString.contains("\"sparte\""), "JSON should contain sparte field")
        assertTrue(jsonString.contains("\"VOLTIGIEREN\""), "JSON should contain value VOLTIGIEREN")
        assertTrue(jsonString.contains("\"gueltigBisJahr\""), "JSON should contain gueltigBisJahr field")
        assertTrue(jsonString.contains("2025"), "JSON should contain value 2025")
        assertTrue(jsonString.contains("\"ausgestelltAm\""), "JSON should contain ausgestelltAm field")
        assertTrue(jsonString.contains("2023-05-20"), "JSON should contain value 2023-05-20")

        // Deserialize from JSON
        val deserializedLizenzInfo = json.decodeFromString<LizenzInfo>(jsonString)

        // Verify deserialized object matches original
        assertEquals(lizenzInfo.lizenzTyp, deserializedLizenzInfo.lizenzTyp)
        assertEquals(lizenzInfo.stufe, deserializedLizenzInfo.stufe)
        assertEquals(lizenzInfo.sparte, deserializedLizenzInfo.sparte)
        assertEquals(lizenzInfo.gueltigBisJahr, deserializedLizenzInfo.gueltigBisJahr)
        assertEquals(lizenzInfo.ausgestelltAm, deserializedLizenzInfo.ausgestelltAm)
    }

    @Test
    fun testCopyLizenzInfo() {
        // Create a LizenzInfo
        val original = LizenzInfo(
            lizenzTyp = LizenzTyp.WESTERN,
            stufe = "C",
            sparte = Sparte.WESTERN,
            gueltigBisJahr = 2023,
            ausgestelltAm = null
        )

        // Create a copy with some modified properties
        val copy = original.copy(
            stufe = "B",
            gueltigBisJahr = 2024,
            ausgestelltAm = LocalDate(2023, 12, 1)
        )

        // Verify copied properties
        assertEquals(original.lizenzTyp, copy.lizenzTyp)
        assertEquals(original.sparte, copy.sparte)

        // Verify modified properties
        assertEquals("B", copy.stufe)
        assertEquals(2024, copy.gueltigBisJahr)
        assertEquals(LocalDate(2023, 12, 1), copy.ausgestelltAm)
    }

    @Test
    fun testDifferentLizenzTypes() {
        // Test different LizenzTyp values
        val lizenzTypes = listOf(
            LizenzTyp.REITER,
            LizenzTyp.FAHRER,
            LizenzTyp.VOLTIGIERER,
            LizenzTyp.WESTERN,
            LizenzTyp.WORKING_EQUITATION,
            LizenzTyp.POLO,
            LizenzTyp.STARTKARTE_ALLG,
            LizenzTyp.STARTKARTE_VOLTIGIEREN,
            LizenzTyp.STARTKARTE_WESTERN,
            LizenzTyp.STARTKARTE_ISLAND,
            LizenzTyp.STARTKARTE_FAHREN_JUGEND,
            LizenzTyp.STARTKARTE_HORSEBALL,
            LizenzTyp.STARTKARTE_POLO,
            LizenzTyp.PARAEQUESTRIAN,
            LizenzTyp.SONSTIGE
        )

        for (lizenzTyp in lizenzTypes) {
            val lizenzInfo = LizenzInfo(
                lizenzTyp = lizenzTyp,
                stufe = "Test",
                sparte = Sparte.SONSTIGES,
                gueltigBisJahr = 2024,
                ausgestelltAm = null
            )

            assertEquals(lizenzTyp, lizenzInfo.lizenzTyp)

            // Serialize and deserialize to verify enum handling
            val json = Json { encodeDefaults = true }
            val jsonString = json.encodeToString(lizenzInfo)
            val deserializedLizenzInfo = json.decodeFromString<LizenzInfo>(jsonString)

            assertEquals(lizenzTyp, deserializedLizenzInfo.lizenzTyp)
        }
    }
}
