
import data.network.TaostatsApiImpl
import data.repository.SubnetRepositoryImpl
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import domain.service.subnet.SubnetService

/**
 * Configure mcp server.
 *
 * @param apikey Taostats API key
 * @return Server instance
 */
fun configureServer(apikey : String): Server {
    // Base URL for the Taostats API
    val baseUrl = "https://api.taostats.io/api/"

    // Create an HTTP client with a default request configuration and JSON content negotiation
    val httpClient = HttpClient {
        defaultRequest {
            url(baseUrl)
            headers {
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.Authorization, apikey)
            }
        }
        // Install content negotiation plugin for JSON serialization/deserialization
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    val server = Server(
        Implementation(
            name = "mcp-kotlin taostat client",
            version = "0.0.1"
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
                prompts = ServerCapabilities.Prompts(listChanged = true),
                resources = ServerCapabilities.Resources(subscribe = true, listChanged = true),
                tools = ServerCapabilities.Tools(listChanged = true),
            )
        )
    )

    server.addTool(
        name = "list_subnet_repositories",
        description = """
            List the github repository of the bittensor subnets.
            The subnets are identified by a unique identifier (netuid) and other characteristics such as a name and their github repository url.
        """.trimIndent()
    ) { request ->

        server.sendLoggingMessage(
            LoggingMessageNotification(
                level = LoggingLevel.info,
                data = JsonObject(mapOf("message" to JsonPrimitive("list_subnet_repositories tool called"))),
            )
        )

        val taostatsApi = TaostatsApiImpl(httpClient)
        val subnetRepository = SubnetRepositoryImpl(taostatsApi)
        val subnetDetails = SubnetService(subnetRepository).getSubnetIdentityList()
        CallToolResult(content = subnetDetails.map { TextContent(it) })
    }

    return server
}