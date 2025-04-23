package di

import application.AppConfig
import application.logging.Logger
import application.logging.McpLoggerImpl
import application.logging.McpServerLogHandler
import data.cache.Cache
import data.cache.CachePolicy
import data.cache.InMemoryCache
import data.cache.github.GitHubContentCachePolicy
import data.cache.taostats.TaostatsSubnetCachePolicy
import data.model.github.repositories.GitHubRepositoryContent
import data.model.taostats.subnet.SubnetIdentity
import data.network.HttpClientProvider
import data.network.github.GitHubApi
import data.network.github.GitHubApiImpl
import data.network.taostats.TaostatsApi
import data.network.taostats.TaostatsApiImpl
import domain.repository.github.GitHubRepoRepository
import data.repository.github.GitHubRepoRepositoryImpl
import domain.repository.taostats.SubnetRepository
import data.repository.taostats.SubnetRepositoryImpl
import data.util.ApiRequestHandler
import domain.service.subnet.SubnetService
import io.ktor.client.*
import io.ktor.client.engine.cio.*

class DependencyContainerImpl(private val config: AppConfig) : DependencyContainer {
    // Lazy initialized dependencies
    private val dependencies = mutableMapOf<String, Any>()

    private val loggers = mutableMapOf<String, McpLoggerImpl>()

    override fun getLogger(source: String): Logger {
        return loggers.getOrPut(source, { McpLoggerImpl(McpServerLogHandler(), source) })
    }

    override fun getHttpClient(): HttpClient {
        return getOrCreate("httpClient") {
            HttpClientProvider().create(CIO.create())
        }
    }

    override fun getApiRequestHandler(): ApiRequestHandler {
        return getOrCreate("apiRequestHandler") {
            ApiRequestHandler(getHttpClient())
        }
    }

    override fun getTaostatsApi(): TaostatsApi {
        return getOrCreate("taostatsApi") {
            TaostatsApiImpl(
                httpClient = getHttpClient(),
                apiRequestHandler = getApiRequestHandler(),
                baseUrl = config.taostatsBaseUrl,
                apiToken = config.taostatsApiToken
            )
        }
    }

    override fun getGitHubApi(): GitHubApi {
        return getOrCreate("gitHubApi") {
            GitHubApiImpl(
                httpClient = getHttpClient(),
                apiRequestHandler = getApiRequestHandler(),
                baseUrl = config.gitHubBaseUrl,
                apiToken = config.gitHubApiToken
            )
        }
    }

    override fun getSubnetRepository(): SubnetRepository {
        return getOrCreate("subnetRepository") {
            SubnetRepositoryImpl(
                taostatsApi = getTaostatsApi(),
                subnetCache = getSubnetCache(),
                subnetListCache = getSubnetListCache(),
                cachePolicy = getSubnetCachePolicy(),
                logger = getLogger("subnetRepository")
            )
        }
    }

    override fun getGitHubRepoRepository(): GitHubRepoRepository {
        return getOrCreate("gitHubRepoRepository") {
            GitHubRepoRepositoryImpl(
                githubApi = getGitHubApi(),
                readMeCache = getGitHubContentCache(),
                gitHubCachePolicy = getGitHubContentCachePolicy(),
                logger = getLogger("gitHubRepoRepository")
            )
        }
    }

    override fun getSubnetCache(): Cache<String, SubnetIdentity> {
        return getOrCreate("subnetCache") {
            InMemoryCache("subnetCache")
        }
    }

    override fun getSubnetListCache(): Cache<String, List<SubnetIdentity>> {
        return getOrCreate("subnetListCache") {
            InMemoryCache("subnetListCache")
        }
    }

    override fun getSubnetCachePolicy(): CachePolicy<SubnetIdentity> {
        return getOrCreate("subnetCachePolicy") {
            TaostatsSubnetCachePolicy()
        }
    }

    override fun getGitHubContentCache(): Cache<String, GitHubRepositoryContent> {
        return getOrCreate("gitHubContentCache") {
            InMemoryCache("gitHubContentCache")
        }
    }

    override fun getGitHubContentCachePolicy(): CachePolicy<GitHubRepositoryContent> {
        return getOrCreate("gitHubContentCachePolicy") {
            GitHubContentCachePolicy()
        }
    }

    override fun getSubnetService(): SubnetService {
        return getOrCreate("subnetService") {
            SubnetService(
                subnetRepository = getSubnetRepository(),
                gitHubRepoRepository = getGitHubRepoRepository()
            )
        }
    }

    /**
     * Generic helper for lazy  loading dependencies
     *
     * @param key The key of the dependency
     * @param factory The factory function to create the dependency
     *
     * @return The dependency
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> getOrCreate(key: String, factory: () -> T) : T{
        return if (dependencies.containsKey(key)) {
            dependencies[key] as T
        } else {
            val dependency = factory()
            dependencies[key] = dependency as Any
            dependency
        }
    }

    /**
     * Clears the dependency container
     */
    fun clear() {
        dependencies.clear()
    }
}