package com.northstar.server.db

import org.jetbrains.exposed.sql.Table

object TempBaseForAnalyticsTable : Table("temp_base_for_analytics") {
    val id = integer("id").autoIncrement()
    val numberOfRoad = integer("number_of_road").nullable()
    val dateStart = varchar("date_start", 120).nullable()
    val dateEnd = varchar("date_end", 120).nullable()
    val routeId = integer("route_id").nullable()
    val cost = integer("cost").nullable()
    val fuelCost = integer("fuel_cost").nullable()

    override val primaryKey = PrimaryKey(id)
}
