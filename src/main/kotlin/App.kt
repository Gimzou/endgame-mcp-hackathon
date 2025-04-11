
import io.ktor.utils.io.streams.*
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered

/**
 * Start mcp server (default : runs using standard input/output).
 *
 * @param args
 * - "--stdio": Runs an MCP server using standard input/output.
 */
fun main(args: Array<String>) {
    // Validate the TAOSTATS_API_KEY
    val taostatsApiKey = System.getenv("TAOSTATS_API_KEY")
        ?: throw IllegalStateException("TAOSTATS_API_KEY environment variable is not set")

    // Validate the command line arguments
    val command = args.firstOrNull() ?: "--stdio"
    when (command) {
        "--stdio" -> runMcpServerUsingStdio(taostatsApiKey)
        else -> {
            System.err.println("Unknown command: $command")
        }
    }


}


/**
 * Run mcp server using standard input/output.
 */
fun runMcpServerUsingStdio(apikey : String) {
    // Configure the mcp server
    val server = configureServer(apikey)

    // Create a transport using standard IO for server communication
    val transport = StdioServerTransport(
        System.`in`.asInput(),
        System.out.asSink().buffered()
    )

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onClose {
            done.complete()
        }
        done.join()
    }
}
