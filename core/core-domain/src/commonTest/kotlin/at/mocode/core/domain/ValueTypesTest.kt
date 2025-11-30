package at.mocode.core.domain

import at.mocode.core.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ValueTypesTest {

  @Test
  fun `EventType validation works`() {
    assertFailsWith<IllegalArgumentException> { EventType("") }
    assertFailsWith<IllegalArgumentException> { EventType("1Bad") }
    assertFailsWith<IllegalArgumentException> { EventType("bad-char!") }
    assertEquals("OrderCreated", EventType("OrderCreated").toString())
  }

  @Test
  fun `EventVersion must be non-negative and comparable`() {
    assertFailsWith<IllegalArgumentException> { EventVersion(-1) }
    assertEquals(0, EventVersion(0).compareTo(EventVersion(0)))
    assertTrue(EventVersion(2) > EventVersion(1))
  }

  @Test
  fun `ErrorCode must be uppercase with allowed characters`() {
    assertFailsWith<IllegalArgumentException> { ErrorCode("") }
    assertFailsWith<IllegalArgumentException> { ErrorCode("abc") }
    assertFailsWith<IllegalArgumentException> { ErrorCode("Bad_Code") }
    assertEquals("VALID_CODE1", ErrorCode("VALID_CODE1").toString())
  }

  @Test
  fun `PageNumber must be non-negative`() {
    assertFailsWith<IllegalArgumentException> { PageNumber(-1) }
    assertEquals("0", PageNumber(0).toString())
  }

  @Test
  fun `PageSize range is enforced`() {
    assertFailsWith<IllegalArgumentException> { PageSize(0) }
    assertFailsWith<IllegalArgumentException> { PageSize(1001) }
    assertEquals("1000", PageSize(1000).toString())
  }
}
