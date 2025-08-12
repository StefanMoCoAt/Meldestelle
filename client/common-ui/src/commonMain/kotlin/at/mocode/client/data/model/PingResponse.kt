package at.mocode.client.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PingResponse(
    val status: String
)
