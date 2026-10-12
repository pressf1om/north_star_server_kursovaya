package com.northstar.server.db

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb

object RoutesTable : Table("routes") {
    val id = integer("id").autoIncrement()
    val cityStart = varchar("city_start", 120)
    val cityEnd = varchar("city_end", 120)
    val createdDate = timestamp("created_date")
    val platonKm = integer("platon_km").nullable()
    val autodorKm = integer("autodor_km").nullable()
    val ollKm = integer("oll_km").nullable()
    val citiesOnRoute = jsonb(
        "cities_on_route",
        Json,
        ListSerializer(String.serializer()),
    ).nullable()

    override val primaryKey = PrimaryKey(id)
}
