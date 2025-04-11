package data.network

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

object SubnetIdentityTestData {
    private fun loadJson(fileName: String): String {
        val inputStream = javaClass.classLoader.getResourceAsStream("test_data/subnet/responses/$fileName")
            ?: throw MissingResourceException("Missing $fileName", this.javaClass.simpleName, fileName)
        return InputStreamReader(inputStream, Charset.defaultCharset()).readText()
    }

    val subnetIdentityJson = loadJson("success/apiSubnetIdentityV1.json")
}

class TaostatsApiImplTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var api: TaostatsApiImpl
    // Base URL for the Taostats API
    val baseUrl = "https://api.taostats.io/api/"
    val mockApiKey = "XXXXXXXXXXXXXXXXXXXXXXXXXXX"

    @BeforeTest
    fun setUp() {
        mockEngine = MockEngine { request ->
            when(request.url.toString()) {
                "https://api.taostats.io/api/subnet/identity/v1?page=2&limit=4" -> {
                    respond(
                        content = SubnetIdentityTestData.subnetIdentityJson,
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

        api = TaostatsApiImpl(HttpClient(mockEngine) {
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
        })
    }

    @Test
    fun `getSubnetIdentity returns a page listing subnet identities`() {
        val result = runBlocking {  api.getSubnetIdentity(2, 4) }

        assertEquals(4, result.data.size, "Subnet list size mismatch")
        assertEquals(2, result.pagination.currentPage, "Current page incorrect")
        assertEquals("Targon", result.data[0].subnetName, "Subnet name incorrect")
    }
}