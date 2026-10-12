package com.northstar.server.routesettings

import com.northstar.server.auth.UserStatus
import com.northstar.server.users.ForbiddenException

class GetRouteSettingsUseCase(
    private val routeSettingsRepository: RouteSettingsRepository,
) {
    suspend fun execute(callerStatus: String): RouteSettingsDto {
        if (callerStatus != UserStatus.ADMIN) {
            throw ForbiddenException()
        }
        return routeSettingsRepository.getCurrent()
    }
}

class UpdateRouteSettingsUseCase(
    private val routeSettingsRepository: RouteSettingsRepository,
) {
    suspend fun execute(callerStatus: String, request: UpdateRouteSettingsRequest): RouteSettingsDto {
        if (callerStatus != UserStatus.ADMIN) {
            throw ForbiddenException()
        }
        return routeSettingsRepository.replace(request)
    }
}
