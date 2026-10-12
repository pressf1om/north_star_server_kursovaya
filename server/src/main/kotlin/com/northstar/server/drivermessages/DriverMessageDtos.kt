package com.northstar.server.drivermessages

import kotlinx.serialization.Serializable

@Serializable
data class CreateDriverMessageRequest(
    val message: String,
    val carNumber: String,
)

@Serializable
data class DriverMessageDto(
    val id: Int,
    val message: String,
    val carNumber: String,
)

@Serializable
data class DriverMessagesListResponse(
    val messages: List<DriverMessageDto>,
)
