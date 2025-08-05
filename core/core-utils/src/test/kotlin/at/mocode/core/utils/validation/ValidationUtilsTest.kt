package at.mocode.core.utils.validation

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidationUtilsTest {

    @Test
    fun `validateNotBlank should return error for blank strings`() {
        assertNotNull(ValidationUtils.validateNotBlank(null, "testField"))
        assertNotNull(ValidationUtils.validateNotBlank("", "testField"))
        assertNotNull(ValidationUtils.validateNotBlank("   ", "testField"))
    }

    @Test
    fun `validateNotBlank should return null for non-blank strings`() {
        assertNull(ValidationUtils.validateNotBlank("value", "testField"))
    }

    @Test
    fun `validateLength should check min and max length`() {
        assertNotNull(ValidationUtils.validateLength("a", "testField", 5, 2), "Should fail for being too short")
        assertNotNull(ValidationUtils.validateLength("abcdef", "testField", 5, 2), "Should fail for being too long")
        assertNull(ValidationUtils.validateLength("abc", "testField", 5, 2), "Should pass with valid length")
    }

    @Test
    fun `validateEmail should validate email format`() {
        assertNull(ValidationUtils.validateEmail("test@example.com", "email"))
        assertNotNull(ValidationUtils.validateEmail("test@", "email"))
        assertNotNull(ValidationUtils.validateEmail("test@example", "email"))
        assertNotNull(ValidationUtils.validateEmail("test.example.com", "email"))
    }
}
