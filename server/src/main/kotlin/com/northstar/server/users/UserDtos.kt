package com.northstar.server.users

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRoleRequest(
    val status: String,
    val carId: Int? = null,
)
