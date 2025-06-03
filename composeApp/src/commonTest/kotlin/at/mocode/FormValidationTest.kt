package at.mocode

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormValidationTest {

    // Test the form validation logic that's used in App.kt

    @Test
    fun testRiderNameValidation() {
        // Valid rider names
        assertTrue(isFieldValid("Max Mustermann"))
        assertTrue(isFieldValid("Anna-Maria Schmidt"))
        assertTrue(isFieldValid("John Doe Jr."))

        // Invalid rider names
        assertFalse(isFieldValid(""))
        assertFalse(isFieldValid("   "))
    }

    @Test
    fun testHorseNameValidation() {
        // Valid horse names
        assertTrue(isFieldValid("Blitz"))
        assertTrue(isFieldValid("Star Light 123"))
        assertTrue(isFieldValid("AT-12345"))

        // Invalid horse names
        assertFalse(isFieldValid(""))
        assertFalse(isFieldValid("   "))
    }

    @Test
    fun testContactValidation() {
        // Valid contact information (at least one of email or phone is provided)
        assertTrue(isContactValid("user@example.com", ""))
        assertTrue(isContactValid("", "1234567890"))
        assertTrue(isContactValid("user@example.com", "1234567890"))

        // Invalid contact information (neither email nor phone is provided)
        assertFalse(isContactValid("", ""))
    }

    @Test
    fun testEventSelectionValidation() {
        // Valid event selections (at least one event is selected)
        assertTrue(isEventsValid(listOf("Event 1")))
        assertTrue(isEventsValid(listOf("Event 1", "Event 2")))
        assertTrue(isEventsValid(listOf("1 Pony Stilspringprüfung 60 cm", "2 Stilspringprüfung 60 cm")))

        // Invalid event selections (no events selected)
        assertFalse(isEventsValid(emptyList()))
    }

    @Test
    fun testFormValidation() {
        // Valid form (all required fields are filled correctly)
        assertTrue(isFormValid(
            riderName = "Max Mustermann",
            horseName = "Blitz",
            email = "max@example.com",
            phone = "",
            selectedEvents = listOf("Event 1")
        ))

        assertTrue(isFormValid(
            riderName = "Anna Schmidt",
            horseName = "Star",
            email = "",
            phone = "1234567890",
            selectedEvents = listOf("Event 1", "Event 2")
        ))

        // Invalid form (missing rider name)
        assertFalse(isFormValid(
            riderName = "",
            horseName = "Blitz",
            email = "max@example.com",
            phone = "",
            selectedEvents = listOf("Event 1")
        ))

        // Invalid form (missing horse name)
        assertFalse(isFormValid(
            riderName = "Max Mustermann",
            horseName = "",
            email = "max@example.com",
            phone = "",
            selectedEvents = listOf("Event 1")
        ))

        // Invalid form (missing contact information)
        assertFalse(isFormValid(
            riderName = "Max Mustermann",
            horseName = "Blitz",
            email = "",
            phone = "",
            selectedEvents = listOf("Event 1")
        ))

        // Invalid form (no events selected)
        assertFalse(isFormValid(
            riderName = "Max Mustermann",
            horseName = "Blitz",
            email = "max@example.com",
            phone = "",
            selectedEvents = emptyList()
        ))

        // Invalid form (invalid email)
        assertFalse(isFormValid(
            riderName = "Max Mustermann",
            horseName = "Blitz",
            email = "invalid-email",
            phone = "",
            selectedEvents = listOf("Event 1")
        ))
    }

    // Helper functions that mimic the validation logic in App.kt

    private fun isFieldValid(value: String): Boolean {
        return value.isNotBlank()
    }

    private fun isContactValid(email: String, phone: String): Boolean {
        return email.isNotBlank() || phone.isNotBlank()
    }

    private fun isEventsValid(events: List<String>): Boolean {
        return events.isNotEmpty()
    }

    // Copy of the email validation function to avoid dependencies on App.kt
    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false

        // Stricter regex that requires domain to have at least one dot
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    private fun isFormValid(
        riderName: String,
        horseName: String,
        email: String,
        phone: String,
        selectedEvents: List<String>
    ): Boolean {
        val isRiderNameValid = isFieldValid(riderName)
        val isHorseNameValid = isFieldValid(horseName)
        val isEmailValid = email.isBlank() || isValidEmail(email)
        val isContactValid = isContactValid(email, phone)
        val isEventsValid = isEventsValid(selectedEvents)

        return isRiderNameValid && isHorseNameValid && isContactValid && isEventsValid && isEmailValid
    }
}
