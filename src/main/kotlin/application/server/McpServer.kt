package application.server

import io.ktor.server.application.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server

/**
 * MCP Server Interface
 */
interface McpServer {
    /**
     * Get the raw server instance
     *
     * @return Server instance
     */
    fun getServer(): Server

    /**
     * Check if server is running
     *
     * @return true if server is running, false otherwise
     */
    fun isRunning(): Boolean

    /**
     * Start the server with STDIO transport
     */
    suspend fun startWithStdio()

    /**
     * Apply the server configuration to a Ktor application for SSE transport
     *
     * @param application Ktor application
     * @param route Route to configure the server
     */
    fun configureKtorApplication(application: Application, route: String = "/mcp")

    /**
     * Configure a tool
     *
     * @param name Name of the tool
     * @param description Description of the tool
     * @param inputSchema Input schema of the tool
     * @param handler Handler for the tool
     */
    fun configureTool(name: String, description: String, inputSchema: Tool.Input, handler: suspend (CallToolRequest) -> CallToolResult)

    /**
     * Configure a resource
     *
     * @param uri URI of the resource
     * @param name Name of the resource
     * @param description Description of the resource
     * @param mimeType MIME type of the resource
     * @param handler Handler for the resource
     */
    fun configureResource(uri: String, name: String, description: String, mimeType: String, handler: suspend (ReadResourceRequest) -> ReadResourceResult)

    /**
     * Configure a prompt
     *
     * @param name Name of the prompt
     * @param description Description of the prompt
     * @param arguments Arguments of the prompt
     * @param handler Handler for the prompt
     */
    fun configurePrompt(name: String, description: String, arguments: List<PromptArgument>, handler: suspend (GetPromptRequest) -> GetPromptResult)

    /**
     * Stop the server
     */
    suspend fun shutdown()
}