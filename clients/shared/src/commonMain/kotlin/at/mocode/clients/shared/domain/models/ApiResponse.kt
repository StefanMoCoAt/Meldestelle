package at.mocode.clients.shared.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val timestamp: String,
    val correlationId: String? = null
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap()
)

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val service: String,
    val healthy: Boolean
)
