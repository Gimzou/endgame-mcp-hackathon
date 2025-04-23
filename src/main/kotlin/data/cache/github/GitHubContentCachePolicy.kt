package data.cache.github

import data.cache.CachePolicy
import data.model.github.repositories.GitHubRepositoryContent

class GitHubContentCachePolicy: CachePolicy<GitHubRepositoryContent> {

    override fun shouldCache(data: GitHubRepositoryContent): Boolean {
        return when (data.type) {
            "file" -> true
            else -> false
        }
    }

    override fun getCacheTtl(data: GitHubRepositoryContent): Long {
        return when (data.type) {
            "file" -> 60 * 60 * 1 // 1 hour
            else -> 0
        }
    }

    override fun generateCacheKey(vararg params: Any): String {
        // create a unique key based on the parameters
        require(params.size == 2) { "GitHubContentCachePolicy requires 2 parameters, a repository owner and a repository name" }
        val owner = params[0].toString()
        val name = params[1].toString()
        return "github:content:$owner:$name"
    }
}