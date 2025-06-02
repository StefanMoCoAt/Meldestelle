package at.mocode.model

import kotlinx.serialization.Serializable

/**
 * Standard API response format for all endpoints.
 * This class ensures consistent response structure and proper serialization.
 */
@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val emailSent: Boolean? = null
)
