package at.mocode.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApiResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Test
    fun testApiResponseSerialization() {
        // Create ApiResponse instances with different configurations
        val successResponse = ApiResponse(
            success = true,
            message = "Operation successful",
            emailSent = true
        )

        val errorResponse = ApiResponse(
            success = false,
            message = "Operation failed",
            emailSent = false
        )

        val responseWithoutEmailSent = ApiResponse(
            success = true,
            message = "Operation completed"
            // emailSent is null by default
        )

        // Test serialization and deserialization for each case
        testSerializationRoundTrip(successResponse)
        testSerializationRoundTrip(errorResponse)
        testSerializationRoundTrip(responseWithoutEmailSent)
    }

    @Test
    fun testApiResponseDeserialization() {
        // JSON string with all fields
        val fullJsonString = """
        {
            "success": true,
            "message": "Email sent successfully",
            "emailSent": true
        }
        """.trimIndent()

        // JSON string without optional field
        val partialJsonString = """
        {
            "success": false,
            "message": "Failed to process request"
        }
        """.trimIndent()

        // Deserialize and verify full JSON
        val fullResponse = json.decodeFromString<ApiResponse>(fullJsonString)
        assertEquals(true, fullResponse.success)
        assertEquals("Email sent successfully", fullResponse.message)
        assertEquals(true, fullResponse.emailSent)

        // Deserialize and verify partial JSON
        val partialResponse = json.decodeFromString<ApiResponse>(partialJsonString)
        assertEquals(false, partialResponse.success)
        assertEquals("Failed to process request", partialResponse.message)
        assertNull(partialResponse.emailSent)
    }

    private fun testSerializationRoundTrip(response: ApiResponse) {
        // Serialize to JSON
        val jsonString = json.encodeToString(response)

        // Deserialize back to ApiResponse
        val decodedResponse = json.decodeFromString<ApiResponse>(jsonString)

        // Verify the deserialized object matches the original
        assertEquals(response.success, decodedResponse.success)
        assertEquals(response.message, decodedResponse.message)
        assertEquals(response.emailSent, decodedResponse.emailSent)
        assertEquals(response, decodedResponse) // Full object equality
    }
}
