package data.network.github

import data.model.github.repositories.GitHubRepositoryContent
import data.util.ApiRequestHandler.ApiResult

/**
 * Api for GitHub
 */
interface GitHubApi {
    /**
     * Get the preferred README for a repository
     *
     * @param owner The owner of the repository
     * @param repo The name of the repository
     * @return The preferred README for the repository
     */
    suspend fun getRepositoryReadMe(owner: String, repo: String) : ApiResult<GitHubRepositoryContent>
}