package at.mocode.client.common.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe LRU cache implementation for API responses.
 * Provides TTL-based expiration and size-based eviction.
 */
class ApiCache(
    private val maxSize: Int,
    private val ttlMs: Long
) {
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val accessOrder = ConcurrentLinkedQueue<String>()
    private val lock = ReentrantReadWriteLock()

    data class CacheEntry(
        val data: Any,
        val timestamp: Long
    )

    /**
     * Retrieves a cached value if it exists and hasn't expired.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        return lock.read {
            val entry = cache[key] ?: return null

            // Check if expired
            if (System.currentTimeMillis() - entry.timestamp > ttlMs) {
                // Remove expired entry
                lock.write {
                    cache.remove(key)
                    accessOrder.remove(key)
                }
                return null
            }

            // Update access order
            lock.write {
                accessOrder.remove(key)
                accessOrder.offer(key)
            }

            entry.data as T
        }
    }

    /**
     * Stores a value in the cache with current timestamp.
     */
    fun put(key: String, value: Any) {
        lock.write {
            // Remove if already exists
            if (cache.containsKey(key)) {
                accessOrder.remove(key)
            }

            // Add new entry
            cache[key] = CacheEntry(value, System.currentTimeMillis())
            accessOrder.offer(key)

            // Evict oldest entries if over capacity
            while (cache.size > maxSize) {
                val oldestKey = accessOrder.poll()
                if (oldestKey != null) {
                    cache.remove(oldestKey)
                }
            }
        }
    }

    /**
     * Removes a specific entry from the cache.
     */
    fun remove(key: String) {
        lock.write {
            cache.remove(key)
            accessOrder.remove(key)
        }
    }

    /**
     * Removes entries matching the given pattern.
     * Useful for invalidating related cache entries.
     */
    fun removePattern(pattern: String) {
        lock.write {
            val keysToRemove = cache.keys.filter { it.contains(pattern) }
            keysToRemove.forEach { key ->
                cache.remove(key)
                accessOrder.remove(key)
            }
        }
    }

    /**
     * Clears all cached entries.
     */
    fun clear() {
        lock.write {
            cache.clear()
            accessOrder.clear()
        }
    }

    /**
     * Removes all expired entries from the cache.
     */
    fun cleanupExpired() {
        val currentTime = System.currentTimeMillis()
        lock.write {
            val expiredKeys = cache.entries
                .filter { currentTime - it.value.timestamp > ttlMs }
                .map { it.key }

            expiredKeys.forEach { key ->
                cache.remove(key)
                accessOrder.remove(key)
            }
        }
    }

    /**
     * Returns current cache statistics.
     */
    fun getStats(): CacheStats {
        return lock.read {
            CacheStats(
                size = cache.size,
                maxSize = maxSize,
                ttlMs = ttlMs
            )
        }
    }

    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val ttlMs: Long
    )
}
