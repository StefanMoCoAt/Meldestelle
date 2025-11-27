package at.mocode.core.domain.serialization

import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Kotlinx Serialization serializer for kotlinx.datetime.LocalDate.
 * Serializes as ISO-8601 date string (yyyy-MM-dd).
 */
object KotlinLocalDateSerializer : KSerializer<LocalDate> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("KotlinLocalDate", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LocalDate) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): LocalDate {
    val text = decoder.decodeString()
    return try {
      LocalDate.parse(text)
    } catch (e: Exception) {
      throw SerializationException("Invalid LocalDate format: '$text'", e)
    }
  }
}
