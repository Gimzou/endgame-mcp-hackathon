package util

import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*

object TestDataLoader {
    inline fun <reified T> loadResponse(path: String): T {
        val json = loadJson(path)
        return Json.decodeFromString<T>(json)
    }

    fun loadJson(path: String): String {
        val inputStream = javaClass.classLoader.getResourceAsStream(path)
            ?: throw MissingResourceException("Missing $path", this.javaClass.simpleName, path)
        return InputStreamReader(inputStream, Charset.defaultCharset()).readText()
    }
}