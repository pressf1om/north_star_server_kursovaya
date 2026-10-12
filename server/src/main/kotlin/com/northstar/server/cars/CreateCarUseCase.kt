package com.northstar.server.cars

import com.northstar.server.auth.UserStatus
import com.northstar.server.users.ForbiddenException

class CreateCarUseCase(
    private val carAdminRepository: CarAdminRepository,
) {
    suspend fun execute(callerStatus: String, request: CreateCarRequest): CarPublicDto {
        if (callerStatus != UserStatus.ADMIN) {
            throw ForbiddenException()
        }
        return carAdminRepository.create(request.carNumber, request.model)
    }
}
