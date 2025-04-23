package data.repository.taostats

import application.logging.Logger
import data.cache.Cache
import data.cache.CachePolicy
import data.model.taostats.subnet.SubnetIdentity
import data.network.taostats.TaostatsApi
import data.util.ApiException
import data.util.ApiRequestHandler
import data.util.PaginationHandler
import domain.repository.taostats.SubnetRepository

class SubnetRepositoryImpl(
    private val taostatsApi : TaostatsApi,
    private val subnetCache : Cache<String, SubnetIdentity>,
    private val subnetListCache : Cache<String, List<SubnetIdentity>>,
    private val cachePolicy: CachePolicy<SubnetIdentity>,
    private val logger: Logger
) : SubnetRepository {

    override suspend fun getAllSubnetIdentities(perPage: Int): List<SubnetIdentity> {
        val cacheKey = cachePolicy.generateCacheKey("all", "subnets")

        // Check if data is in cache first
        logger.debug("Retrieving all bittensor subnets...")
        subnetListCache.get(cacheKey)?.let {
            logger.debug("Found in cache using cachekey: $cacheKey")
            return it
        }

        // Cache miss - fetch from API
        logger.debug("Cache miss - fetching subnets from API ($perPage per page)...")
        val paginationHandler = PaginationHandler { page, itemsPerPage, _ ->
            when (val result = taostatsApi.getSubnetIdentity(page, itemsPerPage)) {
                is ApiRequestHandler.ApiResult.Success -> {
                    logger.debug("Retrieved page ${result.data.pagination.currentPage} of ${result.data.pagination.totalPages} from Taostats")
                    result.data
                }
                is ApiRequestHandler.ApiResult.Error -> {
                    logger.error("Error fetching subnets from Taostats")
                    throw result.exception ?: ApiException.UnknownException("Unknown error occurred")
                }
            }
        }
        val allSubnets = paginationHandler.fetchAllPages(perPage)
        logger.debug("Retrieved all ${allSubnets.size} subnets from Taostats")

        // Cache the fetched data by id, name and the entire list of subnet identities
        subnetCache.putAll(allSubnets.associateBy { cachePolicy.generateCacheKey("id", it.netuid) })
        subnetCache.putAll(allSubnets.associateBy { cachePolicy.generateCacheKey("name", it.subnetName) })
        logger.debug("Added each subnet to cache")
        subnetListCache.put(cacheKey, allSubnets)
        logger.debug("Added all subnets to cache with key $cacheKey")

        return allSubnets
    }

    override suspend fun getSubnetIdentityById(id: Int): SubnetIdentity {
        try {
            val cacheKey = cachePolicy.generateCacheKey("id", id)

            // Check if data is in cache first
            logger.debug("Retrieving subnet $id...")
            subnetCache.get(cacheKey)?.let {
                logger.debug("Found in cache using cachekey: $cacheKey")
                return it
            }

            // Cache miss - fetch from API
            logger.debug("Cache miss - fetching list of subnets...")
            val subnetIdentityList = getAllSubnetIdentities()

            return subnetIdentityList.find { it.netuid == id }
                ?: throw ApiException.NotFoundException("Subnet with id $id not found")

        } catch (e: Exception) {
            when (e) {
                is ApiException.NotFoundException -> throw e
                else -> throw Exception("Unknown error occurred", e)
            }
        }
    }

    override suspend fun getSubnetIdentityByName(name: String): SubnetIdentity {
        try {
            val cacheKey = cachePolicy.generateCacheKey("name", name)

            // Check if data is in cache first
            logger.debug("Retrieving subnet $name...")
            subnetCache.get(cacheKey)?.let {
                logger.debug("Found in cache using cachekey: $cacheKey")
                return it
            }

            // Cache miss - fetch from API
            logger.debug("Cache miss - fetching list of subnets...")
            val subnetIdentityList = getAllSubnetIdentities()

            return subnetIdentityList.find { it.subnetName == name }
                ?: throw ApiException.NotFoundException("Subnet with name $name not found")

        } catch (e: Exception) {
            when (e) {
                is ApiException.NotFoundException -> throw e
                else -> throw Exception("Unknown error occurred", e)
            }
        }
    }
}