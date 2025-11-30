package at.mocode.core.domain

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlin.time.ExperimentalTime::class)
class ApiResponseTest {

  @Test
  fun `success factory sets flags and timestamp`() {
    val res = ApiResponse.success(data = 42)
    assertTrue(res.success)
    assertEquals(42, res.data)
    assertTrue(res.errors.isEmpty())
    assertNotNull(res.timestamp)
  }

  @Test
  fun `error factory with code object`() {
    val res = ApiResponse.error<Int>(ErrorCode("INVALID_INPUT"), "Fehlerhafte Eingabe", field = "name")
    assertFalse(res.success)
    assertNull(res.data)
    assertEquals(1, res.errors.size)
    assertEquals("INVALID_INPUT", res.errors.first().code.value)
    assertEquals("Fehlerhafte Eingabe", res.errors.first().message)
    assertEquals("name", res.errors.first().field)
    assertNotNull(res.timestamp)
  }

  @Test
  fun `error factory with code string`() {
    val res = ApiResponse.error<Int>("NOT_FOUND", "Nicht gefunden")
    assertFalse(res.success)
    assertNull(res.data)
    assertEquals(1, res.errors.size)
    assertEquals("NOT_FOUND", res.errors.first().code.value)
  }

  @Test
  fun `error factory with list`() {
    val res = ApiResponse.error<Int>(listOf())
    assertFalse(res.success)
    assertNull(res.data)
    assertTrue(res.errors.isEmpty())
    assertNotNull(res.timestamp)
  }
}
