package application.logging

import io.modelcontextprotocol.kotlin.sdk.LoggingLevel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Logger implementation for MCP
 */
class McpLoggerImpl(
    private val logHandler: LogHandler,
    private val source: String,
) : Logger {

    override suspend fun debug(message: String) {
        log(LoggingLevel.debug, message)
    }

    override suspend fun info(message: String) {
        log(LoggingLevel.info, message)
    }

    override suspend fun warn(message: String) {
        log(LoggingLevel.warning, message)
    }

    override suspend fun error(message: String, throwable: Throwable?) {
        val fullMessage = if (throwable != null) {
            "${throwable.message}\n${throwable.stackTraceToString()}"
        } else {
            message
        }

        log(LoggingLevel.error, fullMessage)
    }

    private suspend fun log(level: LoggingLevel, message: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val formattedMessage = "$timestamp [$level] [$source] - $message"

        logHandler.handleLog(level, formattedMessage)
    }
}