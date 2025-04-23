package data.repository.github

import application.logging.Logger
import data.cache.Cache
import data.cache.CachePolicy
import data.model.github.repositories.GitHubRepositoryContent
import data.network.github.GitHubApi
import data.util.ApiRequestHandler.ApiResult
import domain.repository.github.GitHubRepoRepository
import org.apache.commons.codec.binary.Base64

class GitHubRepoRepositoryImpl(
    private val githubApi: GitHubApi,
    private val readMeCache: Cache<String, GitHubRepositoryContent>,
    private val gitHubCachePolicy: CachePolicy<GitHubRepositoryContent>,
    private val logger: Logger
) : GitHubRepoRepository {

    data class GithubUrlComponents(
        val owner: String,
        val repo: String
    )

    override suspend fun getGithubRepoReadMe(owner: String, repo: String): String {
        val cacheKey = gitHubCachePolicy.generateCacheKey(owner, repo)

        logger.debug("Retrieving ReadMe for $owner/$repo...")
        // Check if the content is cached
        readMeCache.get(cacheKey)?.let {
            logger.debug("Found in cache using cachekey: $cacheKey")
            return decodeContent(it.content, it.encoding)
        }

        // Cache miss - fetch from API
        logger.debug("Cache miss - fetching $owner/$repo from API...")
        val gitHubContent = when (val result = githubApi.getRepositoryReadMe(owner, repo)) {
            is ApiResult.Success -> {
                logger.debug("Retrieved ${result.data.name} from GitHub")
                result.data
            }
            is ApiResult.Error -> {
                logger.error("Error occurred while retrieving GitHub content from $owner/$repo")
                throw result.exception ?: Exception("Failed to get Github Repository ReadMe from $owner/$repo")
            }
        }

        // Cache the fetched content
        readMeCache.put(cacheKey, gitHubContent)
        logger.debug("Added fetched content to cache with key $cacheKey")

        return decodeContent(gitHubContent.content, gitHubContent.encoding)
    }

    override suspend fun getGithubRepoReadMe(url: String): String {
        val (owner, repo) = parseGithubUrl(url)
        return getGithubRepoReadMe(owner, repo)
    }

    private suspend fun parseGithubUrl(url: String) : GithubUrlComponents {
        return try {
            logger.debug("Parsing url $url...")
            // Remove potential trailing slash
            val cleanUrl = url.trimEnd('/')

            // Extract path components after github.com
            val path = cleanUrl.removePrefix("https://github.com/")

            // Split into owner and repo
            val (owner, repo) = path.split("/")
            logger.debug("Parsed url $url to owner: $owner, repo: $repo")

            if (owner.isNotBlank() && repo.isNotBlank()) {
                GithubUrlComponents(owner, repo)
            } else {
                throw Exception("Invalid Github Repository URL: $url")
            }
        } catch (e: Exception) {
            throw Exception("Failed to get Github Repository ReadMe from $url")
        }
    }

    private fun decodeContent(content: String, encoding: String): String {
        return when (encoding) {
            "base64" -> try {
                String(Base64().decode(content))
            } catch (e: Exception) {
                throw Exception("Failed to decode Github Repository ReadMe", e)
            }
            else -> throw Exception("Unrecognized encoding '$encoding' for GitHub Repository ReadMe")
        }
    }
}