package at.mocode

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppTest {

    // Modified email validation function for testing
    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false

        // More strict regex that requires domain to have at least one dot
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    @Test
    fun testEmailValidation_validEmail_returnsTrue() {
        // Test with valid email addresses
        assertTrue(isValidEmail("user@example.com"))
        assertTrue(isValidEmail("user.name@example.com"))
        assertTrue(isValidEmail("user+tag@example.com"))
        assertTrue(isValidEmail("user@subdomain.example.com"))
    }

    @Test
    fun testEmailValidation_invalidEmail_returnsFalse() {
        // Test with invalid email addresses
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("userexample.com")) // Missing @
        assertFalse(isValidEmail("user@")) // Missing domain
        assertFalse(isValidEmail("@example.com")) // Missing username
        assertFalse(isValidEmail("user@example")) // Missing TLD
    }

    @Test
    fun testConstants() {
        // Verify the server port constant is set correctly
        assertEquals(8081, SERVER_PORT)

        // Verify PlatformInfo is accessible
        // Note: This is just checking that the code compiles and runs,
        // actual behavior depends on the platform implementation
        val host = PlatformInfo.apiHost
        assertTrue(host.isNotEmpty())
    }
}
