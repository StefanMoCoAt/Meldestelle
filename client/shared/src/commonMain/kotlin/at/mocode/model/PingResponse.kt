package at.mocode.model

import kotlinx.serialization.Serializable

@Serializable
data class PingResponse(
    val status: String,
    val timestamp: String,
    val service: String
)

@Serializable
data class EnhancedPingResponse(
    val status: String,
    val timestamp: String,
    val service: String,
    val circuitBreakerState: String? = null,
    val responseTime: Long? = null
)

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val service: String,
    val healthy: Boolean
)
