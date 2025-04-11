package data.model.taostats.subnet

import kotlinx.serialization.Serializable

@Serializable
data class Owner(
    val ss58: String,
    val hex: String
)
