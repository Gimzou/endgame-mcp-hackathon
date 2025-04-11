package data.repository

import data.model.taostats.subnet.SubnetIdentity
import data.network.TaostatsApi
import data.util.PaginationHandler

class SubnetRepositoryImpl(
    private val taostatsApi : TaostatsApi
) : SubnetRepository {

    override suspend fun getAllSubnetIdentities(perPage: Int): List<SubnetIdentity> {
        val paginationHandler = PaginationHandler { page, itemsPerPage, _ ->
            taostatsApi.getSubnetIdentity(page, itemsPerPage)
        }
        val allResults = paginationHandler.fetchAllPages(perPage)
        return allResults
    }
}