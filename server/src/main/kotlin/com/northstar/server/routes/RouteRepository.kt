package com.northstar.server.routes

import com.northstar.server.auth.ValidationException
import com.northstar.server.db.RoutesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

interface RouteRepository {
    suspend fun listAll(): List<RouteDto>
    suspend fun create(request: CreateRouteRequest): RouteDto
}

class ExposedRouteRepository : RouteRepository {

    override suspend fun listAll(): List<RouteDto> = newSuspendedTransaction(Dispatchers.IO) {
        RoutesTable.selectAll()
            .orderBy(RoutesTable.id, SortOrder.ASC)
            .map { it.toRouteDto() }
    }

    override suspend fun create(request: CreateRouteRequest): RouteDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val cityStart = request.cityStart.trim()
            val cityEnd = request.cityEnd.trim()
            if (cityStart.isEmpty() || cityEnd.isEmpty()) {
                throw ValidationException("Укажите города начала и конца")
            }

            val cities = parseCitiesOnRoute(request.citiesOnRoute)
            val id = RoutesTable.insert {
                it[RoutesTable.cityStart] = cityStart
                it[RoutesTable.cityEnd] = cityEnd
                it[RoutesTable.createdDate] = Instant.now()
                it[RoutesTable.platonKm] = request.platonKm
                it[RoutesTable.autodorKm] = request.autodorKm
                it[RoutesTable.ollKm] = request.ollKm
                it[RoutesTable.citiesOnRoute] = cities.ifEmpty { null }
            }[RoutesTable.id]

            RouteDto(
                id = id,
                cityStart = cityStart,
                cityEnd = cityEnd,
                platonKm = request.platonKm,
                autodorKm = request.autodorKm,
                ollKm = request.ollKm,
                citiesOnRoute = cities,
            )
        }

    private fun ResultRow.toRouteDto() = RouteDto(
        id = this[RoutesTable.id],
        cityStart = this[RoutesTable.cityStart],
        cityEnd = this[RoutesTable.cityEnd],
        platonKm = this[RoutesTable.platonKm],
        autodorKm = this[RoutesTable.autodorKm],
        ollKm = this[RoutesTable.ollKm],
        citiesOnRoute = this[RoutesTable.citiesOnRoute] ?: emptyList(),
    )

    companion object {
        fun parseCitiesOnRoute(raw: String): List<String> =
            raw.split(Regex("[,;\\s]+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
    }
}
