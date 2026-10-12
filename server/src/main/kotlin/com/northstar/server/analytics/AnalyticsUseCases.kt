package com.northstar.server.analytics

import com.northstar.server.auth.UserStatus
import com.northstar.server.routes.RouteRepository
import com.northstar.server.routesettings.RouteSettingsRepository
import com.northstar.server.users.ForbiddenException

class ListAnalyticsUseCase(
    private val analyticsRepository: AnalyticsRepository,
    private val routeRepository: RouteRepository,
    private val routeSettingsRepository: RouteSettingsRepository,
) {
    suspend fun execute(callerStatus: String): AnalyticsListResponse {
        requireOfficeRole(callerStatus)
        val routes = routeRepository.listAll().associateBy { it.id }
        val settings = routeSettingsRepository.getCurrent()
        val items = analyticsRepository.listWithCosts(routes, settings).map { variant ->
            if (variant.finalCost == null && variant.routeId != null) {
                val route = routes[variant.routeId]
                if (route != null) {
                    variant.copy(
                        routeLabel = "${route.cityStart} — ${route.cityEnd}",
                        finalCost = TripCostCalculator.calculate(
                            platonKm = route.platonKm,
                            ollKm = route.ollKm,
                            platonCost = settings.platonCost,
                            driverSalary = settings.driverSalary,
                            autodorCost = variant.cost,
                            fuelCost = variant.fuelCost,
                        ),
                    )
                } else {
                    variant
                }
            } else {
                variant
            }
        }
        return AnalyticsListResponse(items)
    }
}

class CreateAnalyticsVariantUseCase(
    private val analyticsRepository: AnalyticsRepository,
    private val routeRepository: RouteRepository,
    private val routeSettingsRepository: RouteSettingsRepository,
) {
    suspend fun execute(callerStatus: String, request: CreateAnalyticsVariantRequest): AnalyticsVariantDto {
        requireOfficeRole(callerStatus)
        val route = routeRepository.findById(request.routeId) ?: throw RouteNotFoundException()
        val created = analyticsRepository.create(request, route)
        val settings = routeSettingsRepository.getCurrent()
        return created.copy(
            routeLabel = "${route.cityStart} — ${route.cityEnd}",
            finalCost = TripCostCalculator.calculate(
                platonKm = route.platonKm,
                ollKm = route.ollKm,
                platonCost = settings.platonCost,
                driverSalary = settings.driverSalary,
                autodorCost = request.cost,
                fuelCost = request.fuelCost,
            ),
        )
    }
}

class ClearAnalyticsUseCase(
    private val analyticsRepository: AnalyticsRepository,
) {
    suspend fun execute(callerStatus: String) {
        requireOfficeRole(callerStatus)
        analyticsRepository.clearAll()
    }
}

private fun requireOfficeRole(callerStatus: String) {
    if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
        throw ForbiddenException()
    }
}
