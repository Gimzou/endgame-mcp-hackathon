package data.network.github

import data.model.github.repositories.GitHubRepositoryContent
import data.util.ApiException
import data.util.ApiRequestHandler
import data.util.ApiRequestHandler.ApiResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

class GitHubApiImpl(
    private val httpClient: HttpClient,
    private val apiRequestHandler: ApiRequestHandler,
    private val baseUrl: String = "https://api.github.com",
    private val apiToken: String,
    private val apiVersion: String = "2022-11-28"
) : GitHubApi {

    override suspend fun getRepositoryReadMe(owner: String, repo: String): ApiResult<GitHubRepositoryContent> {
        return apiRequestHandler.safeApiCall(
            apiCall = {
                val response = httpClient.get("$baseUrl/repos/$owner/$repo/readme") {
                    header("Accept", "application/vnd.github+json")
                    header("Authorization", "Bearer $apiToken")
                    header("X-GitHub-Api-Version", apiVersion)
                }

                when (response.status.value) {
                    in 200..299 -> response.body<GitHubRepositoryContent>()
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
                    is ApiException.BadRequestException -> e
                    is ApiException.UnauthorizedException -> e
                    is ApiException.ForbiddenException -> e
                    is ApiException.NotFoundException -> e
                    is ApiException.ServerException -> e
                    is IOException -> ApiException.NetworkException("Network error occurred", e)
                    is SerializationException -> ApiException.ParseException("Serialization error occurred", e)
                    else -> ApiException.UnknownException("Unknown error occurred", e)

                }
            }
        )
    }
}