package com.northstar.server.cars

import kotlinx.serialization.Serializable

@Serializable
data class CarPublicDto(
    val id: Int,
    val carNumber: String,
    val model: String,
    val status: String? = null,
)

@Serializable
data class CarsListResponse(
    val cars: List<CarPublicDto>,
)

@Serializable
data class CreateCarRequest(
    val carNumber: String,
    val model: String,
)
