package com.northstar.server.drivermessages

import com.northstar.server.auth.UserAuthRepository
import com.northstar.server.auth.UserStatus
import com.northstar.server.auth.ValidationException
import com.northstar.server.users.ForbiddenException

class DriverMessageNotFoundException : RuntimeException("Сообщение не найдено")

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
    private val userAuthRepository: UserAuthRepository,
) {
    suspend fun execute(
        callerStatus: String,
        callerUserId: Int,
        carNumber: String?,
    ): DriverMessagesListResponse {
        when (callerStatus) {
            UserStatus.ADMIN, UserStatus.DISPATCHER -> {
                val items = if (carNumber.isNullOrBlank()) {
                    driverMessageRepository.listAll()
                } else {
                    driverMessageRepository.listByCar(carNumber.trim())
                }
                return DriverMessagesListResponse(items)
            }
            UserStatus.DRIVER -> {
                val driverCarNumber = userAuthRepository.findCarByDriverId(callerUserId)?.carNumber
                    ?: throw ForbiddenException()
                val requested = carNumber?.trim().orEmpty()
                if (requested.isNotEmpty() && requested != driverCarNumber) {
                    throw ForbiddenException()
                }
                return DriverMessagesListResponse(
                    driverMessageRepository.listByCar(driverCarNumber),
                )
            }
            else -> throw ForbiddenException()
        }
    }
}

class ReplyDriverMessageUseCase(
    private val driverMessageRepository: DriverMessageRepository,
) {
    suspend fun execute(
        callerStatus: String,
        messageId: Int,
        request: ReplyDriverMessageRequest,
    ): DriverMessageDto {
        if (callerStatus != UserStatus.ADMIN && callerStatus != UserStatus.DISPATCHER) {
            throw ForbiddenException()
        }
        val text = request.reply.trim()
        if (text.isEmpty()) {
            throw ValidationException("Введите ответ")
        }
        if (text.length > 5000) {
            throw ValidationException("Ответ слишком длинный")
        }
        return driverMessageRepository.reply(messageId, text)
            ?: throw DriverMessageNotFoundException()
    }
}
