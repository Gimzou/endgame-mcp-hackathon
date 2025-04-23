package data.model.taostats.subnet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubnetIdentity(
    val netuid: Int,
    @SerialName("subnet_name") val subnetName: String,
    @SerialName("github_repo") val gitHubRepo: String?,
    @SerialName("subnet_contact") val subnetContact: String?,
    @SerialName("subnet_url") val subnetUrl: String?,
    val discord: String?,
    val description: String?,
    val additional: String?
)
