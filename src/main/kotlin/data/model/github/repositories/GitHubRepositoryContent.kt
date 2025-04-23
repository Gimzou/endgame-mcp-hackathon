package data.model.github.repositories

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepositoryContent(
    val type: String,
    val encoding: String,
    val size: Int,
    val name: String,
    val path: String,
    val content: String,
    val sha: String,
    val url: String,
    @SerialName("git_url") val gitUrl: String?,
    @SerialName("html_url") val htmlUrl: String?,
    @SerialName("download_url") val downloadUrl: String?,
    @SerialName("_links") val links: GitHubRepositoryLinks,
    val target: String? = null,         // Optional
    @SerialName("submodule_git_url") val submoduleGitUrl: String? = null // Optional
)
