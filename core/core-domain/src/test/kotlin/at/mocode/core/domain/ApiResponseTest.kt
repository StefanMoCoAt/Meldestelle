package at.mocode.core.domain

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApiResponseTest {

    @Test
    fun `ApiResponse success should create a successful response with data`() {
        // Arrange
        val testData = "This is a test"

        // Act
        val response = ApiResponse.success(testData)

        // Assert
        assertTrue(response.success, "Response should be successful")
        assertEquals(testData, response.data, "Response data should match the input data")
        assertTrue(response.errors.isEmpty(), "Errors list should be empty for a successful response")
        assertNotNull(response.timestamp, "Timestamp should be generated")
    }

    @Test
    fun `ApiResponse error with single message should create a failed response with one error`() {
        // Arrange
        val errorCode = "NOT_FOUND"
        val errorMessage = "The requested resource was not found."
        val errorField = "resourceId"

        // Act
        val response = ApiResponse.error<Unit>(errorCode, errorMessage, errorField)

        // Assert
        assertFalse(response.success, "Response should not be successful")
        assertNull(response.data, "Data should be null for a failed response")
        assertEquals(1, response.errors.size, "Should contain exactly one error")

        val error = response.errors.first()
        assertEquals(errorCode, error.code, "Error code should match")
        assertEquals(errorMessage, error.message, "Error message should match")
        assertEquals(errorField, error.field, "Error field should match")
    }

    @Test
    fun `ApiResponse error with list should create a failed response with multiple errors`() {
        // Arrange
        val errors = listOf(
            ErrorDto("INVALID_INPUT", "Username cannot be empty.", "username"),
            ErrorDto("INVALID_INPUT", "Password is too short.", "password")
        )

        // Act
        val response = ApiResponse.error<Unit>(errors)

        // Assert
        assertFalse(response.success, "Response should not be successful")
        assertNull(response.data, "Data should be null for a failed response")
        assertEquals(2, response.errors.size, "Should contain two errors")
        assertEquals(errors, response.errors, "The error list should match the input list")
    }
}
