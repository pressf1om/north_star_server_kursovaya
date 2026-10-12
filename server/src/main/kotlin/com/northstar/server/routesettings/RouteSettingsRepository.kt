package com.northstar.server.routesettings

import com.northstar.server.auth.ValidationException
import com.northstar.server.db.SettingsForRoutesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface RouteSettingsRepository {
    suspend fun getCurrent(): RouteSettingsDto
    suspend fun replace(request: UpdateRouteSettingsRequest): RouteSettingsDto
}

class ExposedRouteSettingsRepository : RouteSettingsRepository {

    override suspend fun getCurrent(): RouteSettingsDto = newSuspendedTransaction(Dispatchers.IO) {
        val row = SettingsForRoutesTable.selectAll()
            .orderBy(SettingsForRoutesTable.id, SortOrder.DESC)
            .limit(1)
            .firstOrNull()
        if (row == null) {
            RouteSettingsDto()
        } else {
            RouteSettingsDto(
                driverSalary = row[SettingsForRoutesTable.driverSalary],
                platonCost = row[SettingsForRoutesTable.platonCost],
            )
        }
    }

    override suspend fun replace(request: UpdateRouteSettingsRequest): RouteSettingsDto =
        newSuspendedTransaction(Dispatchers.IO) {
            if (request.driverSalary < 0 || request.platonCost < 0) {
                throw ValidationException("Тарифы не могут быть отрицательными")
            }
            SettingsForRoutesTable.deleteAll()
            SettingsForRoutesTable.insert {
                it[driverSalary] = request.driverSalary
                it[platonCost] = request.platonCost
            }
            RouteSettingsDto(
                driverSalary = request.driverSalary,
                platonCost = request.platonCost,
            )
        }
}
