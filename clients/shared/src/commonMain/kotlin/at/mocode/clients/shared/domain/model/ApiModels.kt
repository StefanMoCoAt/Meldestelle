package at.mocode.clients.shared.domain.model

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
  val message: String
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
