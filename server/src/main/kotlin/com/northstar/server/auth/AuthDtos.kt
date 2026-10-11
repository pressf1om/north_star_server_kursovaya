package com.northstar.server.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)

@Serializable
data class UserPublicDto(
    val id: Int,
    val email: String,
    val username: String,
    val status: String,
    val carNumber: String? = null,
    val carModel: String? = null,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserPublicDto,
)

@Serializable
data class MeResponse(
    val user: UserPublicDto,
    val authenticated: Boolean = true,
)

@Serializable
data class MessageResponse(
    val message: String,
)
