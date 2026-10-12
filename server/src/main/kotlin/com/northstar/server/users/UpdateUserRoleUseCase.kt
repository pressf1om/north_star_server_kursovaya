package com.northstar.server.users

import com.northstar.server.auth.UserPublicDto
import com.northstar.server.auth.UserStatus
import com.northstar.server.auth.ValidationException
import com.northstar.server.auth.UserNotFoundException
import com.northstar.server.cars.CarAdminRepository

class UpdateUserRoleUseCase(
    private val userAdminRepository: UserAdminRepository,
    private val carAdminRepository: CarAdminRepository,
) {
    suspend fun execute(
        callerStatus: String,
        userId: Int,
        request: UpdateUserRoleRequest,
    ): UserPublicDto {
        if (callerStatus != UserStatus.ADMIN) {
            throw ForbiddenException()
        }

        val status = request.status.trim()
        if (status != UserStatus.DISPATCHER && status != UserStatus.DRIVER) {
            throw ValidationException("Можно выдать только роль Диспетчер или Водитель")
        }

        if (status == UserStatus.DRIVER) {
            val carId = request.carId
                ?: throw ValidationException("Для водителя нужно выбрать машину")
            if (carAdminRepository.findById(carId) == null) {
                throw ValidationException("Машина не найдена")
            }
        }

        return userAdminRepository.updateRole(
            userId = userId,
            status = status,
            carId = request.carId,
        )
    }
}
