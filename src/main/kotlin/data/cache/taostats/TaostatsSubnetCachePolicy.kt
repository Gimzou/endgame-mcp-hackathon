package data.cache.taostats

import data.cache.CachePolicy
import data.model.taostats.subnet.SubnetIdentity

class TaostatsSubnetCachePolicy : CachePolicy<SubnetIdentity> {
    override fun shouldCache(data: SubnetIdentity): Boolean {
        // always cache subnet identity
        return true
    }

    override fun getCacheTtl(data: SubnetIdentity): Long {
        // no discrimination between subnets
        return 60 * 60 * 1 // 1 hour
    }

    override fun generateCacheKey(vararg params: Any): String {
        // create a unique key based on the parameters
        require(params.size == 2) { "TaostatsSubnetCachePolicy requires 2 parameters, a keyType and it's value" }
        val keyType = params[0].toString()
        val keyValue = params[1].toString()
        return "taostats:subnetIdentity:$keyType:$keyValue"
    }
}