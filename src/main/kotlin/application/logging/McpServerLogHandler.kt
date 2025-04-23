package application.logging

import io.modelcontextprotocol.kotlin.sdk.LoggingLevel

class McpServerLogHandler : LogHandler {
    override suspend fun handleLog(level: LoggingLevel, message: String) {
        // Write log message to stderr - this doesn't interfere with STDIO
        System.err.println(message)
    }
}