package com.northstar.server.applications

import com.northstar.server.auth.UserAuthRepository
import com.northstar.server.auth.UserStatus
import com.northstar.server.auth.ValidationException
import com.northstar.server.users.ForbiddenException

class InvalidStatusTransitionException : RuntimeException("Недопустимый переход статуса")

class UpdateApplicationStatusUseCase(
    private val applicationRepository: ApplicationRepository,
    private val userAuthRepository: UserAuthRepository,
) {
    suspend fun execute(
        callerStatus: String,
        callerUserId: Int,
        applicationId: Int,
        request: UpdateApplicationStatusRequest,
    ): ApplicationDto? {
        val newStatus = request.status.trim()
        if (!ApplicationStatus.isAllowedCode(newStatus)) {
            throw ValidationException("Статус должен быть от 1 до 9")
        }

        val current = applicationRepository.findById(applicationId)
            ?: throw ApplicationNotFoundException()

        if (callerStatus == UserStatus.DRIVER) {
            val car = userAuthRepository.findCarByDriverId(callerUserId)
            if (car?.carNumber != current.carNow) {
                throw ForbiddenException()
            }
            val expected = ApplicationStatus.driverNext(current.status)
            if (expected != newStatus) {
                throw InvalidStatusTransitionException()
            }
        } else if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }

        return applicationRepository.updateStatus(applicationId, newStatus)
    }
}
