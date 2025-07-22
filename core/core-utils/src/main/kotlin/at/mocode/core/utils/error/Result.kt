package at.mocode.core.utils.error

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [T]
 * or a failure with an arbitrary [Throwable] exception.
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with the given [data] value.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with the given [exception] that caused it to fail.
     */
    data class Error(val exception: Throwable) : Result<Nothing>()

    /**
     * Returns `true` if this instance represents a successful outcome.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Returns `true` if this instance represents a failed outcome.
     */
    fun isError(): Boolean = this is Error

    /**
     * Returns the encapsulated value if this instance represents [Success] or `null` if it is [Error].
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the encapsulated value if this instance represents [Success] or throws the encapsulated [exception] if it is [Error].
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }

    companion object {
        /**
         * Creates a [Result.Success] instance with the given [data] value.
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Creates a [Result.Error] instance with the given [exception].
         */
        fun error(exception: Throwable): Result<Nothing> = Error(exception)
    }
}

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
inline fun <T> runCatching(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.error(e)
    }
}
