#!/usr/bin/env kotlin

/**
 * Simple test script to verify API validation is working correctly.
 * This script tests the validation implementation added to all API endpoints.
 */

import at.mocode.validation.ApiValidationUtils
import at.mocode.validation.ValidationError

fun main() {
    println("=== API Validation Test ===")
    println()

    // Test 1: Query Parameter Validation
    println("Test 1: Query Parameter Validation")
    testQueryParameterValidation()
    println()

    // Test 2: Login Request Validation
    println("Test 2: Login Request Validation")
    testLoginRequestValidation()
    println()

    // Test 3: Country Request Validation
    println("Test 3: Country Request Validation")
    testCountryRequestValidation()
    println()

    // Test 4: Horse Request Validation
    println("Test 4: Horse Request Validation")
    testHorseRequestValidation()
    println()

    // Test 5: Event Request Validation
    println("Test 5: Event Request Validation")
    testEventRequestValidation()
    println()

    println("=== All Validation Tests Completed ===")
}

fun testQueryParameterValidation() {
    // Test valid parameters
    val validErrors = ApiValidationUtils.validateQueryParameters(
        limit = "50",
        offset = "0",
        search = "test"
    )
    println("Valid query parameters: ${if (ApiValidationUtils.isValid(validErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test invalid limit
    val invalidLimitErrors = ApiValidationUtils.validateQueryParameters(
        limit = "invalid"
    )
    println("Invalid limit parameter: ${if (!ApiValidationUtils.isValid(invalidLimitErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test limit out of range
    val outOfRangeLimitErrors = ApiValidationUtils.validateQueryParameters(
        limit = "2000"
    )
    println("Out of range limit: ${if (!ApiValidationUtils.isValid(outOfRangeLimitErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test invalid offset
    val invalidOffsetErrors = ApiValidationUtils.validateQueryParameters(
        offset = "-1"
    )
    println("Invalid offset parameter: ${if (!ApiValidationUtils.isValid(invalidOffsetErrors)) "✓ PASS" else "✗ FAIL"}")
}

fun testLoginRequestValidation() {
    // Test valid login
    val validErrors = ApiValidationUtils.validateLoginRequest("user@example.com", "password123")
    println("Valid login request: ${if (ApiValidationUtils.isValid(validErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test missing username
    val missingUsernameErrors = ApiValidationUtils.validateLoginRequest(null, "password123")
    println("Missing username: ${if (!ApiValidationUtils.isValid(missingUsernameErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test missing password
    val missingPasswordErrors = ApiValidationUtils.validateLoginRequest("user@example.com", null)
    println("Missing password: ${if (!ApiValidationUtils.isValid(missingPasswordErrors)) "✓ PASS" else "✗ FAIL"}")
}

fun testCountryRequestValidation() {
    // Test valid country request
    val validErrors = ApiValidationUtils.validateCountryRequest("AT", "AUT", "Österreich", "Austria")
    println("Valid country request: ${if (ApiValidationUtils.isValid(validErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test missing required fields
    val missingFieldsErrors = ApiValidationUtils.validateCountryRequest(null, null, null, null)
    println("Missing required fields: ${if (!ApiValidationUtils.isValid(missingFieldsErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test invalid ISO codes
    val invalidIsoErrors = ApiValidationUtils.validateCountryRequest("INVALID", "INVALID", "Test", "Test")
    println("Invalid ISO codes: ${if (!ApiValidationUtils.isValid(invalidIsoErrors)) "✓ PASS" else "✗ FAIL"}")
}

fun testHorseRequestValidation() {
    // Test valid horse request
    val validErrors = ApiValidationUtils.validateHorseRequest("Thunder", "123456789", "987654321", "OEPS123", "FEI456")
    println("Valid horse request: ${if (ApiValidationUtils.isValid(validErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test missing horse name
    val missingNameErrors = ApiValidationUtils.validateHorseRequest(null, "123456789", "987654321", "OEPS123", "FEI456")
    println("Missing horse name: ${if (!ApiValidationUtils.isValid(missingNameErrors)) "✓ PASS" else "✗ FAIL"}")
}

fun testEventRequestValidation() {
    import kotlinx.datetime.LocalDate

    val startDate = LocalDate(2024, 6, 1)
    val endDate = LocalDate(2024, 6, 3)

    // Test valid event request
    val validErrors = ApiValidationUtils.validateEventRequest("Test Event", "Vienna", startDate, endDate, 100)
    println("Valid event request: ${if (ApiValidationUtils.isValid(validErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test missing event name
    val missingNameErrors = ApiValidationUtils.validateEventRequest(null, "Vienna", startDate, endDate, 100)
    println("Missing event name: ${if (!ApiValidationUtils.isValid(missingNameErrors)) "✓ PASS" else "✗ FAIL"}")

    // Test invalid date range (end before start)
    val invalidDateErrors = ApiValidationUtils.validateEventRequest("Test Event", "Vienna", endDate, startDate, 100)
    println("Invalid date range: ${if (!ApiValidationUtils.isValid(invalidDateErrors)) "✓ PASS" else "✗ FAIL"}")
}
