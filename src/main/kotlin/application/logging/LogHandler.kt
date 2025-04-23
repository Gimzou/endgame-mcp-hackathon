package application.logging

import io.modelcontextprotocol.kotlin.sdk.LoggingLevel

/**
 * Log handler interface
 */
interface LogHandler {
    suspend fun handleLog(level: LoggingLevel, message: String)
}