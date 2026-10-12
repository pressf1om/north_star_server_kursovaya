package com.northstar.server.applications

import com.northstar.server.auth.ValidationException
import com.northstar.server.cars.CarStatus
import com.northstar.server.db.ApplicationsTable
import com.northstar.server.db.CarsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class CarNotAvailableException : RuntimeException("Машина недоступна")

interface ApplicationRepository {
    suspend fun createAssigned(request: CreateApplicationRequest): ApplicationDto
}

class ExposedApplicationRepository : ApplicationRepository {

    override suspend fun createAssigned(request: CreateApplicationRequest): ApplicationDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val carNumber = request.carNow.trim()
            val carRow = CarsTable.selectAll()
                .where { CarsTable.carNumber eq carNumber }
                .limit(1)
                .firstOrNull()
                ?: throw ValidationException("Машина не найдена")

            val carStatus = carRow[CarsTable.status]
            if (carStatus != CarStatus.FREE) {
                throw CarNotAvailableException()
            }

            val coordStart = request.coordStart.trim()
            val coordEnd = request.coordEnd.trim()
            val dateOfStart = request.dateOfStart.trim()
            val weight = request.weight.trim()

            validateCoordinate(coordStart, "начала")
            validateCoordinate(coordEnd, "конца")
            if (dateOfStart.isEmpty() || weight.isEmpty() || carNumber.isEmpty()) {
                throw ValidationException("Заполните все поля")
            }

            val id = ApplicationsTable.insert {
                it[ApplicationsTable.coordStart] = coordStart
                it[ApplicationsTable.coordEnd] = coordEnd
                it[ApplicationsTable.dateOfStart] = dateOfStart
                it[ApplicationsTable.dateOfEnd] = null
                it[ApplicationsTable.status] = ApplicationStatus.ASSIGNED
                it[ApplicationsTable.weight] = weight
                it[ApplicationsTable.carNow] = carNumber
            }[ApplicationsTable.id]

            CarsTable.update({ CarsTable.carNumber eq carNumber }) {
                it[CarsTable.status] = ApplicationStatus.ASSIGNED
            }

            ApplicationDto(
                id = id,
                coordStart = coordStart,
                coordEnd = coordEnd,
                dateOfStart = dateOfStart,
                dateOfEnd = null,
                status = ApplicationStatus.ASSIGNED,
                weight = weight,
                carNow = carNumber,
            )
        }

    private fun validateCoordinate(raw: String, label: String) {
        if (raw.isEmpty()) {
            throw ValidationException("Укажите координаты $label")
        }
        val parts = raw.split(',').map { it.trim() }
        if (parts.size != 2 || parts[0].toDoubleOrNull() == null || parts[1].toDoubleOrNull() == null) {
            throw ValidationException("Координаты $label: формат широта,долгота")
        }
    }
}
