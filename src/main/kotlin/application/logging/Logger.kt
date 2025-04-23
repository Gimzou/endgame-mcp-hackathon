package application.logging

interface Logger {
    suspend fun debug(message: String)
    suspend fun info(message: String)
    suspend fun warn(message: String)
    suspend fun error(message: String, throwable: Throwable? = null)
}