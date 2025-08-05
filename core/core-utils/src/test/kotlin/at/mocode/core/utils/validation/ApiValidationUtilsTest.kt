package at.mocode.core.utils.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ApiValidationUtilsTest {

    @Test
    fun `validateQueryParameters should validate limit and offset`() {
        // Test valid parameters
        var errors = ApiValidationUtils.validateQueryParameters(limit = "50", offset = "10")
        assertTrue(errors.isEmpty(), "Valid limit and offset should produce no errors")

        // Test invalid limit
        errors = ApiValidationUtils.validateQueryParameters(limit = "invalid")
        assertEquals(1, errors.size)
        assertEquals("limit", errors.first().field)

        // Test out of range limit
        errors = ApiValidationUtils.validateQueryParameters(limit = "0")
        assertEquals(1, errors.size)
        assertEquals("limit", errors.first().field)

        // Test invalid offset
        errors = ApiValidationUtils.validateQueryParameters(offset = "-1")
        assertEquals(1, errors.size)
        assertEquals("offset", errors.first().field)
    }

    @Test
    fun `validateLoginRequest should validate username and password`() {
        // Test valid request
        var errors = ApiValidationUtils.validateLoginRequest("user@example.com", "password123")
        assertTrue(errors.isEmpty())

        // Test missing username
        errors = ApiValidationUtils.validateLoginRequest(null, "password123")
        assertTrue(errors.any { it.field == "username" })

        // Test password too short
        errors = ApiValidationUtils.validateLoginRequest("user@example.com", "pass")
        assertTrue(errors.any { it.field == "password" })
    }
}
