package com.northstar.server.routesettings

import kotlinx.serialization.Serializable

@Serializable
data class RouteSettingsDto(
    val driverSalary: Double? = null,
    val platonCost: Double? = null,
)

@Serializable
data class UpdateRouteSettingsRequest(
    val driverSalary: Double,
    val platonCost: Double,
)
