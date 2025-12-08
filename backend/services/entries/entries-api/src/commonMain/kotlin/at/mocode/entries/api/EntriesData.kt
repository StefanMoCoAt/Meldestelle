package at.mocode.entries.api

import kotlinx.serialization.Serializable

@Serializable
data class EntriesResponse(val status: String, val timestamp: String, val service: String)

@Serializable
data class EnhancedEntriesResponse(
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
