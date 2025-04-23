package domain.service.subnet

import domain.repository.github.GitHubRepoRepository
import domain.repository.taostats.SubnetRepository

class SubnetService(
    private val subnetRepository: SubnetRepository,
    private val gitHubRepoRepository: GitHubRepoRepository
) {

    /**
     * List all subnets
     * @return List<String>
     */
    suspend fun getSubnetIdentityList(): List<String> {
        return subnetRepository.getAllSubnetIdentities().map { subnetIdentity ->
            """
                Id : ${subnetIdentity.netuid}
                Name : ${subnetIdentity.subnetName}
                Description : ${subnetIdentity.description}
                GitHub Repository : ${subnetIdentity.gitHubRepo}
            """.trimIndent()
        }
    }

    /**
     * Get subnet GitHub repository url
     *
     * @param  id: Int
     * @return String?
     */
    private suspend fun getSubnetGithubRepository(id: Int) : String? {
        val subnetIdentity = subnetRepository.getSubnetIdentityById(id)
        return subnetIdentity.gitHubRepo
    }

    /**
     * Fetch the content of the README file of the GitHub repository of a subnet
     *
     * @param  id: Int
     * @return String
     */
    suspend fun getSubnetReadMe(id: Int): String {
        // Get subnet GitHub repository
        val githubRepoUrl = getSubnetGithubRepository(id)
            ?: throw Exception("No github repository mentioned for subnet id $id")

        // Fetch the content of the README file of the GitHub repository of the subnet
        return gitHubRepoRepository.getGithubRepoReadMe(githubRepoUrl)
    }

}