package at.mocode

import at.mocode.utils.ApiResponse
import at.mocode.utils.ErrorResponse
import kotlin.test.*

/**
 * Comprehensive test suite for ApiResponse utility classes and data structures.
 *
 * This test class verifies:
 * - ApiResponse data class structure and behavior
 * - ErrorResponse data class structure and behavior
 * - Data class serialization compatibility
 * - Proper field assignments and null handling
 */
class ApiResponseTest {

    @Test
    fun testApiResponseDataClassSuccess() {
        val response = ApiResponse(
            success = true,
            data = mapOf("id" to "1", "name" to "Test"),
            message = "Operation successful"
        )

        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals("Operation successful", response.message)
        assertNull(response.error)
    }

    @Test
    fun testApiResponseDataClassError() {
        val response = ApiResponse<Nothing>(
            success = false,
            error = "VALIDATION_ERROR",
            message = "Invalid input"
        )

        assertFalse(response.success)
        assertNull(response.data)
        assertEquals("VALIDATION_ERROR", response.error)
        assertEquals("Invalid input", response.message)
    }

    @Test
    fun testApiResponseDataClassMinimal() {
        val response = ApiResponse<String>(success = true)

        assertTrue(response.success)
        assertNull(response.data)
        assertNull(response.error)
        assertNull(response.message)
    }

    @Test
    fun testApiResponseDataClassWithNullData() {
        val response = ApiResponse<String>(
            success = true,
            data = null,
            message = "No data available"
        )

        assertTrue(response.success)
        assertNull(response.data)
        assertNull(response.error)
        assertEquals("No data available", response.message)
    }

    @Test
    fun testApiResponseDataClassWithAllFields() {
        val response = ApiResponse(
            success = false,
            data = "some data",
            error = "ERROR_CODE",
            message = "Error message"
        )

        assertFalse(response.success)
        assertEquals("some data", response.data)
        assertEquals("ERROR_CODE", response.error)
        assertEquals("Error message", response.message)
    }

    @Test
    fun testErrorResponseDataClass() {
        val errorResponse = ErrorResponse(
            code = "NOT_FOUND",
            message = "Resource not found",
            details = "The requested item does not exist"
        )

        assertEquals("NOT_FOUND", errorResponse.code)
        assertEquals("Resource not found", errorResponse.message)
        assertEquals("The requested item does not exist", errorResponse.details)
    }

    @Test
    fun testErrorResponseDataClassWithoutDetails() {
        val errorResponse = ErrorResponse(
            code = "VALIDATION_ERROR",
            message = "Invalid input"
        )

        assertEquals("VALIDATION_ERROR", errorResponse.code)
        assertEquals("Invalid input", errorResponse.message)
        assertNull(errorResponse.details)
    }

    @Test
    fun testErrorResponseDataClassWithEmptyDetails() {
        val errorResponse = ErrorResponse(
            code = "INTERNAL_ERROR",
            message = "Server error",
            details = ""
        )

        assertEquals("INTERNAL_ERROR", errorResponse.code)
        assertEquals("Server error", errorResponse.message)
        assertEquals("", errorResponse.details)
    }

    @Test
    fun testApiResponseEquality() {
        val response1 = ApiResponse(
            success = true,
            data = "test",
            message = "success"
        )

        val response2 = ApiResponse(
            success = true,
            data = "test",
            message = "success"
        )

        assertEquals(response1, response2)
    }

    @Test
    fun testApiResponseInequality() {
        val response1 = ApiResponse(
            success = true,
            data = "test1",
            message = "success"
        )

        val response2 = ApiResponse(
            success = true,
            data = "test2",
            message = "success"
        )

        assertNotEquals(response1, response2)
    }

    @Test
    fun testErrorResponseEquality() {
        val error1 = ErrorResponse(
            code = "ERROR",
            message = "Test error",
            details = "Details"
        )

        val error2 = ErrorResponse(
            code = "ERROR",
            message = "Test error",
            details = "Details"
        )

        assertEquals(error1, error2)
    }

    @Test
    fun testErrorResponseInequality() {
        val error1 = ErrorResponse(
            code = "ERROR1",
            message = "Test error",
            details = "Details"
        )

        val error2 = ErrorResponse(
            code = "ERROR2",
            message = "Test error",
            details = "Details"
        )

        assertNotEquals(error1, error2)
    }

    @Test
    fun testApiResponseToString() {
        val response = ApiResponse(
            success = true,
            data = "test",
            message = "success"
        )

        val toString = response.toString()
        assertTrue(toString.contains("success=true"))
        assertTrue(toString.contains("data=test"))
        assertTrue(toString.contains("message=success"))
    }

    @Test
    fun testErrorResponseToString() {
        val error = ErrorResponse(
            code = "ERROR",
            message = "Test error",
            details = "Details"
        )

        val toString = error.toString()
        assertTrue(toString.contains("code=ERROR"))
        assertTrue(toString.contains("message=Test error"))
        assertTrue(toString.contains("details=Details"))
    }

    @Test
    fun testApiResponseCopy() {
        val original = ApiResponse(
            success = true,
            data = "original",
            message = "original message"
        )

        val copied = original.copy(data = "modified")

        assertTrue(copied.success)
        assertEquals("modified", copied.data)
        assertEquals("original message", copied.message)
        assertNull(copied.error)
    }

    @Test
    fun testErrorResponseCopy() {
        val original = ErrorResponse(
            code = "ERROR",
            message = "Original message",
            details = "Original details"
        )

        val copied = original.copy(message = "Modified message")

        assertEquals("ERROR", copied.code)
        assertEquals("Modified message", copied.message)
        assertEquals("Original details", copied.details)
    }

}
