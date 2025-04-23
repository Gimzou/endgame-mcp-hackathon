package di

import application.logging.Logger
import data.cache.Cache
import data.cache.CachePolicy
import data.model.github.repositories.GitHubRepositoryContent
import data.model.taostats.subnet.SubnetIdentity
import data.network.github.GitHubApi
import data.network.taostats.TaostatsApi
import domain.repository.github.GitHubRepoRepository
import domain.repository.taostats.SubnetRepository
import data.util.ApiRequestHandler
import domain.service.subnet.SubnetService
import io.ktor.client.*

/**
 * Dependency container
 */
interface DependencyContainer {
    fun getLogger(source: String): Logger
    fun getHttpClient(): HttpClient
    fun getApiRequestHandler(): ApiRequestHandler
    fun getTaostatsApi(): TaostatsApi
    fun getGitHubApi(): GitHubApi
    fun getSubnetRepository(): SubnetRepository
    fun getGitHubRepoRepository(): GitHubRepoRepository
    fun getSubnetCache(): Cache<String, SubnetIdentity>
    fun getSubnetListCache(): Cache<String, List<SubnetIdentity>>
    fun getSubnetCachePolicy(): CachePolicy<SubnetIdentity>
    fun getGitHubContentCache(): Cache<String, GitHubRepositoryContent>
    fun getGitHubContentCachePolicy(): CachePolicy<GitHubRepositoryContent>
    fun getSubnetService(): SubnetService
}