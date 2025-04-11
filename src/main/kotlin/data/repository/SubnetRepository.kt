package data.repository

import data.model.taostats.subnet.SubnetIdentity

/**
 * Repository interfae for managing subnet related operations through Taostat API
 */
interface SubnetRepository {
    /**
     * Get all subnet identities
     *
     * @param perPage number of items per page
     *
     * @return paginated response of subnet identity list
     */
    suspend fun getAllSubnetIdentities(perPage: Int = 50) : List<SubnetIdentity>
}