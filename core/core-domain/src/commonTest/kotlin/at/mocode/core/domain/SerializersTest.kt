package at.mocode.core.domain

import at.mocode.core.domain.serialization.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

@OptIn(kotlin.time.ExperimentalTime::class)
class SerializersTest {

  @Test
  fun `Instant roundtrip`() {
    val instant = kotlin.time.Instant.parse("2024-01-01T00:00:00Z")
    val json = Json.encodeToString(KotlinInstantSerializer, instant)
    val decoded = Json.decodeFromString(KotlinInstantSerializer, json)
    assertEquals(instant, decoded)
  }

  @Test
  fun `UUID roundtrip`() {
    val uuid = Uuid.random()
    val json = Json.encodeToString(UuidSerializer, uuid)
    val decoded = Json.decodeFromString(UuidSerializer, json)
    assertEquals(uuid, decoded)
  }

  @Test
  fun `LocalDate roundtrip`() {
    val ld = LocalDate.parse("2024-06-15")
    val json = Json.encodeToString(LocalDateSerializer, ld)
    val decoded = Json.decodeFromString(LocalDateSerializer, json)
    assertEquals(ld, decoded)
  }

  @Test
  fun `LocalDateTime roundtrip`() {
    val ldt = LocalDateTime.parse("2024-06-15T12:34:56")
    val json = Json.encodeToString(LocalDateTimeSerializer, ldt)
    val decoded = Json.decodeFromString(LocalDateTimeSerializer, json)
    assertEquals(ldt, decoded)
  }

  @Test
  fun `LocalTime roundtrip`() {
    val lt = LocalTime.parse("12:34:56")
    val json = Json.encodeToString(LocalTimeSerializer, lt)
    val decoded = Json.decodeFromString(LocalTimeSerializer, json)
    assertEquals(lt, decoded)
  }
}
