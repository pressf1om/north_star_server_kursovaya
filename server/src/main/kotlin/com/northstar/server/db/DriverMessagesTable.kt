package com.northstar.server.db

import org.jetbrains.exposed.sql.Table

object DriverMessagesTable : Table("driver_messages") {
    val id = integer("id").autoIncrement()
    val message = varchar("message", 5000)
    val carNumber = varchar("car_number", 20)

    override val primaryKey = PrimaryKey(id)
}
