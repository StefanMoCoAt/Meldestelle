package at.mocode.infrastructure.cache.redis

import at.mocode.infrastructure.cache.api.CacheEntry
import at.mocode.infrastructure.cache.api.CacheSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class JacksonCacheSerializer : CacheSerializer {
    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun <T : Any> serialize(value: T): ByteArray {
        return objectMapper.writeValueAsBytes(value)
    }

    override fun <T : Any> deserialize(bytes: ByteArray, clazz: Class<T>): T {
        return objectMapper.readValue(bytes, clazz)
    }

    override fun <T : Any> serializeEntry(entry: CacheEntry<T>): ByteArray {
        val wrapper = CacheEntryWrapper(
            key = entry.key,
            valueBytes = serialize(entry.value),
            valueType = entry.value.javaClass.name,
            createdAt = java.time.Instant.ofEpochMilli(entry.createdAt.toEpochMilliseconds()),
            expiresAt = entry.expiresAt?.toEpochMilliseconds()?.let { java.time.Instant.ofEpochMilli(it) },
            lastModifiedAt = java.time.Instant.ofEpochMilli(entry.lastModifiedAt.toEpochMilliseconds()),
            isDirty = entry.isDirty,
            isLocal = entry.isLocal
        )
        return objectMapper.writeValueAsBytes(wrapper)
    }

    override fun <T : Any> deserializeEntry(bytes: ByteArray, valueClass: Class<T>): CacheEntry<T> {
        val wrapper = objectMapper.readValue<CacheEntryWrapper>(bytes)
        val value = deserialize(wrapper.valueBytes, valueClass)
        return CacheEntry(
            key = wrapper.key,
            value = value,
            createdAt = Instant.fromEpochMilliseconds(wrapper.createdAt.toEpochMilli()),
            expiresAt = wrapper.expiresAt?.toEpochMilli()?.let { Instant.fromEpochMilliseconds(it) },
            lastModifiedAt = Instant.fromEpochMilliseconds(wrapper.lastModifiedAt.toEpochMilli()),
            isDirty = wrapper.isDirty,
            isLocal = wrapper.isLocal
        )
    }

    override fun compress(bytes: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        GZIPOutputStream(outputStream).use { it.write(bytes) }
        return outputStream.toByteArray()
    }

    override fun decompress(bytes: ByteArray): ByteArray {
        val inputStream = GZIPInputStream(ByteArrayInputStream(bytes))
        return inputStream.readBytes()
    }

    private data class CacheEntryWrapper(
        val key: String,
        val valueBytes: ByteArray,
        val valueType: String,
        val createdAt: java.time.Instant,
        val expiresAt: java.time.Instant?,
        val lastModifiedAt: java.time.Instant,
        val isDirty: Boolean,
        val isLocal: Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CacheEntryWrapper

            if (key != other.key) return false
            if (!valueBytes.contentEquals(other.valueBytes)) return false
            if (valueType != other.valueType) return false
            if (createdAt != other.createdAt) return false
            if (expiresAt != other.expiresAt) return false
            if (lastModifiedAt != other.lastModifiedAt) return false
            if (isDirty != other.isDirty) return false
            if (isLocal != other.isLocal) return false

            return true
        }

        override fun hashCode(): Int {
            var result = key.hashCode()
            result = 31 * result + valueBytes.contentHashCode()
            result = 31 * result + valueType.hashCode()
            result = 31 * result + createdAt.hashCode()
            result = 31 * result + (expiresAt?.hashCode() ?: 0)
            result = 31 * result + lastModifiedAt.hashCode()
            result = 31 * result + isDirty.hashCode()
            result = 31 * result + isLocal.hashCode()
            return result
        }
    }
}
