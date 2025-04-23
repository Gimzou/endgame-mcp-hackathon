package data.repository.taostats

import application.AppConfig
import data.model.taostats.PaginatedResponse
import data.model.taostats.Pagination
import data.model.taostats.subnet.SubnetIdentity
import data.network.taostats.TaostatsApi
import data.util.ApiRequestHandler.ApiResult
import di.DependencyContainer
import di.DependencyContainerImpl
import domain.repository.taostats.SubnetRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class SubnetRepositoryImplTest {
    private lateinit var testDependencyContainer : DependencyContainer
    private val mockTaostatsApi = mockk<TaostatsApi>()
    private lateinit var subnetRepository: SubnetRepository

    @BeforeTest
    fun setUp() {
        testDependencyContainer = DependencyContainerImpl(AppConfig.fromEnv())
        subnetRepository = SubnetRepositoryImpl(
            mockTaostatsApi,
            subnetCache = testDependencyContainer.getSubnetCache(),
            subnetListCache = testDependencyContainer.getSubnetListCache(),
            cachePolicy = testDependencyContainer.getSubnetCachePolicy(),
            logger = testDependencyContainer.getLogger(this.javaClass.simpleName)
        )
    }

    @Test
    fun `getAllSubnetIdentities fetches and combines all pages`() = runBlocking {
        val page1 = PaginatedResponse (
            Pagination(1, 2, 4, 2, 2, null),
            listOf(
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
        )
        val page2 = PaginatedResponse (
            Pagination(2, 2, 4, 2, null, 1),
            listOf(
                SubnetIdentity(
                    netuid = 3,
                    subnetName = "Test-3",
                    description = "A third description",
                    gitHubRepo = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                    subnetContact = null,
                    subnetUrl = null,
                    discord = null,
                    additional = null
                ),
                SubnetIdentity(
                    netuid = 4,
                    subnetName = "Test-4",
                    description = "A fourth description",
                    gitHubRepo = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                    subnetContact = null,
                    subnetUrl = null,
                    discord = null,
                    additional = null
                )
            )
        )

        coEvery { mockTaostatsApi.getSubnetIdentity(1, 2) } returns ApiResult.Success(page1)
        coEvery { mockTaostatsApi.getSubnetIdentity(2, 2) } returns ApiResult.Success(page2)

        val result = subnetRepository.getAllSubnetIdentities(2)

        assertEquals(4, result.size)
        assertEquals(setOf(1, 2, 3, 4), result.map { it.netuid }.toSet())
    }
}