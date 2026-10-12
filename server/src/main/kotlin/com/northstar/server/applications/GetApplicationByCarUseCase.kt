package com.northstar.server.applications

import com.northstar.server.auth.UserAuthRepository
import com.northstar.server.auth.UserStatus
import com.northstar.server.users.ForbiddenException

class GetApplicationByCarUseCase(
    private val applicationRepository: ApplicationRepository,
    private val userAuthRepository: UserAuthRepository,
) {
    suspend fun execute(
        callerStatus: String,
        callerUserId: Int,
        carNumber: String,
    ): ApplicationDto {
        val number = carNumber.trim()
        if (number.isEmpty()) {
            throw ApplicationNotFoundException()
        }

        if (callerStatus == UserStatus.DRIVER) {
            val car = userAuthRepository.findCarByDriverId(callerUserId)
            if (car?.carNumber != number) {
                throw ForbiddenException()
            }
        } else if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }

        return applicationRepository.findByCarNumber(number)
            ?: throw ApplicationNotFoundException()
    }
}
