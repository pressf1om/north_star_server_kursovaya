package com.northstar.server.applications

import kotlinx.serialization.Serializable

@Serializable
data class UpdateApplicationStatusRequest(
    val status: String,
)

@Serializable
data class UpdateApplicationStatusResponse(
    val archived: Boolean,
    val application: ApplicationDto? = null,
)

@Serializable
data class CreateApplicationRequest(
    val coordStart: String,
    val coordEnd: String,
    val dateOfStart: String,
    val weight: String,
    val carNow: String,
)

@Serializable
data class ApplicationsListResponse(
    val applications: List<ApplicationDto>,
)

@Serializable
data class ApplicationDto(
    val id: Int,
    val coordStart: String,
    val coordEnd: String,
    val dateOfStart: String,
    val dateOfEnd: String? = null,
    val status: String? = null,
    val weight: String? = null,
    val carNow: String? = null,
)
