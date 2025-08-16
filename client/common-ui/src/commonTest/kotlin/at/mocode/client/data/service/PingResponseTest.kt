package at.mocode.client.data.service

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PingResponseTest {

    @Test
    fun `should create PingResponse with status`() {
        // Given
        val status = "pong"

        // When
        val response = PingResponse(status = status)

        // Then
        assertEquals(status, response.status)
        assertNotNull(response)
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val response = PingResponse(status = "pong")

        // When
        val json = Json.encodeToString(response)

        // Then
        assertTrue(json.contains("\"status\":\"pong\""))
        assertTrue(json.startsWith("{"))
        assertTrue(json.endsWith("}"))
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val json = """{"status":"pong"}"""

        // When
        val response = Json.decodeFromString<PingResponse>(json)

        // Then
        assertEquals("pong", response.status)
    }

    @Test
    fun `should handle different status values`() {
        // Given & When & Then
        val responses = listOf("pong", "ok", "alive", "healthy")

        responses.forEach { status ->
            val response = PingResponse(status = status)
            assertEquals(status, response.status)

            // Test serialization roundtrip
            val json = Json.encodeToString(response)
            val deserialized = Json.decodeFromString<PingResponse>(json)
            assertEquals(status, deserialized.status)
        }
    }

    @Test
    fun `should handle empty status`() {
        // Given
        val emptyStatus = ""

        // When
        val response = PingResponse(status = emptyStatus)

        // Then
        assertEquals("", response.status)

        // Test serialization works with empty string
        val json = Json.encodeToString(response)
        val deserialized = Json.decodeFromString<PingResponse>(json)
        assertEquals("", deserialized.status)
    }

    @Test
    fun `should be data class with proper equals and hashCode`() {
        // Given
        val response1 = PingResponse("pong")
        val response2 = PingResponse("pong")
        val response3 = PingResponse("different")

        // Then
        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())
        assertTrue(response1 != response3)
    }

    @Test
    fun `should have proper toString representation`() {
        // Given
        val response = PingResponse("pong")

        // When
        val toString = response.toString()

        // Then
        assertTrue(toString.contains("PingResponse"))
        assertTrue(toString.contains("pong"))
    }
}
