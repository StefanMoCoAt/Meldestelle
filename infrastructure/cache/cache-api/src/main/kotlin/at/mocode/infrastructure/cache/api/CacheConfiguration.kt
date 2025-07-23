package at.mocode.infrastructure.cache.api

import java.time.Duration

/**
 * Configuration for the distributed cache.
 */
interface CacheConfiguration {
    /**
     * Default time-to-live for cache entries.
     * If null, entries do not expire by default.
     */
    val defaultTtl: Duration?

    /**
     * Maximum number of entries to store in the local cache.
     * If null, there is no limit.
     */
    val localCacheMaxSize: Int?

    /**
     * Whether to enable offline mode.
     * If true, the cache will store entries locally when offline
     * and synchronize them when online.
     */
    val offlineModeEnabled: Boolean

    /**
     * How often to attempt synchronization in offline mode.
     */
    val synchronizationInterval: Duration

    /**
     * Maximum age of entries to keep in the local cache when offline.
     * If null, entries do not expire when offline.
     */
    val offlineEntryMaxAge: Duration?

    /**
     * Prefix to add to all cache keys.
     * This can be used to namespace cache entries.
     */
    val keyPrefix: String

    /**
     * Whether to compress cache entries.
     */
    val compressionEnabled: Boolean

    /**
     * Threshold in bytes above which to compress cache entries.
     * Only used if compressionEnabled is true.
     */
    val compressionThreshold: Int
}

/**
 * Default implementation of CacheConfiguration.
 */
data class DefaultCacheConfiguration(
    override val defaultTtl: Duration? = Duration.ofHours(1),
    override val localCacheMaxSize: Int? = 10000,
    override val offlineModeEnabled: Boolean = true,
    override val synchronizationInterval: Duration = Duration.ofMinutes(5),
    override val offlineEntryMaxAge: Duration? = Duration.ofDays(7),
    override val keyPrefix: String = "",
    override val compressionEnabled: Boolean = true,
    override val compressionThreshold: Int = 1024
) : CacheConfiguration
