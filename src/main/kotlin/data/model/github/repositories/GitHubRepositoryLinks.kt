package data.model.github.repositories

import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryLinks(
    val git: String?,
    val html: String?,
    val self: String
)
