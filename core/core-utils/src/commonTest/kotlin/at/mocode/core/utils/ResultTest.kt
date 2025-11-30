package at.mocode.core.utils

import at.mocode.core.domain.model.ErrorCode
import at.mocode.core.domain.model.ErrorDto
import at.mocode.core.domain.model.ValidationError
import kotlin.test.*

class ResultTest {

  @Test
  fun `success and failure flags`() {
    val s = Result.success(1)
    assertTrue(s.isSuccess)
    assertFalse(s.isFailure)

    val f: Result<Int> = Result.failure(ErrorDto(ErrorCode("E"), "m"))
    assertTrue(f.isFailure)
    assertFalse(f.isSuccess)
  }

  @Test
  fun `map flatMap fold`() {
    val s = Result.success(2).map { it * 2 }
    assertEquals(4, (s as Result.Success).value)

    val f: Result<Int> = Result.failure(ErrorDto(ErrorCode("E"), "m"))
    assertTrue(f.map { it + 1 } is Result.Failure)

    val flat = Result.success(2).flatMap { Result.success(it.toString()) }
    assertEquals("2", (flat as Result.Success).value)

    val folded = flat.fold({ it.length }, { -1 })
    assertEquals(1, folded)
  }

  @Test
  fun `zip and combine`() {
    val a = Result.success(1)
    val b = Result.success("x")
    val zipped = a.zip(b)
    assertTrue(zipped is Result.Success)
    assertEquals(Pair(1, "x"), (zipped as Result.Success).value)

    val f1: Result<Int> = Result.failure(ErrorDto(ErrorCode("E1"), ""))
    val f2: Result<String> = Result.failure(ErrorDto(ErrorCode("E2"), ""))
    val z2 = f1.zip(b)
    assertTrue(z2 is Result.Failure)

    val combined = Result.combine(listOf(Result.success(1), Result.success(2)))
    assertTrue(combined is Result.Success)
    assertEquals(listOf(1, 2), (combined as Result.Success).value)

    val combinedFail =
      Result.combine(listOf(f1 as Result<Int>, Result.success(3), Result.failure(ErrorDto(ErrorCode("E3"), ""))))
    assertTrue(combinedFail is Result.Failure)
    assertEquals(2, (combinedFail as Result.Failure).errors.size)
  }

  @Test
  fun `runCatching failure conversion failureFromValidation and recovery`() {
    val ok = Result.runCatching { "ok" }
    assertTrue(ok is Result.Success)

    val iae = Result.runCatching<String> { throw IllegalArgumentException("bad") }
    assertTrue(iae is Result.Failure)
    assertEquals("INVALID_ARGUMENT", (iae as Result.Failure).errors.first().code.value)

    val generic = Result.runCatching<String> { throw Exception("x") }
    assertTrue(generic is Result.Failure)

    val verrs = listOf(ValidationError.required("name"), ValidationError.invalidFormat("email"))
    val fromVal: Result<Unit> = Result.failure(verrs)
    assertTrue(fromVal is Result.Failure)
    assertEquals("REQUIRED", (fromVal as Result.Failure).errors.first().code.value)

    val rec = Result.failure<String>(ErrorDto(ErrorCode("E"), "")).recover { _ -> "fallback" }
    assertTrue(rec is Result.Success)

    val recFail =
      Result.failure<String>(ErrorDto(ErrorCode("E"), "")).recoverCatching { _ -> throw IllegalStateException("boom") }
    assertTrue(recFail is Result.Failure)
    assertEquals("RECOVERY_FAILED", (recFail as Result.Failure).errors.first().code.value)
  }

  @Test
  fun `getOrNull default throw and toResult`() {
    val s = Result.success(5)
    assertEquals(5, s.getOrNull())

    val f: Result<Int> = Result.failure(ErrorDto(ErrorCode("E"), ""))
    assertNull(f.getOrNull())
    assertEquals(7, f.getOrDefault(7))

    assertEquals(5, s.getOrThrow())
    try {
      f.getOrThrow()
      fail("should throw")
    } catch (e: IllegalStateException) {
      // ok
    }

    val nullable: Int? = null
    val r = nullable.toResult("ist leer")
    assertTrue(r is Result.Failure)

    val r2 = 3.toResult()
    assertTrue(r2 is Result.Success)
  }
}
