package at.mocode.infrastructure.cache.api

import java.time.Duration

/**
 * Interface for a distributed cache that supports offline capability.
 * This cache can be used to store and retrieve data across multiple instances
 * and provides mechanisms for offline operation.
 */
interface DistributedCache {
    /**
     * Retrieves a value from the cache.
     *
     * @param key The key to retrieve
     * @return The value associated with the key, or null if not found
     */
    fun <T : Any> get(key: String, clazz: Class<T>): T?

    /**
     * Stores a value in the cache with an optional time-to-live.
     *
     * @param key The key to store the value under
     * @param value The value to store
     * @param ttl Optional time-to-live for the cache entry
     */
    fun <T : Any> set(key: String, value: T, ttl: Duration? = null)

    /**
     * Removes a value from the cache.
     *
     * @param key The key to remove
     */
    fun delete(key: String)

    /**
     * Checks if a key exists in the cache.
     *
     * @param key The key to check
     * @return true if the key exists, false otherwise
     */
    fun exists(key: String): Boolean

    /**
     * Retrieves multiple values from the cache.
     *
     * @param keys The keys to retrieve
     * @return A map of keys to values, with missing keys omitted
     */
    fun <T : Any> multiGet(keys: Collection<String>, clazz: Class<T>): Map<String, T>

    /**
     * Stores multiple values in the cache with an optional time-to-live.
     *
     * @param entries The key-value pairs to store
     * @param ttl Optional time-to-live for the cache entries
     */
    fun <T : Any> multiSet(entries: Map<String, T>, ttl: Duration? = null)

    /**
     * Removes multiple values from the cache.
     *
     * @param keys The keys to remove
     */
    fun multiDelete(keys: Collection<String>)

    /**
     * Synchronizes the local cache with the distributed cache.
     * This is used to ensure that the local cache is up-to-date with the distributed cache
     * after being offline.
     *
     * @param keys Optional collection of keys to synchronize. If null, all keys are synchronized.
     */
    fun synchronize(keys: Collection<String>? = null)

    /**
     * Marks a key as dirty, indicating that it has been modified locally
     * and needs to be synchronized with the distributed cache.
     *
     * @param key The key to mark as dirty
     */
    fun markDirty(key: String)

    /**
     * Gets all keys that have been marked as dirty.
     *
     * @return A collection of dirty keys
     */
    fun getDirtyKeys(): Collection<String>

    /**
     * Clears all entries from the cache.
     */
    fun clear()
}
