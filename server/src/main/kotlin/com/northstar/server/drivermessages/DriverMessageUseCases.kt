package com.northstar.server.drivermessages

import com.northstar.server.auth.UserAuthRepository
import com.northstar.server.auth.UserStatus
import com.northstar.server.auth.ValidationException
import com.northstar.server.users.ForbiddenException

class CreateDriverMessageUseCase(
    private val driverMessageRepository: DriverMessageRepository,
    private val userAuthRepository: UserAuthRepository,
) {
    suspend fun execute(
        callerStatus: String,
        callerUserId: Int,
        request: CreateDriverMessageRequest,
    ): DriverMessageDto {
        if (callerStatus != UserStatus.DRIVER) {
            throw ForbiddenException()
        }

        val text = request.message.trim()
        if (text.isEmpty()) {
            throw ValidationException("Введите текст сообщения")
        }
        if (text.length > 5000) {
            throw ValidationException("Сообщение слишком длинное")
        }

        val carNumber = request.carNumber.trim()
        if (carNumber.isEmpty()) {
            throw ValidationException("Укажите номер машины")
        }

        val driverCar = userAuthRepository.findCarByDriverId(callerUserId)
            ?: throw ValidationException("Машина не привязана к учётке")
        if (driverCar.carNumber != carNumber) {
            throw ForbiddenException()
        }

        return driverMessageRepository.create(text, carNumber)
    }
}

class ListDriverMessagesUseCase(
    private val driverMessageRepository: DriverMessageRepository,
) {
    suspend fun execute(callerStatus: String, carNumber: String?): DriverMessagesListResponse {
        if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }
        val items = if (carNumber.isNullOrBlank()) {
            driverMessageRepository.listAll()
        } else {
            driverMessageRepository.listByCar(carNumber.trim())
        }
        return DriverMessagesListResponse(items)
    }
}
