package at.mocode.clients.shared.data.repository

import at.mocode.clients.shared.domain.models.ApiResponse
import at.mocode.clients.shared.domain.models.ApiError

/**
 * Base repository interface defining common operations and patterns
 * for data access across the application.
 */
interface Repository

/**
 * Result wrapper for repository operations to handle success/error states
 */
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val error: ApiError) : RepositoryResult<Nothing>()
    data class Loading(val message: String = "Loading...") : RepositoryResult<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getErrorOrNull(): ApiError? = when (this) {
        is Error -> error
        else -> null
    }
}

/**
 * Extension function to convert ApiResponse to RepositoryResult
 */
fun <T> ApiResponse<T>.toRepositoryResult(): RepositoryResult<T> {
    return if (success && data != null) {
        RepositoryResult.Success(data)
    } else {
        RepositoryResult.Error(
            error ?: ApiError(
                code = "UNKNOWN_ERROR",
                message = "Unknown error occurred"
            )
        )
    }
}

/**
 * Extension function to handle repository results with callbacks
 */
inline fun <T> RepositoryResult<T>.onSuccess(action: (T) -> Unit): RepositoryResult<T> {
    if (this is RepositoryResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> RepositoryResult<T>.onError(action: (ApiError) -> Unit): RepositoryResult<T> {
    if (this is RepositoryResult.Error) {
        action(error)
    }
    return this
}

inline fun <T> RepositoryResult<T>.onLoading(action: (String) -> Unit): RepositoryResult<T> {
    if (this is RepositoryResult.Loading) {
        action(message)
    }
    return this
}
