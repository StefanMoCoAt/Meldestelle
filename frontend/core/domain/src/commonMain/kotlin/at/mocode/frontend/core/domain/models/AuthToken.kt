package at.mocode.frontend.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthToken(
  val accessToken: String,
  val tokenType: String = "Bearer",
  val expiresAtEpochMillis: Long? = null
)
