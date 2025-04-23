package domain.service.subnet

import data.model.taostats.subnet.SubnetIdentity
import domain.repository.github.GitHubRepoRepository
import domain.repository.taostats.SubnetRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SubnetServiceTest {
    private val mockSubnetRepository = mockk<SubnetRepository>()
    private val mockGitHubRepoRepository = mockk<GitHubRepoRepository>()
    private val subnetService = SubnetService(mockSubnetRepository, mockGitHubRepoRepository)

    @Test
    fun `getSubnetIdentityList returns the github repository`() {
        val testData = listOf(
            SubnetIdentity(
                netuid = 1,
                subnetName = "Test-1",
                description = "A first description",
                gitHubRepo = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                subnetContact = null,
                subnetUrl = null,
                discord = null,
                additional = null,
            ),
            SubnetIdentity(
                netuid = 2,
                subnetName = "Test-2",
                description = "A second description",
                gitHubRepo = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                subnetContact = null,
                subnetUrl = null,
                discord = null,
                additional = null
            )
        )

        coEvery { mockSubnetRepository.getAllSubnetIdentities() } returns testData

        val result = runBlocking { subnetService.getSubnetIdentityList() }

        assertEquals("""
                Id : 1
                Name : Test-1
                Description : A first description
                GitHub Repository : XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            """.trimIndent(), result[0])
    }

    @Test
    fun `getRepositoryReadMe returns the README file of a github repository`() = runBlocking {
        // Given
        val testData = "This is a test README file"
        val subnet = SubnetIdentity(
            netuid = 1,
            subnetName = "Test-1",
            description = "A first description",
            gitHubRepo = "https://github.com/owner/repo",
            subnetContact = null,
            subnetUrl = null,
            discord = null,
            additional = null
        )
        val owner = "owner"
        val repo = "repo"

        // When
        coEvery { mockSubnetRepository.getSubnetIdentityById(subnet.netuid) } returns subnet
        coEvery { mockGitHubRepoRepository.getGithubRepoReadMe(subnet.gitHubRepo!!) } returns testData

        val result = runBlocking { subnetService.getSubnetReadMe(1) }

        // Then
        assertEquals(testData, result)
    }
}