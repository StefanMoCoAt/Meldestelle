package at.mocode.clients.shared.network

import at.mocode.clients.shared.domain.models.ApiError
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.io.IOException

/**
 * Custom exceptions for network operations
 */
sealed class NetworkException(
    message: String,
    cause: Throwable? = null,
    val apiError: ApiError
) : Exception(message, cause) {

    class ConnectionException(
        message: String = "Connection failed",
        cause: Throwable? = null
    ) : NetworkException(
        message = message,
        cause = cause,
        apiError = ApiError(
            code = "CONNECTION_ERROR",
            message = message,
            details = mapOf("type" to "network_connectivity")
        )
    )

    class TimeoutException(
        message: String = "Request timed out",
        cause: Throwable? = null
    ) : NetworkException(
        message = message,
        cause = cause,
        apiError = ApiError(
            code = "TIMEOUT_ERROR",
            message = message,
            details = mapOf("type" to "request_timeout")
        )
    )

    class ServerException(
        statusCode: Int,
        message: String = "Server error",
        cause: Throwable? = null
    ) : NetworkException(
        message = message,
        cause = cause,
        apiError = ApiError(
            code = "SERVER_ERROR",
            message = message,
            details = mapOf(
                "type" to "server_error",
                "status_code" to statusCode.toString()
            )
        )
    )

    class ClientException(
        statusCode: Int,
        message: String = "Client error",
        cause: Throwable? = null
    ) : NetworkException(
        message = message,
        cause = cause,
        apiError = ApiError(
            code = "CLIENT_ERROR",
            message = message,
            details = mapOf(
                "type" to "client_error",
                "status_code" to statusCode.toString()
            )
        )
    )

    class AuthenticationException(
        message: String = "Authentication failed",
        cause: Throwable? = null
    ) : NetworkException(
        message = message,
        cause = cause,
        apiError = ApiError(
            code = "AUTHENTICATION_ERROR",
            message = message,
            details = mapOf("type" to "authentication_failure")
        )
    )

    class AuthorizationException(
        message: String = "Authorization failed",
        cause: Throwable? = null
    ) : NetworkException(
        message = message,
        cause = cause,
        apiError = ApiError(
            code = "AUTHORIZATION_ERROR",
            message = message,
            details = mapOf("type" to "authorization_failure")
        )
    )

    class UnknownException(
        message: String = "Unknown error occurred",
        cause: Throwable? = null
    ) : NetworkException(
        message = message,
        cause = cause,
        apiError = ApiError(
            code = "UNKNOWN_ERROR",
            message = message,
            details = mapOf("type" to "unknown_error")
        )
    )
}

/**
 * Extension function to convert various exceptions to NetworkException
 */
fun Throwable.toNetworkException(): NetworkException {
    return when (this) {
        is ConnectTimeoutException -> NetworkException.TimeoutException(
            message = "Connection timeout: ${this.message}",
            cause = this
        )
        is SocketTimeoutException -> NetworkException.TimeoutException(
            message = "Socket timeout: ${this.message}",
            cause = this
        )
        is ResponseException -> when (this.response.status.value) {
            401 -> NetworkException.AuthenticationException(
                message = "Authentication required",
                cause = this
            )
            403 -> NetworkException.AuthorizationException(
                message = "Access forbidden",
                cause = this
            )
            in 400..499 -> NetworkException.ClientException(
                statusCode = this.response.status.value,
                message = "Client error: ${this.message}",
                cause = this
            )
            in 500..599 -> NetworkException.ServerException(
                statusCode = this.response.status.value,
                message = "Server error: ${this.message}",
                cause = this
            )
            else -> NetworkException.UnknownException(
                message = "HTTP error: ${this.message}",
                cause = this
            )
        }
        is IOException -> NetworkException.ConnectionException(
            message = "Network connection failed: ${this.message}",
            cause = this
        )
        is NetworkException -> this
        else -> NetworkException.UnknownException(
            message = "Unexpected error: ${this.message}",
            cause = this
        )
    }
}
