package data.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A utility class for handling API requests
 */
class ApiRequestHandler (private val coroutineScope: CoroutineScope) {
    sealed class ApiResult<out T> {
        data class Success<out T>(val data: T) : ApiResult<T>()
        data class Error(val message: String) : ApiResult<Nothing>()
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
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
        return try {
            val response = apiCall()
            ApiResult.Success(response)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}