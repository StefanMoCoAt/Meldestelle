package at.mocode.infrastructure.gateway.routing

import at.mocode.core.domain.model.BaseDto
import kotlinx.serialization.Serializable

/**
 * Health status information for the API Gateway and its contexts.
 * This class is used to provide health status information to clients.
 */
@Serializable
data class HealthStatus(
    val status: String,
    val contexts: Map<String, String>
) : BaseDto
