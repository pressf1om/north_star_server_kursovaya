package com.northstar.server.routes

import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    val id: Int,
    val cityStart: String,
    val cityEnd: String,
    val platonKm: Int? = null,
    val autodorKm: Int? = null,
    val ollKm: Int? = null,
    val citiesOnRoute: List<String> = emptyList(),
)

@Serializable
data class RoutesListResponse(
    val routes: List<RouteDto>,
)

@Serializable
data class CreateRouteRequest(
    val cityStart: String,
    val cityEnd: String,
    val platonKm: Int? = null,
    val autodorKm: Int? = null,
    val ollKm: Int? = null,
    /** Города через запятую, точку с запятой или пробел */
    val citiesOnRoute: String = "",
)
