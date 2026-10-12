package com.northstar.server.routes

import com.northstar.server.auth.UserStatus
import com.northstar.server.users.ForbiddenException

class ListRoutesUseCase(
    private val routeRepository: RouteRepository,
) {
    suspend fun execute(callerStatus: String): RoutesListResponse {
        if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }
        return RoutesListResponse(routeRepository.listAll())
    }
}

class CreateRouteUseCase(
    private val routeRepository: RouteRepository,
) {
    suspend fun execute(callerStatus: String, request: CreateRouteRequest): RouteDto {
        if (callerStatus != UserStatus.ADMIN) {
            throw ForbiddenException()
        }
        return routeRepository.create(request)
    }
}
