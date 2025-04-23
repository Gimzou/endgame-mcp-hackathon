
import application.AppConfig
import application.server.McpServer
import application.server.McpServerImpl
import di.DependencyContainer
import di.DependencyContainerImpl
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Start mcp server (default : runs using standard input/output).
 *
 * @param args
 * - "--stdio": Runs an MCP server using standard input/output.
 * - "--sse": Runs an MCP server using server-sent events.
 * - Any other value: Prints an error message.
 */
fun main(args: Array<String>): Unit = runBlocking {
    val command = args.firstOrNull() ?: "--stdio"

    // Create config from environment variables
    val config = AppConfig.fromEnv()

    // Create dependency container
    val container = DependencyContainerImpl(config)

    // Create MCP server
    val mcpServer = McpServerImpl(
        serverName = "Bittensor Subnet Explorer",
        serverVersion = "0.0.1",
        dependencies = container
    )

    // Configures tools
    configureTools(mcpServer, container)

    // Validate the command line arguments
    when (command) {
        "--stdio" -> mcpServer.startWithStdio()
        "--sse" -> embeddedServer(CIO, port = 8080) {
            mcpServer.configureKtorApplication(this)
        }.start(wait = true)
        else -> {
            System.err.println("Unknown command: $command")
        }
    }
}

/**
 * Configure tools for the MCP server.
 *
 * @param mcpServer The MCP server instance.
 * @param dependencies The dependency container.
 */
private fun configureTools(mcpServer: McpServer, dependencies: DependencyContainer) {
    val subnetService = dependencies.getSubnetService()

    mcpServer.configureTool(
        name = "get_subnet_list",
        description = """
            List the current bittensor subnets.
            The subnets are identified by a unique identifier (netuid) and other characteristics such as a name and their github repository url.
        """.trimIndent(),
        inputSchema = Tool.Input()
    ) {
        val allSubnetIdentities = subnetService.getSubnetIdentityList()
        CallToolResult(content = allSubnetIdentities.map { TextContent(it) })
    }

    mcpServer.configureTool(
        name = "get_subnet_documentation",
        description = """
            Retrieve the technical documentation of a bittensor subnet from it's GitHub repository.
            
            Given a unique identifier (netuid), the tool will find the GitHub repository url from the list of bittensor subnets.
            It will then retrieve the technical documentation from the repository and return it as a string.
        """.trimIndent(),
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("netuid") {
                    put("type", "integer")
                    put("description", "A unique identifier of the subnet (e.g. 1, 2, 99)")
                }
            },
            required = listOf("state")
        )
    ) { request ->

        val netuid = request.arguments["netuid"]?.jsonPrimitive?.content
            ?: return@configureTool CallToolResult(
                content = listOf(TextContent("The 'netuid' parameter is required."))
            )

        val documentation = subnetService.getSubnetReadMe(netuid.toInt())
        CallToolResult(content = listOf(TextContent(documentation)))
    }
}



