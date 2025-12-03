package at.mocode.infrastructure.cache.api

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

interface CacheConfiguration {
    val defaultTtl: Duration?
    val localCacheMaxSize: Int?
    val offlineModeEnabled: Boolean
    val synchronizationInterval: Duration
    val offlineEntryMaxAge: Duration?
    val keyPrefix: String
    val compressionEnabled: Boolean
    val compressionThreshold: Int
}

data class DefaultCacheConfiguration(
    override val defaultTtl: Duration? = 1.hours,
    override val localCacheMaxSize: Int? = 10000,
    override val offlineModeEnabled: Boolean = true,
    override val synchronizationInterval: Duration = 5.minutes,
    override val offlineEntryMaxAge: Duration? = 7.days,
    override val keyPrefix: String = "",
    override val compressionEnabled: Boolean = true,
    override val compressionThreshold: Int = 1024
) : CacheConfiguration
