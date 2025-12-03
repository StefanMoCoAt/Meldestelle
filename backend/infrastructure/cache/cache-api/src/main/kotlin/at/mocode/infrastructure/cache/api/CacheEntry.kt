package at.mocode.infrastructure.cache.api

import kotlin.time.Clock
import kotlin.time.Instant

data class CacheEntry<T : Any>(
    val key: String,
    val value: T,
    val createdAt: Instant = Clock.System.now(),
    val expiresAt: Instant? = null,
    val lastModifiedAt: Instant = Clock.System.now(),
    val isDirty: Boolean = false,
    val isLocal: Boolean = false
) {
    fun isExpired(): Boolean {
        return expiresAt?.let { it < Clock.System.now() } ?: false
    }

    fun markDirty(): CacheEntry<T> {
        return copy(isDirty = true, lastModifiedAt = Clock.System.now())
    }

    fun markClean(): CacheEntry<T> {
        return copy(isDirty = false, isLocal = false, lastModifiedAt = Clock.System.now())
    }

    fun markLocal(): CacheEntry<T> {
        return copy(isLocal = true, lastModifiedAt = Clock.System.now())
    }

    fun updateValue(newValue: T): CacheEntry<T> {
        return copy(value = newValue, lastModifiedAt = Clock.System.now())
    }

    fun updateExpiration(newExpiresAt: Instant?): CacheEntry<T> {
        return copy(expiresAt = newExpiresAt, lastModifiedAt = Clock.System.now())
    }
}
