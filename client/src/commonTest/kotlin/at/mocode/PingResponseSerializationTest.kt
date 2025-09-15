package at.mocode

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class PingResponseSerializationTest {

    @Test
    fun `should decode PingResponse with unknown fields and nulls omitted`() {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = false
            encodeDefaults = false
            prettyPrint = false
            explicitNulls = false
        }

        val input = """
            {
              "status": "OK",
              "timestamp": "2025-09-15T20:00:00Z",
              "message": null,
              "extra": 123,
              "nested": {"foo": "bar"}
            }
        """.trimIndent()

        val decoded = json.decodeFromString(PingResponse.serializer(), input)

        assertEquals("OK", decoded.status)
        assertEquals("2025-09-15T20:00:00Z", decoded.timestamp)
        // message is nullable and nulls are omitted; ensure it's null when input is null
        assertEquals(null, decoded.message)
    }
}
