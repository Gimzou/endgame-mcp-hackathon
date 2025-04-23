package domain.repository.github

/**
 * Interface for managing GitHub repository interaction through GitHub API
 */
interface GitHubRepoRepository {
    /**
     * Get the readme of a GitHub repository
     * @param owner the owner of the repository
     * @param repo the name of the repository
     * @return the readme content of the repository
     */
    suspend fun getGithubRepoReadMe(owner: String, repo: String): String

    /**
     * Get the readme of a GitHub repository
     * @param url the url of the repository
     * @return the readme content of the repository
     */
    suspend fun getGithubRepoReadMe(url: String): String
}