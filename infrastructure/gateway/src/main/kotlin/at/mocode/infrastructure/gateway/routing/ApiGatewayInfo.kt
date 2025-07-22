package at.mocode.infrastructure.gateway.routing

import at.mocode.core.domain.model.BaseDto
import kotlinx.serialization.Serializable

/**
 * Information about the API Gateway.
 * This class is used to provide information about the API Gateway to clients.
 */
@Serializable
data class ApiGatewayInfo(
    val name: String,
    val version: String,
    val description: String,
    val availableContexts: List<String>,
    val endpoints: Map<String, String>
) : BaseDto
