package at.mocode.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class NennungTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Test
    fun testNennungSerialization() {
        // Create a Nennung instance
        val nennung = Nennung(
            riderName = "Max Mustermann",
            horseName = "Blitz",
            email = "max@example.com",
            phone = "0123456789",
            selectedEvents = listOf("1 Pony Stilspringprüfung 60 cm", "2 Stilspringprüfung 60 cm"),
            comments = "Bitte frühen Startplatz"
        )

        // Serialize to JSON
        val jsonString = json.encodeToString(nennung)

        // Deserialize back to Nennung
        val decodedNennung = json.decodeFromString<Nennung>(jsonString)

        // Verify the deserialized object matches the original
        assertEquals(nennung.riderName, decodedNennung.riderName)
        assertEquals(nennung.horseName, decodedNennung.horseName)
        assertEquals(nennung.email, decodedNennung.email)
        assertEquals(nennung.phone, decodedNennung.phone)
        assertEquals(nennung.selectedEvents, decodedNennung.selectedEvents)
        assertEquals(nennung.comments, decodedNennung.comments)
        assertEquals(nennung, decodedNennung) // Full object equality
    }

    @Test
    fun testNennungDeserialization() {
        // JSON string representing a Nennung
        val jsonString = """
        {
            "riderName": "Anna Schmidt",
            "horseName": "Donner",
            "email": "anna@example.com",
            "phone": "9876543210",
            "selectedEvents": ["4 Stilspringprüfung 80 cm", "6 Stilspringprüfung 95 cm"],
            "comments": "Keine besonderen Wünsche"
        }
        """.trimIndent()

        // Deserialize to Nennung
        val nennung = json.decodeFromString<Nennung>(jsonString)

        // Verify the deserialized object has the expected values
        assertEquals("Anna Schmidt", nennung.riderName)
        assertEquals("Donner", nennung.horseName)
        assertEquals("anna@example.com", nennung.email)
        assertEquals("9876543210", nennung.phone)
        assertEquals(listOf("4 Stilspringprüfung 80 cm", "6 Stilspringprüfung 95 cm"), nennung.selectedEvents)
        assertEquals("Keine besonderen Wünsche", nennung.comments)
    }
}
