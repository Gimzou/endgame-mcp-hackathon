package domain.service.subnet

import data.model.taostats.subnet.SubnetIdentity
import data.repository.SubnetRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SubnetServiceTest {
    private val mockSubnetRepository = mockk<SubnetRepository>()
    private val subnetService = SubnetService(mockSubnetRepository)

    @Test
    fun `getSubnetIdentityList returns the github repository`() {
        val testData = listOf(
            SubnetIdentity(
                netuid = 1,
                subnetName = "Test-1",
                description = "A first description",
                githubRepo = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                subnetContact = null,
                subnetUrl = null,
                discord = null,
                additional = null,
            ),
            SubnetIdentity(
                netuid = 2,
                subnetName = "Test-2",
                description = "A second description",
                githubRepo = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
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
                Github Repository : XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            """.trimIndent(), result[0])
    }
}