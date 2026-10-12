package com.northstar.server.applications

import com.northstar.server.auth.UserStatus
import com.northstar.server.users.ForbiddenException

class DeleteApplicationUseCase(
    private val applicationRepository: ApplicationRepository,
) {
    suspend fun execute(callerStatus: String, applicationId: Int) {
        if (callerStatus != UserStatus.ADMIN) {
            throw ForbiddenException()
        }
        if (!applicationRepository.deleteById(applicationId)) {
            throw ApplicationNotFoundException()
        }
    }
}
