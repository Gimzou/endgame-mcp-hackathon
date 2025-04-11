package data.util

import data.model.taostats.PaginatedResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A utility class for handling pagination in API requests.
 *
 * @param fetchPage A suspend function that takes page number, per page count, and order as parameters and returns a PaginatedResponse of type T.
 * @constructor Creates a PaginationHandler instance with the provided fetchPage function.
 */
class PaginationHandler<T>(
    private val fetchPage : suspend(Int, Int, String) -> PaginatedResponse<T>
) {
    suspend fun fetchAllPages(perPage: Int = 100, order: String = "") : List<T> {
        val allItems = mutableListOf<T>()
        var currentPage = 1
        var hasNextPage = true

        while (hasNextPage) {
            val response = fetchPage(currentPage, perPage, order)
            allItems.addAll(response.data)

            hasNextPage = response.pagination.nextPage != null
            currentPage = response.pagination.nextPage ?: break
        }

        return allItems
    }

    fun fetchPageFlow(perPage: Int = 100, order: String = "") : Flow<T> = flow {
        var currentPage = 1
        var hasNextPage = true

        while (hasNextPage) {
            val response = fetchPage(currentPage, perPage, order)
            response.data.forEach { emit(it) }

            hasNextPage = response.pagination.nextPage != null
            currentPage = response.pagination.nextPage ?: break
        }
    }
}