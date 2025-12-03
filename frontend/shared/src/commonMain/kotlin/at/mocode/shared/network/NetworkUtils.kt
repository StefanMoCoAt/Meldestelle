package at.mocode.shared.network


import at.mocode.shared.domain.model.ApiError
import kotlinx.coroutines.delay

// Using platform-agnostic timestamp handling

/**
 * Simple timestamp provider for multiplatform compatibility
 */
expect fun currentTimeMillis(): Long

/**
 * Network utilities for handling retry logic and resilience
 */
object NetworkUtils {

  /**
   * Retry configuration for network operations
   */
  data class RetryConfig(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 1000L,
    val maxDelayMs: Long = 10000L,
    val backoffMultiplier: Double = 2.0,
    val retryableExceptions: Set<String> = setOf(
      "CONNECTION_ERROR",
      "TIMEOUT_ERROR",
      "SERVER_ERROR"
    )
  )

  /**
   * Execute operation with retry logic
   */
  suspend fun <T> withRetry(
    config: RetryConfig = RetryConfig(),
    operation: suspend () -> RepositoryResult<T>
  ): RepositoryResult<T> {
    var lastError: ApiError? = null
    var currentDelay = config.initialDelayMs

    repeat(config.maxAttempts) { attempt ->
      try {
        val result = operation()

        // Return success immediately
        if (result.isSuccess()) {
          return result
        }

        // Check if the error is retryable
        val error = result.getErrorOrNull()
        if (error != null && shouldRetry(error, config)) {
          lastError = error

          // Don't delay on the last attempt
          if (attempt < config.maxAttempts - 1) {
            delay(currentDelay)
            currentDelay = minOf(
              (currentDelay * config.backoffMultiplier).toLong(),
              config.maxDelayMs
            )
          }
        } else {
          // Non-retryable error, return immediately
          return result
        }
      } catch (e: Exception) {
        val networkException = e.toNetworkException()
        lastError = networkException.apiError

        if (shouldRetry(networkException.apiError, config)) {
          if (attempt < config.maxAttempts - 1) {
            delay(currentDelay)
            currentDelay = minOf(
              (currentDelay * config.backoffMultiplier).toLong(),
              config.maxDelayMs
            )
          }
        } else {
          return RepositoryResult.Error(networkException.apiError)
        }
      }
    }

    // All attempts exhausted, return last error
    return RepositoryResult.Error(
      lastError ?: ApiError(
        code = "MAX_RETRIES_EXCEEDED",
        message = "Maximum retry attempts exceeded"
      )
    )
  }

  /**
   * Check if an error should trigger a retry
   */
  private fun shouldRetry(error: ApiError, config: RetryConfig): Boolean {
    return config.retryableExceptions.contains(error.code)
  }

  /**
   * Network connectivity checker (simplified for shared module)
   */
  object ConnectivityChecker {
    private var isOnline: Boolean = true
    private var lastCheckMillis: Long = 0L

    fun setOnlineStatus(online: Boolean) {
      isOnline = online
      lastCheckMillis = currentTimeMillis()
    }

    fun isOnline(): Boolean = isOnline

    fun getLastCheckMillis(): Long = lastCheckMillis

    /**
     * Simple connectivity test by attempting a lightweight operation
     */
    suspend fun checkConnectivity(testOperation: suspend () -> Boolean): Boolean {
      return try {
        val result = testOperation()
        setOnlineStatus(result)
        result
      } catch (_: Exception) {
        setOnlineStatus(false)
        false
      }
    }
  }

  /**
   * Circuit breaker pattern for network operations
   */
  class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val recoveryTimeoutMs: Long = 60000L,
    private val successThreshold: Int = 3
  ) {
    private enum class State { CLOSED, OPEN, HALF_OPEN }

    private var state = State.CLOSED
    private var failureCount = 0
    private var successCount = 0
    private var lastFailureTime = 0L

    suspend fun <T> execute(operation: suspend () -> RepositoryResult<T>): RepositoryResult<T> {
      when (state) {
        State.OPEN -> {
          if (currentTimeMillis() - lastFailureTime >= recoveryTimeoutMs) {
            state = State.HALF_OPEN
            successCount = 0
          } else {
            return RepositoryResult.Error(
              ApiError(
                code = "CIRCUIT_BREAKER_OPEN",
                message = "Circuit breaker is open, requests blocked"
              )
            )
          }
        }

        State.HALF_OPEN -> {
          // Allow limited requests to test recovery
        }

        State.CLOSED -> {
          // Normal operation
        }
      }

      return try {
        val result = operation()

        if (result.isSuccess()) {
          onSuccess()
        } else {
          onFailure()
        }

        result
      } catch (e: Exception) {
        onFailure()
        val networkException = e.toNetworkException()
        RepositoryResult.Error(networkException.apiError)
      }
    }

    private fun onSuccess() {
      failureCount = 0

      when (state) {
        State.HALF_OPEN -> {
          successCount++
          if (successCount >= successThreshold) {
            state = State.CLOSED
          }
        }

        else -> {
          state = State.CLOSED
        }
      }
    }

    private fun onFailure() {
      failureCount++
      lastFailureTime = currentTimeMillis()

      if (failureCount >= failureThreshold) {
        state = State.OPEN
      }
    }

    fun getState(): String = state.name
    fun getFailureCount(): Int = failureCount
  }
}
