package com.northstar.server.analytics

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsVariantDto(
    val id: Int,
    val numberOfRoad: Int? = null,
    val dateStart: String? = null,
    val dateEnd: String? = null,
    val routeId: Int? = null,
    val routeLabel: String? = null,
    val cost: Int? = null,
    val fuelCost: Int? = null,
    val finalCost: Double? = null,
)

@Serializable
data class AnalyticsListResponse(
    val items: List<AnalyticsVariantDto>,
)

@Serializable
data class CreateAnalyticsVariantRequest(
    val numberOfRoad: Int,
    val dateStart: String,
    val dateEnd: String,
    val routeId: Int,
    val cost: Int,
    val fuelCost: Int,
)
