package com.northstar.server.applications

import com.northstar.server.auth.UserAuthRepository
import com.northstar.server.auth.UserStatus
import com.northstar.server.auth.ValidationException
import com.northstar.server.users.ForbiddenException

class ListCompletedApplicationsUseCase(
    private val completedApplicationRepository: CompletedApplicationRepository,
    private val userAuthRepository: UserAuthRepository,
) {
    suspend fun listAll(callerStatus: String): ApplicationsListResponse {
        if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }
        return ApplicationsListResponse(completedApplicationRepository.listAll())
    }

    suspend fun listByCar(
        callerStatus: String,
        callerUserId: Int,
        carNumber: String,
    ): ApplicationsListResponse {
        val number = carNumber.trim()
        if (number.isEmpty()) {
            throw ValidationException("Укажите номер машины")
        }

        if (callerStatus == UserStatus.DRIVER) {
            val car = userAuthRepository.findCarByDriverId(callerUserId)
            if (car?.carNumber != number) {
                throw ForbiddenException()
            }
        } else if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }

        return ApplicationsListResponse(completedApplicationRepository.listByCar(number))
    }
}
