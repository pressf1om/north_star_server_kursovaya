package com.northstar.server.analytics

import com.northstar.server.auth.ValidationException
import com.northstar.server.db.RoutesTable
import com.northstar.server.db.TempBaseForAnalyticsTable
import com.northstar.server.routes.RouteDto
import com.northstar.server.routesettings.RouteSettingsDto
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface AnalyticsRepository {
    suspend fun listWithCosts(
        routesById: Map<Int, RouteDto>,
        settings: RouteSettingsDto,
    ): List<AnalyticsVariantDto>

    suspend fun create(request: CreateAnalyticsVariantRequest, route: RouteDto): AnalyticsVariantDto

    suspend fun clearAll()
}

class ExposedAnalyticsRepository : AnalyticsRepository {

    override suspend fun listWithCosts(
        routesById: Map<Int, RouteDto>,
        settings: RouteSettingsDto,
    ): List<AnalyticsVariantDto> = newSuspendedTransaction(Dispatchers.IO) {
        TempBaseForAnalyticsTable.selectAll()
            .orderBy(TempBaseForAnalyticsTable.id, SortOrder.ASC)
            .map { it.toDto(routesById, settings) }
    }

    override suspend fun create(
        request: CreateAnalyticsVariantRequest,
        route: RouteDto,
    ): AnalyticsVariantDto = newSuspendedTransaction(Dispatchers.IO) {
        validateRequest(request)
        val id = TempBaseForAnalyticsTable.insert {
            it[numberOfRoad] = request.numberOfRoad
            it[dateStart] = request.dateStart.trim()
            it[dateEnd] = request.dateEnd.trim()
            it[routeId] = request.routeId
            it[cost] = request.cost
            it[fuelCost] = request.fuelCost
        }[TempBaseForAnalyticsTable.id]

        AnalyticsVariantDto(
            id = id,
            numberOfRoad = request.numberOfRoad,
            dateStart = request.dateStart.trim(),
            dateEnd = request.dateEnd.trim(),
            routeId = request.routeId,
            routeLabel = "${route.cityStart} — ${route.cityEnd}",
            cost = request.cost,
            fuelCost = request.fuelCost,
            finalCost = null,
        )
    }

    override suspend fun clearAll() {
        newSuspendedTransaction(Dispatchers.IO) {
            TempBaseForAnalyticsTable.deleteAll()
        }
    }

    private fun ResultRow.toDto(
        routesById: Map<Int, RouteDto>,
        settings: RouteSettingsDto,
    ): AnalyticsVariantDto {
        val routeId = this[TempBaseForAnalyticsTable.routeId]
        val route = routeId?.let { routesById[it] }
        val routeLabel = route?.let { "${it.cityStart} — ${it.cityEnd}" }
        val finalCost = if (route != null) {
            TripCostCalculator.calculate(
                platonKm = route.platonKm,
                ollKm = route.ollKm,
                platonCost = settings.platonCost,
                driverSalary = settings.driverSalary,
                autodorCost = this[TempBaseForAnalyticsTable.cost],
                fuelCost = this[TempBaseForAnalyticsTable.fuelCost],
            )
        } else {
            null
        }
        return AnalyticsVariantDto(
            id = this[TempBaseForAnalyticsTable.id],
            numberOfRoad = this[TempBaseForAnalyticsTable.numberOfRoad],
            dateStart = this[TempBaseForAnalyticsTable.dateStart],
            dateEnd = this[TempBaseForAnalyticsTable.dateEnd],
            routeId = routeId,
            routeLabel = routeLabel,
            cost = this[TempBaseForAnalyticsTable.cost],
            fuelCost = this[TempBaseForAnalyticsTable.fuelCost],
            finalCost = finalCost,
        )
    }

    companion object {
        fun validateRequest(request: CreateAnalyticsVariantRequest) {
            if (request.numberOfRoad <= 0) {
                throw ValidationException("Укажите номер рейса")
            }
            if (request.dateStart.trim().isEmpty() || request.dateEnd.trim().isEmpty()) {
                throw ValidationException("Укажите даты начала и конца")
            }
            if (request.routeId <= 0) {
                throw ValidationException("Выберите маршрут")
            }
            if (request.cost < 0 || request.fuelCost < 0) {
                throw ValidationException("Стоимости не могут быть отрицательными")
            }
        }
    }
}

class RouteNotFoundException : RuntimeException("Маршрут не найден")
