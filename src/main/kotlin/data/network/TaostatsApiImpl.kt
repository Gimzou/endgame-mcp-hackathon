package data.network

import data.model.taostats.PaginatedResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import data.model.taostats.subnet.SubnetIdentity

class TaostatsApiImpl(
    private val httpClient: HttpClient
) : TaostatsApi {

    override suspend fun getSubnetIdentity(page: Int, perPage: Int): PaginatedResponse<SubnetIdentity> {
        return httpClient.get("subnet/identity/v1") {
            parameter("page", page)
            parameter("limit", perPage)
        }.body()
    }
}