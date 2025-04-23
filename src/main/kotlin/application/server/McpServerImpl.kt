package application.server

import application.logging.Logger
import application.logging.McpLoggerImpl
import application.logging.McpServerLogHandler
import di.DependencyContainer
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.utils.io.streams.*
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.coroutines.*
import kotlinx.io.asSink
import kotlinx.io.buffered
import java.util.concurrent.atomic.AtomicBoolean

class McpServerImpl(
    serverName: String = "Bittensor Subnet Explorer",
    serverVersion: String = "0.0.1",
    dependencies: DependencyContainer
) : McpServer {
    private val server: Server = Server(
        serverInfo = Implementation(
            name = serverName,
            version = serverVersion
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                prompts = ServerCapabilities.Prompts(
                    listChanged = true
                ),
                resources = ServerCapabilities.Resources(
                    subscribe = true,
                    listChanged = true
                ),
                tools = ServerCapabilities.Tools(
                    listChanged = true
                ),
            )
        )
    )
    private val isRunning = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val logger: Logger = McpLoggerImpl(McpServerLogHandler(), "mcp-server")

    override suspend fun startWithStdio() {
        if (isRunning.getAndSet(true)) {
            logger.warn("Server is already running")
            return
        }

        try {
            val transport = StdioServerTransport(
                System.`in`.asInput(),
                System.out.asSink().buffered()
            )

            server.connect(transport)

            logger.info("MCP server started successfully")

            // Keep the server running
            val done = Job()
            server.onClose { done.complete() }
            done.join()

        } catch (e: Exception) {
            logger.error("Error starting MCP server: ${e.message}")
            throw e
        } finally {
            isRunning.set(false)
        }
    }

    override fun configureKtorApplication(application: Application, route: String) {
        with(application) {
            // Install SSE support
            install(SSE)

            // Configure routing
            routing {
                route(route) {
                    mcp {
                        // Use the existing server instance
                        server
                    }
                }
            }
        }

        isRunning.set(true)
        runBlocking {
            logger.info("MCP server configured for SSE transport at route: $route")
        }
    }

    /**
     * Configure tools for the server
     */
    override fun configureTool(
        name: String,
        description: String,
        inputSchema: Tool.Input,
        handler: suspend (CallToolRequest) -> CallToolResult
    ) {
        server.addTool(name, description, inputSchema, handler)
    }

    /**
     * Configure resources for the server
     */
    override fun configureResource(
        uri: String,
        name: String,
        description: String,
        mimeType: String,
        handler: suspend (ReadResourceRequest) -> ReadResourceResult
    ) {
        server.addResource(uri, name, description, mimeType, handler)
    }

    /**
     * Configure prompts for the server
     */
    override fun configurePrompt(
        name: String,
        description: String,
        arguments: List<PromptArgument>,
        handler: suspend (GetPromptRequest) -> GetPromptResult
    ) {
        val prompt = Prompt(
            name = name,
            description = description,
            arguments = arguments
        )
        server.addPrompt(prompt, handler)
    }

    override fun getServer(): Server {
        return server
    }

    override fun isRunning(): Boolean {
        return isRunning.get()
    }

    override suspend fun shutdown() {
        isRunning.set(false)
        logger.info("Shutting down MCP server...")
        scope.launch {
            try {
                // Attempt to close any connection
                server.close()
                logger.info("MCP server closed successfully")
            } catch (e: Exception) {
                logger.error("Error closing MCP server: ${e.message}")
            }
        }
    }
}