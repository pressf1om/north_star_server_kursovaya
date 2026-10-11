package com.northstar.server.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 200)
    val username = varchar("username", 120).uniqueIndex()
    val password = varchar("password", 255)
    val createdDate = timestamp("created_date")
    val status = varchar("status", 120)

    override val primaryKey = PrimaryKey(id)
}
