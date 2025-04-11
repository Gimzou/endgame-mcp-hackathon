package data.network

import data.model.taostats.PaginatedResponse
import data.model.taostats.subnet.SubnetIdentity

/**
 * Api for Taostats
 */
interface TaostatsApi {
    /**
     * Get subnet identity
     *
     * @param page page number
     * @param perPage number of items per page
     *
     * @return paginated response with subnet identity
     */
    suspend fun getSubnetIdentity(page: Int, perPage: Int) : PaginatedResponse<SubnetIdentity>
}