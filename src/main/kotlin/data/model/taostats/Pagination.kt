package data.model.taostats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("total_items") val totalItems: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("next_page") val nextPage: Int?,
    @SerialName("prev_page") val prevPage: Int?
)


