package com.northstar.server.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object CarsTable : Table("cars") {
    val id = integer("id").autoIncrement()
    val carNumber = varchar("car_number", 120).uniqueIndex()
    val model = varchar("model", 120)
    val createdDate = timestamp("created_date")
    val status = varchar("status", 120).nullable()
    val carDriverId = integer("car_driver_id").nullable()

    override val primaryKey = PrimaryKey(id)
}
