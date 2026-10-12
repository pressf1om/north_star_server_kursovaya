package com.northstar.server.db

import org.jetbrains.exposed.sql.Table

object ApplicationsTable : Table("applications") {
    val id = integer("id").autoIncrement()
    val coordStart = varchar("coord_start", 120)
    val coordEnd = varchar("coord_end", 120)
    val dateOfStart = varchar("date_of_start", 120)
    val dateOfEnd = varchar("date_of_end", 120).nullable()
    val status = varchar("status", 120).nullable()
    val weight = varchar("weight", 120).nullable()
    val carNow = varchar("car_now", 120).nullable()

    override val primaryKey = PrimaryKey(id)
}
