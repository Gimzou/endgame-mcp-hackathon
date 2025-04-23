package data.network.taostats

import data.model.taostats.PaginatedResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import data.model.taostats.subnet.SubnetIdentity
import data.util.ApiException
import data.util.ApiRequestHandler
import data.util.ApiRequestHandler.ApiResult
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

class TaostatsApiImpl(
    private val httpClient: HttpClient,
    private val apiRequestHandler: ApiRequestHandler,
    private val baseUrl: String = "https://api.taostats.io",
    private val apiToken: String,
    private val apiVersion: String = "v1"
) : TaostatsApi {

    override suspend fun getSubnetIdentity(page: Int, perPage: Int): ApiResult<PaginatedResponse<SubnetIdentity>> {
        return apiRequestHandler.safeApiCall (
            apiCall = {
                val response = httpClient.get("$baseUrl/api/subnet/identity/$apiVersion") {
                    // request headers
                    header("Accept", "application/json")
                    header("Authorization", apiToken)
                    // url parameters
                    parameter("page", page)
                    parameter("limit", perPage)
                }

                when (response.status.value) {
                    in 200..299 -> response.body<PaginatedResponse<SubnetIdentity>>()
                    400 -> throw ApiException.BadRequestException("Invalid parameters provided")
                    401 -> throw ApiException.UnauthorizedException("Authentication required")
                    403 -> throw ApiException.ForbiddenException("Access denied")
                    404 -> throw ApiException.NotFoundException("Resource not found")
                    in 500..599 -> throw ApiException.ServerException("Server error occurred")
                    else -> throw ApiException.UnknownException("Unknown error occurred")
                }
            },
            errorHandler = { e ->
                when (e) {
                    is ApiException -> e
                    is IOException -> ApiException.NetworkException("Network error occurred", e)
                    is SerializationException -> ApiException.ParseException("Serialization error occurred", e)
                    else -> ApiException.UnknownException("Unknown error occurred", e)

                }
            }
        )
    }
}