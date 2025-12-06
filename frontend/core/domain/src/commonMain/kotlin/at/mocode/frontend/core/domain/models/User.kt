package at.mocode.frontend.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
  val id: String,
  val username: String,
  val displayName: String? = null,
  val roles: List<String> = emptyList()
)
