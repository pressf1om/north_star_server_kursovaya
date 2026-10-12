package com.northstar.server.applications

import com.northstar.server.auth.UserStatus
import com.northstar.server.users.ForbiddenException

class ListApplicationsUseCase(
    private val applicationRepository: ApplicationRepository,
) {
    suspend fun execute(callerStatus: String): ApplicationsListResponse {
        if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }
        return ApplicationsListResponse(applicationRepository.listCurrent())
    }
}
