package data.repository.github

import application.AppConfig
import data.model.github.repositories.GitHubRepositoryContent
import data.model.github.repositories.GitHubRepositoryLinks
import data.network.github.GitHubApi
import data.util.ApiRequestHandler.ApiResult
import di.DependencyContainer
import di.DependencyContainerImpl
import domain.repository.github.GitHubRepoRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import util.TestDataLoader
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GitHubRepoRepositoryImplTest {
    private lateinit var testDependencyContainer : DependencyContainer
    private val mockGitHubApi = mockk<GitHubApi>()
    private lateinit var githubRepoRepository: GitHubRepoRepository

    @BeforeTest
    fun setUp() {
        testDependencyContainer = DependencyContainerImpl(AppConfig.fromEnv())
        githubRepoRepository = GitHubRepoRepositoryImpl(
            mockGitHubApi,
            testDependencyContainer.getGitHubContentCache(),
            testDependencyContainer.getGitHubContentCachePolicy(),
            testDependencyContainer.getLogger(this.javaClass.simpleName)
        )
    }

    @Test
    fun `getGithubRepoReadMe should decode base64 content from api response`() = runBlocking {
        // Given
        val expectedText = "encoded content ..."
        val invalidText = "a different encoded content ..."

        val apiResponseWithBase64EncodedContent =
            TestDataLoader.loadResponse<GitHubRepositoryContent>("test_data.github.repositories.content.responses.success/githubApiRepositoriesContentReadMeV20221128.json")

        coEvery { mockGitHubApi.getRepositoryReadMe("test", "test") } returns ApiResult.Success(apiResponseWithBase64EncodedContent)

        // When
        val textContent = githubRepoRepository.getGithubRepoReadMe("test", "test")

        // Then
        assertEquals(expectedText, textContent)
        assertNotEquals(invalidText, textContent)
    }

    @Test
    fun `getGithubRepoReadMe should handle valid github repository urls`() = runBlocking {
        // Given
        val validGithubRepositoryUrls = "https://github.com/test-owner/test-repo-name"
        val validContent = "encoded content ..."
        val content = GitHubRepositoryContent(
            content = "ZW5jb2RlZCBjb250ZW50IC4uLg==",
            encoding = "base64",
            type = "",
            size = 0,
            name = "",
            path = "",
            sha = "",
            url = "",
            gitUrl = "",
            htmlUrl = "",
            downloadUrl = "",
            links = GitHubRepositoryLinks(
                git = "",
                html = "",
                self = ""
            ),
            target = "",
            submoduleGitUrl = ""
        )

        coEvery { mockGitHubApi.getRepositoryReadMe("test-owner", "test-repo-name") } returns ApiResult.Success(content)

        // When
        val result = githubRepoRepository.getGithubRepoReadMe(validGithubRepositoryUrls)

        // Then
        assertEquals(validContent, result)
    }
}