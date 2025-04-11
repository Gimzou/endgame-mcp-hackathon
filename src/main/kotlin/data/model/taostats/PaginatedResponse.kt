package data.model.taostats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    @SerialName("pagination") val pagination: Pagination,
    val data: List<T>
)

