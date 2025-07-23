package at.mocode.infrastructure.cache.api

import java.time.Instant

/**
 * Represents an entry in the cache with metadata for offline capability.
 *
 * @param key The key of the cache entry
 * @param value The value stored in the cache
 * @param createdAt When the entry was created
 * @param expiresAt When the entry expires, or null if it doesn't expire
 * @param lastModifiedAt When the entry was last modified
 * @param isDirty Whether the entry has been modified locally and needs to be synchronized
 * @param isLocal Whether the entry is only stored locally (not yet synchronized)
 */
data class CacheEntry<T : Any>(
    val key: String,
    val value: T,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant? = null,
    val lastModifiedAt: Instant = Instant.now(),
    val isDirty: Boolean = false,
    val isLocal: Boolean = false
) {
    /**
     * Checks if the entry is expired.
     *
     * @return true if the entry is expired, false otherwise
     */
    fun isExpired(): Boolean {
        return expiresAt?.isBefore(Instant.now()) ?: false
    }

    /**
     * Creates a new entry with the isDirty flag set to true.
     *
     * @return A new CacheEntry with isDirty set to true
     */
    fun markDirty(): CacheEntry<T> {
        return copy(
            isDirty = true,
            lastModifiedAt = Instant.now()
        )
    }

    /**
     * Creates a new entry with the isDirty flag set to false.
     *
     * @return A new CacheEntry with isDirty set to false
     */
    fun markClean(): CacheEntry<T> {
        return copy(
            isDirty = false,
            isLocal = false,
            lastModifiedAt = Instant.now()
        )
    }

    /**
     * Creates a new entry with the isLocal flag set to true.
     *
     * @return A new CacheEntry with isLocal set to true
     */
    fun markLocal(): CacheEntry<T> {
        return copy(
            isLocal = true,
            lastModifiedAt = Instant.now()
        )
    }

    /**
     * Creates a new entry with an updated value.
     *
     * @param newValue The new value
     * @return A new CacheEntry with the updated value
     */
    fun updateValue(newValue: T): CacheEntry<T> {
        return copy(
            value = newValue,
            lastModifiedAt = Instant.now()
        )
    }

    /**
     * Creates a new entry with an updated expiration time.
     *
     * @param newExpiresAt The new expiration time
     * @return A new CacheEntry with the updated expiration time
     */
    fun updateExpiration(newExpiresAt: Instant?): CacheEntry<T> {
        return copy(
            expiresAt = newExpiresAt,
            lastModifiedAt = Instant.now()
        )
    }
}
