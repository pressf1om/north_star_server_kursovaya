package com.northstar.server.users

import com.northstar.server.auth.UserPublicDto
import com.northstar.server.auth.UserStatus

class ForbiddenException : RuntimeException("Недостаточно прав")

class ListUsersUseCase(
    private val userAdminRepository: UserAdminRepository,
) {
    suspend fun execute(callerStatus: String): UsersListResponse {
        if (callerStatus != UserStatus.ADMIN) {
            throw ForbiddenException()
        }
        return UsersListResponse(userAdminRepository.listAllPublic())
    }
}

@kotlinx.serialization.Serializable
data class UsersListResponse(
    val users: List<UserPublicDto>,
)
