package at.mocode.ping.api

import at.mocode.core.sync.Syncable
import kotlinx.serialization.Serializable

@Serializable
data class PingResponse(val status: String, val timestamp: String, val service: String)

@Serializable
data class EnhancedPingResponse(
    val status: String,
    val timestamp: String,
    val service: String,
    val circuitBreakerState: String,
    val responseTime: Long
)

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val service: String,
    val healthy: Boolean
)

/**
 * Sync-Contract: Ping Event für Delta-Sync.
 */
@Serializable
data class PingEvent(
  // Using a String for the ID to be compatible with UUIDs from the backend.
  override val id: String,
  val message: String,
  // Using a Long for the timestamp, which can be derived from a UUIDv7.
  override val lastModified: Long
) : Syncable
