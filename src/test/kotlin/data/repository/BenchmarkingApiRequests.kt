package data.repository

import di.DependencyContainerImpl
import application.AppConfig
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class BenchmarkingApiRequests {
    private val appConfig = AppConfig.fromEnv()
    private val container = DependencyContainerImpl(appConfig)
    private val gitHubRepoRepository = container.getGitHubRepoRepository()
    private val subnetRepository = container.getSubnetRepository()

    @Test
    fun benchmarkGitHubRepoContentReadMe() = runBlocking {
        val firstRequestTime = measureTimeMillis {
            val owner = "Gimzou"
            val repo = "endgame-mcp-hackathon"
            gitHubRepoRepository.getGithubRepoReadMe("https://github.com/$owner/$repo")
            println("Retrieved ReadMe from $owner/$repo")
        }
        println("First request took $firstRequestTime ms")

        val secondRequestTime = measureTimeMillis {
            val owner = "Gimzou"
            val repo = "endgame-mcp-hackathon"
            gitHubRepoRepository.getGithubRepoReadMe("https://github.com/$owner/$repo")
            println("Retrieved ReadMe from $owner/$repo")
        }
        println("Second request took $secondRequestTime ms")
    }

    @Test
    fun benchmarkSubnetList() = runBlocking {
        val firstRequestTime = measureTimeMillis {
            val list = subnetRepository.getAllSubnetIdentities()
            println("Retrieved list of subnets (size: ${list.size})")
        }
        println("First request took $firstRequestTime ms")

        val secondRequestTime = measureTimeMillis {
            val list = subnetRepository.getAllSubnetIdentities()
            println("Retrieved list of subnets (size: ${list.size})")
        }
        println("Second request took $secondRequestTime ms")
    }
}