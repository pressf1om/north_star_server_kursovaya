package com.northstar.server.db

import org.jetbrains.exposed.sql.Table

object SettingsForRoutesTable : Table("settings_for_routes") {
    val id = integer("id").autoIncrement()
    val driverSalary = double("driver_salary").nullable()
    val platonCost = double("platon_cost").nullable()

    override val primaryKey = PrimaryKey(id)
}
