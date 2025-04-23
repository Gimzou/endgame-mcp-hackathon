package domain.repository.taostats

import data.model.taostats.subnet.SubnetIdentity

/**
 * Interface for managing subnet related operations through Taostat API
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

    /**
     * Get subnet identity by id
     *
     * @param id subnet identity id
     *
     * @return subnet identity
     */
    suspend fun getSubnetIdentityById(id: Int) : SubnetIdentity

    /**
     * Get subnet identity by name
     *
     * @param name subnet identity name
     *
     * @return subnet identity
     */
    suspend fun getSubnetIdentityByName(name: String) : SubnetIdentity
}