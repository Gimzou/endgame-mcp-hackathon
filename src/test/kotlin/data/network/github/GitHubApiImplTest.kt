package data.network.github

import data.network.HttpClientProvider
import data.util.ApiException
import data.util.ApiRequestHandler
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import util.TestDataLoader
import kotlin.test.*

class GitHubApiImplTest {
    private lateinit var httpClient: HttpClient
    private lateinit var githubApi: GitHubApiImpl
    private lateinit var apiRequestHandler: ApiRequestHandler

    private val baseUrl = "https://api.github.com"
    private val token = "test-token"

    @BeforeTest
    fun setup() {
        val mockEngine = MockEngine { request ->
            when (request.url.toString()) {
                "$baseUrl/repos/owner/repo/readme" -> {
                    respond(
                        content = TestDataLoader.loadJson("test_data.github.repositories.content.responses.success/githubApiRepositoriesContentReadMeV20221128.json"),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "$baseUrl/repos/unknown-owner/repo/readme" -> {
                    respond(
                        content = """{"message": "Not Found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url}")
            }
        }
        httpClient = HttpClientProvider().create(mockEngine)
        apiRequestHandler = ApiRequestHandler(httpClient)

        githubApi = GitHubApiImpl(
            httpClient = httpClient,
            apiRequestHandler = apiRequestHandler,
            baseUrl = baseUrl,
            apiToken = token
        )
    }

    @Test
    fun `getRepositoryReadMe should return content when successful`() = runBlocking {
        // When
        val result = githubApi.getRepositoryReadMe("owner", "repo")

        // Then
        assertTrue(result is ApiRequestHandler.ApiResult.Success)

        assertEquals("file", result.data.type)
        assertEquals("base64", result.data.encoding)
        assertEquals("README.md", result.data.name)
    }

    @Test
    fun `getRepositoryReadMe should include authorization header`() = runBlocking {
        // Given - Create client that verifies headers
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    // Verify authorization header
                    assertEquals("Bearer $token", request.headers["Authorization"])

                    respond(
                        content = TestDataLoader.loadJson("test_data.github.repositories.content.responses.success/githubApiRepositoriesContentReadMeV20221128.json"),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }

        // When
        val api = GitHubApiImpl(httpClient, apiRequestHandler, baseUrl, token)
        val repositoryReadMe = api.getRepositoryReadMe("owner", "repo")
    }

    @Test
    fun `getRepositoryReadMe should handle error responses`() = runBlocking {
        // Given - Create client that returns error
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = """{"message": "Not Found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }

        // When
        val api = GitHubApiImpl(httpClient, apiRequestHandler, baseUrl, token)

        // Then
        try {
            val response = api.getRepositoryReadMe("owner", "repo")
        } catch (e : Exception) {
            assertTrue(e is ApiException.NotFoundException)
        }

    }
}