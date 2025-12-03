package at.mocode.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Generischer Wrapper für API-Antworten.
 */
@Serializable
data class ApiResponse<T>(
  val success: Boolean,
  val data: T? = null,
  val error: ApiError? = null
)

@Serializable
data class ApiError(
  val code: String,
  val message: String,
  val details: Map<String, String> = emptyMap()
)

/**
 * Das Ergebnis eines Repository-Aufrufs.
 * Die UI kennt nur das hier, keine HTTP-Exceptions!
 */
sealed class Resource<out T> {
  data class Success<T>(val data: T) : Resource<T>()
  data class Error(val message: String, val code: String? = null) : Resource<Nothing>()
  data object Loading : Resource<Nothing>()
}

/**
 * Datenmodell für den Ping.
 */
@Serializable
data class PingData(
  val status: String,
  val timestamp: String,
  val service: String
)

/**
 * Minimale User- und Auth-Models für Shared-Kernel (Quick-Fix für Build).
 * Hinweis: Für MP-25 können diese in :frontend:core:domain verschoben/ausgebaut werden.
 */
@Serializable
data class AuthToken(
  val accessToken: String,
  val tokenType: String = "Bearer",
  val expiresAtEpochMillis: Long? = null
)

@Serializable
data class User(
  val id: String,
  val username: String,
  val displayName: String? = null,
  val roles: List<String> = emptyList()
)
