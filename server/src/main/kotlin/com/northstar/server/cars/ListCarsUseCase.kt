package com.northstar.server.cars

import com.northstar.server.auth.UserStatus
import com.northstar.server.users.ForbiddenException

class ListCarsUseCase(
    private val carAdminRepository: CarAdminRepository,
) {
    suspend fun execute(callerStatus: String): CarsListResponse {
        if (callerStatus != UserStatus.ADMIN) {
            throw ForbiddenException()
        }
        return CarsListResponse(carAdminRepository.listAll())
    }
}
