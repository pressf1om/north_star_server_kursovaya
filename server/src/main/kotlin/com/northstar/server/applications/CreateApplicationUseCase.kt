package com.northstar.server.applications

import com.northstar.server.auth.UserStatus
import com.northstar.server.users.ForbiddenException

class CreateApplicationUseCase(
    private val applicationRepository: ApplicationRepository,
) {
    suspend fun execute(callerStatus: String, request: CreateApplicationRequest): ApplicationDto {
        if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }
        return applicationRepository.createAssigned(request)
    }
}
