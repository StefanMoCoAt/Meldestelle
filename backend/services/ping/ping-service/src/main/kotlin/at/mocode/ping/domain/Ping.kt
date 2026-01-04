package at.mocode.ping.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import java.time.Instant

/**
 * Domain Entity für einen Ping.
 * Unabhängig von Frameworks (Pure Kotlin).
 */
@OptIn(ExperimentalUuidApi::class)
data class Ping(
    val id: Uuid = Uuid.generateV7(), // Kotlin 2.3.0 UUID v7
    val message: String,
    val timestamp: Instant = Instant.now()
)
