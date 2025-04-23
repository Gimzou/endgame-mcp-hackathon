package data.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A utility class for handling API requests
 */
class ApiRequestHandler (private val coroutineScope: CoroutineScope) {
    sealed class ApiResult<out T> {
        data class Success<out T>(val data: T) : ApiResult<T>()
        data class Error(
            val message: String,
            val exception: ApiException? = null
        ) : ApiResult<Nothing>()
    }

    // execute a request and handle the response
    fun <T> execute(
        request: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val response = request()
                onSuccess(response)
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    // use a suspending function and expect an ApiResult in return
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T,
        errorHandler: (Exception) -> ApiException = { e -> ApiException.UnknownException("Unknown error occurred", e)}
    ): ApiResult<T> {
        return try {
            val response = apiCall()
            ApiResult.Success(response)
        } catch (e: Exception) {
            val apiException = when (e) {
                is ApiException -> e
                else -> errorHandler(e)
            }
            ApiResult.Error(apiException.message ?: "Unknown error occurred", apiException)
        }
    }
}

/**
 * A custom exception class for handling API errors
 */
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class BadRequestException(message: String) : ApiException(message)
    class UnauthorizedException(message: String) : ApiException(message)
    class ForbiddenException(message: String) : ApiException(message)
    class NotFoundException(message: String) : ApiException(message)
    class ServerException(message: String) : ApiException(message)
    class NetworkException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class ParseException(message: String, cause: Throwable? = null) : ApiException(message, cause)
    class UnknownException(message: String, cause: Throwable? = null) : ApiException(message, cause)
}