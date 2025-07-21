package at.mocode.validation.test

import at.mocode.validation.ApiValidationUtils
import at.mocode.validation.ValidationError
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlinx.datetime.LocalDate

/**
 * Test class for API validation utilities.
 *
 * This test verifies that the validation implementation works correctly
 * for all API endpoints.
 */
class ValidationTest {

    @Test
    fun testQueryParameterValidation() {
        // Test valid parameters
        val validErrors = ApiValidationUtils.validateQueryParameters(
            limit = "50",
            offset = "0",
            search = "test"
        )
        assertTrue(ApiValidationUtils.isValid(validErrors), "Valid query parameters should pass validation")

        // Test invalid limit
        val invalidLimitErrors = ApiValidationUtils.validateQueryParameters(
            limit = "invalid"
        )
        assertFalse(ApiValidationUtils.isValid(invalidLimitErrors), "Invalid limit parameter should fail validation")

        // Test limit out of range
        val outOfRangeLimitErrors = ApiValidationUtils.validateQueryParameters(
            limit = "2000"
        )
        assertFalse(ApiValidationUtils.isValid(outOfRangeLimitErrors), "Out of range limit should fail validation")

        // Test invalid offset
        val invalidOffsetErrors = ApiValidationUtils.validateQueryParameters(
            offset = "-1"
        )
        assertFalse(ApiValidationUtils.isValid(invalidOffsetErrors), "Invalid offset parameter should fail validation")
    }

    @Test
    fun testLoginRequestValidation() {
        // Test valid login
        val validErrors = ApiValidationUtils.validateLoginRequest("user@example.com", "password123")
        assertTrue(ApiValidationUtils.isValid(validErrors), "Valid login request should pass validation")

        // Test missing username
        val missingUsernameErrors = ApiValidationUtils.validateLoginRequest(null, "password123")
        assertFalse(ApiValidationUtils.isValid(missingUsernameErrors), "Missing username should fail validation")

        // Test missing password
        val missingPasswordErrors = ApiValidationUtils.validateLoginRequest("user@example.com", null)
        assertFalse(ApiValidationUtils.isValid(missingPasswordErrors), "Missing password should fail validation")
    }

    @Test
    fun testCountryRequestValidation() {
        // Test valid country request
        val validErrors = ApiValidationUtils.validateCountryRequest("AT", "AUT", "Österreich", "Austria")
        assertTrue(ApiValidationUtils.isValid(validErrors), "Valid country request should pass validation")

        // Test missing required fields
        val missingFieldsErrors = ApiValidationUtils.validateCountryRequest(null, null, null, null)
        assertFalse(ApiValidationUtils.isValid(missingFieldsErrors), "Missing required fields should fail validation")

        // Test invalid ISO codes
        val invalidIsoErrors = ApiValidationUtils.validateCountryRequest("INVALID", "INVALID", "Test", "Test")
        assertFalse(ApiValidationUtils.isValid(invalidIsoErrors), "Invalid ISO codes should fail validation")
    }

    @Test
    fun testHorseRequestValidation() {
        // Test valid horse request
        val validErrors = ApiValidationUtils.validateHorseRequest("Thunder", "123456789", "987654321", "OEPS123", "FEI456")
        assertTrue(ApiValidationUtils.isValid(validErrors), "Valid horse request should pass validation")

        // Test missing horse name
        val missingNameErrors = ApiValidationUtils.validateHorseRequest(null, "123456789", "987654321", "OEPS123", "FEI456")
        assertFalse(ApiValidationUtils.isValid(missingNameErrors), "Missing horse name should fail validation")
    }

    @Test
    fun testEventRequestValidation() {
        val startDate = LocalDate(2024, 6, 1)
        val endDate = LocalDate(2024, 6, 3)

        // Test valid event request
        val validErrors = ApiValidationUtils.validateEventRequest("Test Event", "Vienna", startDate, endDate, 100)
        assertTrue(ApiValidationUtils.isValid(validErrors), "Valid event request should pass validation")

        // Test missing event name
        val missingNameErrors = ApiValidationUtils.validateEventRequest(null, "Vienna", startDate, endDate, 100)
        assertFalse(ApiValidationUtils.isValid(missingNameErrors), "Missing event name should fail validation")

        // Test invalid date range (end before start)
        val invalidDateErrors = ApiValidationUtils.validateEventRequest("Test Event", "Vienna", endDate, startDate, 100)
        assertFalse(ApiValidationUtils.isValid(invalidDateErrors), "Invalid date range should fail validation")
    }
}
