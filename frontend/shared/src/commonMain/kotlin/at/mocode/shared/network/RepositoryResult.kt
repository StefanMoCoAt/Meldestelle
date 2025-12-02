package at.mocode.shared.network

import at.mocode.shared.domain.model.ApiError

/**
 * Einheitlicher Ergebnis-Typ für Repository-/Netzwerkoperationen.
 */
sealed class RepositoryResult<out T> {
  data class Success<T>(val value: T) : RepositoryResult<T>()
  data class Error(val apiError: ApiError) : RepositoryResult<Nothing>()
}

fun <T> RepositoryResult<T>.isSuccess(): Boolean = this is RepositoryResult.Success

fun <T> RepositoryResult<T>.getErrorOrNull(): ApiError? = when (this) {
  is RepositoryResult.Success -> null
  is RepositoryResult.Error -> this.apiError
}
