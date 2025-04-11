package domain.service.subnet

import data.repository.SubnetRepository

class SubnetService(private val subnetRepository: SubnetRepository) {

    suspend fun getSubnetIdentityList(): List<String> {
        return subnetRepository.getAllSubnetIdentities().map { subnetIdentity ->
            """
                Id : ${subnetIdentity.netuid}
                Name : ${subnetIdentity.subnetName}
                Description : ${subnetIdentity.description}
                Github Repository : ${subnetIdentity.githubRepo}
            """.trimIndent()
        }
    }
}