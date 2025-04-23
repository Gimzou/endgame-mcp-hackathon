package data.network.taostats

import data.util.ApiRequestHandler
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import util.TestDataLoader
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaostatsApiImplTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var api: TaostatsApiImpl
    private lateinit var apiRequestHandler: ApiRequestHandler

    // Base URL for the Taostats API
    val baseUrl = "https://api.taostats.io/api/"
    val mockApiKey = "XXXXXXXXXXXXXXXXXXXXXXXXXXX"

    @BeforeTest
    fun setUp() {
        mockEngine = MockEngine { request ->
            when(request.url.toString()) {
                "https://api.taostats.io/api/subnet/identity/v1?page=2&limit=4" -> {
                    respond(
                        content = TestDataLoader.loadJson("test_data/taostats/subnet/responses/success/apiSubnetIdentityV1.json"),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                } else -> {
                    respond(
                        content = "",
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }

        apiRequestHandler = ApiRequestHandler(mockEngine)

        api = TaostatsApiImpl(
            httpClient = HttpClient(mockEngine) {
                defaultRequest {
                    url(baseUrl)
                    headers {
                        header(HttpHeaders.Accept, ContentType.Application.Json)
                        header(HttpHeaders.Authorization, mockApiKey)
                    }
                }
                // Install content negotiation plugin for JSON serialization/deserialization
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    })
                }
            },
            apiRequestHandler = apiRequestHandler,
            apiToken = mockApiKey
        )
    }

    @Test
    fun `getSubnetIdentity returns a page listing subnet identities`() = runBlocking {
        // Given
        val result = api.getSubnetIdentity(2, 4)

        // Then
        assertTrue(result is ApiRequestHandler.ApiResult.Success)

        assertEquals(4, result.data.data.size, "Subnet list size mismatch")
        assertEquals(2, result.data.pagination.currentPage, "Current page incorrect")
        assertEquals("Targon", result.data.data[0].subnetName, "Subnet name incorrect")
    }
}