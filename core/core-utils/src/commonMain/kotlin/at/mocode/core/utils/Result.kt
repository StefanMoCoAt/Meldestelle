package at.mocode.core.utils

import at.mocode.core.domain.model.ErrorDto
import at.mocode.core.domain.model.ValidationError
import kotlin.jvm.JvmName

/**
 * Typsichere Result-Klasse für Fehlermanagement im gesamten System.
 * Bietet einen funktionalen Ansatz zur Fehlerbehandlung ohne Exceptions.
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with a value.
     */
    data class Success<T>(val value: T) : Result<T>()

    /**
     * Represents a failed operation with error messages.
     */
    data class Failure(val errors: List<ErrorDto>) : Result<Nothing>()

    /**
     * Checks if the Result is a success.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Checks if the Result is a failure.
     */
    val isFailure: Boolean get() = this is Failure

    /**
     * Gets the value if it's a success, otherwise null.
     *
     * @return the value if this is a Success, or null if this is a Failure
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    /**
     * Gets the value if it's a success, otherwise the default value.
     *
     * @param defaultValue the value to return if this is a Failure
     * @return the value if this is a Success, or the default value if this is a Failure
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> defaultValue
    }

    /**
     * Gets the errors if it's a failure, otherwise an empty list.
     *
     * @return the list of errors if this is a Failure, or an empty list if this is a Success
     */
    @JvmName("retrieveErrors")
    fun getErrors(): List<ErrorDto> = when (this) {
        is Success -> emptyList()
        is Failure -> errors
    }

    /**
     * Transforms the value if it's a success.
     *
     * @param transform function to apply to the success value
     * @return a new Success with the transformed value if this is a Success, or this unchanged Failure
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    /**
     * Transforms the Result flatly (for nested Results).
     * Unlike map, which wraps the transformed value in a new Success, flatMap uses the Result returned by the transform function.
     *
     * @param transform function that returns a Result
     * @return the Result returned by the transform function if this is a Success, or this unchanged Failure
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    /**
     * Executes an action if it's a success.
     *
     * @param action the function to execute with the success value
     * @return this Result, unchanged, to allow for chaining
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(value)
        return this
    }

    /**
     * Executes an action if it's a failure.
     *
     * @param action the function to execute with the list of errors
     * @return this Result, unchanged, to allow for chaining
     */
    inline fun onFailure(action: (List<ErrorDto>) -> Unit): Result<T> {
        if (this is Failure) action(errors)
        return this
    }

    /**
     * Transforms the Result by applying one of two functions depending on whether it's a success or failure.
     *
     * @param onSuccess function to apply if this is a success
     * @param onFailure function to apply if this is a failure
     * @return the result of applying the appropriate function
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (List<ErrorDto>) -> R
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(errors)
    }

    /**
     * Attempts to recover from a failure by applying the specified function to the error list.
     * If this is already a success, it is returned unchanged.
     *
     * @param transform function to apply to the error list to recover
     * @return a new Success if recovery was successful, or this unchanged Result if already a success
     */
    inline fun recover(transform: (List<ErrorDto>) -> @UnsafeVariance T): Result<T> = when (this) {
        is Success -> this
        is Failure -> Success(transform(errors))
    }

    /**
     * Attempts to recover from a failure by applying the specified function to the error list.
     * If this is already a success, it is returned unchanged.
     * If an exception occurs during recovery, it is converted to a new failure.
     *
     * @param transform function to apply to the error list to recover
     * @return a new Success if recovery was successful, a new Failure if recovery threw an exception,
     *         or this unchanged Result if already a success
     */
    inline fun recoverCatching(transform: (List<ErrorDto>) -> @UnsafeVariance T): Result<T> = when (this) {
        is Success -> this
        is Failure -> try {
            Success(transform(errors))
        } catch (e: Exception) {
            Failure(listOf(ErrorDto(
                code = at.mocode.core.domain.model.ErrorCode("RECOVERY_FAILED"),
                message = e.message ?: "Recovery failed with an unknown error"
            )))
        }
    }

    /**
     * Combines this Result with another Result, creating a pair of their values if both are successful.
     * If either Result is a failure, the combined Result will be a failure containing all errors.
     *
     * @param other the Result to combine with this one
     * @return a Result containing a Pair of values if both are successful, or a Failure with all errors
     */
    fun <R> zip(other: Result<R>): Result<Pair<T, R>> = when {
        this is Success && other is Success -> Success(Pair(this.value, other.value))
        this is Success && other is Failure -> Failure(other.errors)
        this is Failure && other is Success -> Failure(this.errors)
        this is Failure && other is Failure -> {
            val allErrors = this.errors + other.errors
            Failure(allErrors)
        }
        // This branch should never be reached due to sealed class, but included for completeness
        else -> throw IllegalStateException("Unreachable code - Result should be either Success or Failure")
    }

    /**
     * Safely attempts to get the value, throwing a custom exception if this is a failure.
     *
     * @param errorHandler function that converts the list of errors to an exception
     * @return the value if this is a Success
     * @throws E if this is a Failure, as created by the errorHandler
     */
    inline fun <E : Throwable> getOrThrow(errorHandler: (List<ErrorDto>) -> E): T = when (this) {
        is Success -> value
        is Failure -> throw errorHandler(errors)
    }

    /**
     * Gets the value if it's a success, or throws an IllegalStateException with a message constructed from the errors.
     *
     * @return the value if this is a Success
     * @throws IllegalStateException if this is a Failure, with a message containing the error details
     */
    fun getOrThrow(): T = getOrThrow { errors ->
        IllegalStateException("Result is a Failure with errors: ${errors.joinToString { it.message }}")
    }

    companion object {
        /**
         * Creates a successful Result.
         *
         * @param value the value to wrap in a Success
         * @return a new Success containing the provided value
         */
        fun <T> success(value: T): Result<T> = Success(value)

        /**
         * Creates a failure Result with a single error.
         *
         * @param error the error to include in the Failure
         * @return a new Failure containing the provided error
         */
        fun <T> failure(error: ErrorDto): Result<T> = Failure(listOf(error))

        /**
         * Creates a failure Result with multiple errors.
         *
         * @param errors the list of errors to include in the Failure
         * @return a new Failure containing the provided errors
         */
        fun <T> failure(errors: List<ErrorDto>): Result<T> = Failure(errors)

        /**
         * Creates a failure Result from ValidationErrors.
         * Converts the ValidationErrors to ErrorDtos internally.
         *
         * @param validationErrors the list of validation errors to convert and include in the Failure
         * @return a new Failure containing ErrorDtos converted from the provided ValidationErrors
         */
        @JvmName("failureFromValidationErrors")
        fun <T> failure(validationErrors: List<ValidationError>): Result<T> =
            Failure(validationErrors.toErrorDtos())

        /**
         * Executes an operation that returns a Result and catches exceptions.
         * Provides more specific error codes based on the type of exception caught.
         *
         * @param operation the operation to execute
         * @return a Success with the operation result, or a Failure with error details if an exception occurred
         */
        inline fun <T> runCatching(operation: () -> T): Result<T> = try {
            success(operation())
        } catch (e: IllegalArgumentException) {
            failure(ErrorDto(
                code = at.mocode.core.domain.model.ErrorCode("INVALID_ARGUMENT"),
                message = e.message ?: "Invalid argument provided"
            ))
        } catch (e: IllegalStateException) {
            failure(ErrorDto(
                code = at.mocode.core.domain.model.ErrorCode("INVALID_STATE"),
                message = e.message ?: "Operation called in invalid state"
            ))
        } catch (e: UnsupportedOperationException) {
            failure(ErrorDto(
                code = at.mocode.core.domain.model.ErrorCode("UNSUPPORTED_OPERATION"),
                message = e.message ?: "Operation not supported"
            ))
        } catch (e: IndexOutOfBoundsException) {
            failure(ErrorDto(
                code = at.mocode.core.domain.model.ErrorCode("INDEX_OUT_OF_BOUNDS"),
                message = e.message ?: "Index out of bounds"
            ))
        } catch (e: NullPointerException) {
            failure(ErrorDto(
                code = at.mocode.core.domain.model.ErrorCode("NULL_REFERENCE"),
                message = e.message ?: "Unexpected null reference"
            ))
        } catch (e: ClassCastException) {
            failure(ErrorDto(
                code = at.mocode.core.domain.model.ErrorCode("TYPE_MISMATCH"),
                message = e.message ?: "Type mismatch occurred"
            ))
        } catch (e: Exception) {
            // Fallback for any other exception type
            failure(ErrorDto(
                code = at.mocode.core.domain.model.ErrorCode("OPERATION_FAILED"),
                message = e.message ?: "Unknown error occurred"
            ))
        }

        /**
         * Combines multiple Results into a single Result with a list.
         * Optimized for performance with large collections.
         *
         * @param results a list of Results to combine
         * @return a Success containing a list of all success values if all Results are successful,
         *         or a Failure containing all error messages if any Results are failures
         */
        fun <T> combine(results: List<Result<T>>): Result<List<T>> {
            // Fast path for empty list
            if (results.isEmpty()) {
                return success(emptyList())
            }

            // Fast path for single result
            if (results.size == 1) {
                return results.first().map { listOf(it) }
            }

            // Check if there are any failures
            val anyFailure = results.any { it.isFailure }

            // If no failures, we can optimize by directly mapping to values
            if (!anyFailure) {
                return success(results.map { (it as Success).value })
            }

            // If there are failures, collect all errors
            val errors = results
                .filterIsInstance<Failure>()
                .flatMap { it.errors }

            // If empty results list contained no failures, return empty success
            if (errors.isEmpty()) {
                return success(emptyList())
            }

            return failure(errors)
        }
    }
}

/**
 * Extension function to convert nullable values to Results.
 * This is useful for handling nullable values in a functional way.
 *
 * @param errorMessage custom error message to use when the value is null
 * @return a Success containing the non-null value, or a Failure if the value is null
 */
fun <T> T?.toResult(errorMessage: String = "Value is null"): Result<T> =
    if (this != null) {
        Result.success(this)
    } else {
        Result.failure(ErrorDto(
            code = at.mocode.core.domain.model.ErrorCode("NULL_VALUE"),
            message = errorMessage
        ))
    }
