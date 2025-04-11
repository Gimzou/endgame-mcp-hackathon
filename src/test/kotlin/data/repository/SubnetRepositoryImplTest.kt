package data.repository

import data.model.taostats.PaginatedResponse
import data.model.taostats.Pagination
import data.model.taostats.subnet.SubnetIdentity
import data.network.TaostatsApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SubnetRepositoryImplTest {
    private val mockTaostatsApi = mockk<TaostatsApi>()
    private val subnetRepository = SubnetRepositoryImpl(mockTaostatsApi)

    @Test
    fun `getAllSubnetIdentities fetches and combines all pages`() {
        val page1 = PaginatedResponse (
            Pagination(1, 2, 4, 2, 2, null),
            listOf(
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
        )
        val page2 = PaginatedResponse (
            Pagination(2, 2, 4, 2, null, 1),
            listOf(
                SubnetIdentity(
                    netuid = 3,
                    subnetName = "Test-3",
                    description = "A third description",
                    githubRepo = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                    subnetContact = null,
                    subnetUrl = null,
                    discord = null,
                    additional = null
                ),
                SubnetIdentity(
                    netuid = 4,
                    subnetName = "Test-4",
                    description = "A fourth description",
                    githubRepo = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                    subnetContact = null,
                    subnetUrl = null,
                    discord = null,
                    additional = null
                )
            )
        )

        coEvery { mockTaostatsApi.getSubnetIdentity(1, 2) } returns page1
        coEvery { mockTaostatsApi.getSubnetIdentity(2, 2) } returns page2

        val result = runBlocking { subnetRepository.getAllSubnetIdentities(2) }

        assertEquals(4, result.size)
        assertEquals(setOf(1, 2, 3, 4), result.map { it.netuid }.toSet())
    }
}