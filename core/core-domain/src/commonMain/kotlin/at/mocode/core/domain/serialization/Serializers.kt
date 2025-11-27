package at.mocode.core.domain.serialization

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Serializer für kotlin.time. Instant Objekte.
 * Konvertiert Instant zu/von ISO-8601 String-Repräsentation.
 */
@OptIn(ExperimentalTime::class)
object KotlinInstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Instant) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): Instant {
    return Instant.parse(decoder.decodeString())
  }
}

// Note: Serializer for kotlinx.datetime.Instant is defined in a separate file

/**
 * Serializer für UUID Objekte.
 * Konvertiert UUID zu/von String-Repräsentation.
 */
@OptIn(ExperimentalUuidApi::class)
object UuidSerializer : KSerializer<Uuid> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Uuid) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): Uuid {
    return Uuid.parse(decoder.decodeString())
  }
}

/**
 * Serializer für kotlinx.datetime.LocalDate Objekte.
 * Konvertiert LocalDate zu/von ISO-8601 String-Repräsentation.
 */
object LocalDateSerializer : KSerializer<LocalDate> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LocalDate) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): LocalDate {
    return LocalDate.parse(decoder.decodeString())
  }
}

/**
 * Serializer für kotlinx.datetime. LocalDateTime Objekte.
 * Konvertiert LocalDateTime zu/von ISO-8601 String-Repräsentation.
 */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LocalDateTime) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): LocalDateTime {
    return LocalDateTime.parse(decoder.decodeString())
  }
}

/**
 * Serializer für kotlinx.datetime.LocalTime Objekte.
 * Konvertiert LocalTime zu/von ISO-8601 String-Repräsentation.
 */
object LocalTimeSerializer : KSerializer<LocalTime> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LocalTime) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): LocalTime {
    return LocalTime.parse(decoder.decodeString())
  }
}
