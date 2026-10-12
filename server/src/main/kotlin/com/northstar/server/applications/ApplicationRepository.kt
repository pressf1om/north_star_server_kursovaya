package com.northstar.server.applications

import com.northstar.server.auth.ValidationException
import com.northstar.server.cars.CarStatus
import com.northstar.server.db.ApplicationsTable
import com.northstar.server.db.CarsTable
import com.northstar.server.db.CompletedApplicationsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class CarNotAvailableException : RuntimeException("Машина недоступна")

interface ApplicationRepository {
    suspend fun createAssigned(request: CreateApplicationRequest): ApplicationDto
    suspend fun listCurrent(): List<ApplicationDto>
    suspend fun findById(id: Int): ApplicationDto?
    suspend fun findByCarNumber(carNumber: String): ApplicationDto?
    suspend fun deleteById(id: Int): Boolean
    suspend fun updateStatus(id: Int, newStatus: String): ApplicationDto?
}

class ExposedApplicationRepository : ApplicationRepository {

    override suspend fun findById(id: Int): ApplicationDto? = newSuspendedTransaction(Dispatchers.IO) {
        ApplicationsTable.selectAll()
            .where { ApplicationsTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toApplicationDto()
    }

    override suspend fun findByCarNumber(carNumber: String): ApplicationDto? =
        newSuspendedTransaction(Dispatchers.IO) {
            ApplicationsTable.selectAll()
                .where { ApplicationsTable.carNow eq carNumber.trim() }
                .limit(1)
                .firstOrNull()
                ?.toApplicationDto()
        }

    override suspend fun deleteById(id: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val row = ApplicationsTable.selectAll()
            .where { ApplicationsTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?: return@newSuspendedTransaction false

        val carNumber = row[ApplicationsTable.carNow]
        ApplicationsTable.deleteWhere { ApplicationsTable.id eq id }
        if (!carNumber.isNullOrBlank()) {
            CarsTable.update({ CarsTable.carNumber eq carNumber }) {
                it[CarsTable.status] = CarStatus.FREE
            }
        }
        true
    }

    override suspend fun updateStatus(id: Int, newStatus: String): ApplicationDto? =
        newSuspendedTransaction(Dispatchers.IO) {
            val row = ApplicationsTable.selectAll()
                .where { ApplicationsTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?: return@newSuspendedTransaction null

            val carNumber = row[ApplicationsTable.carNow]

            if (newStatus == ApplicationStatus.FREE) {
                CompletedApplicationsTable.insert {
                    it[coordStart] = row[ApplicationsTable.coordStart]
                    it[coordEnd] = row[ApplicationsTable.coordEnd]
                    it[dateOfStart] = row[ApplicationsTable.dateOfStart]
                    it[dateOfEnd] = row[ApplicationsTable.dateOfEnd]
                    it[status] = ApplicationStatus.FREE
                    it[weight] = row[ApplicationsTable.weight]
                    it[carNow] = carNumber
                }
                ApplicationsTable.deleteWhere { ApplicationsTable.id eq id }
                if (!carNumber.isNullOrBlank()) {
                    CarsTable.update({ CarsTable.carNumber eq carNumber }) {
                        it[CarsTable.status] = CarStatus.FREE
                    }
                }
                return@newSuspendedTransaction null
            }

            ApplicationsTable.update({ ApplicationsTable.id eq id }) {
                it[ApplicationsTable.status] = newStatus
            }
            if (!carNumber.isNullOrBlank()) {
                CarsTable.update({ CarsTable.carNumber eq carNumber }) {
                    it[CarsTable.status] = newStatus
                }
            }

            ApplicationsTable.selectAll()
                .where { ApplicationsTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toApplicationDto()
        }

    override suspend fun listCurrent(): List<ApplicationDto> = newSuspendedTransaction(Dispatchers.IO) {
        ApplicationsTable.selectAll()
            .orderBy(ApplicationsTable.id, SortOrder.ASC)
            .map { it.toApplicationDto() }
    }

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

    private fun ResultRow.toApplicationDto() = ApplicationDto(
        id = this[ApplicationsTable.id],
        coordStart = this[ApplicationsTable.coordStart],
        coordEnd = this[ApplicationsTable.coordEnd],
        dateOfStart = this[ApplicationsTable.dateOfStart],
        dateOfEnd = this[ApplicationsTable.dateOfEnd],
        status = this[ApplicationsTable.status],
        weight = this[ApplicationsTable.weight],
        carNow = this[ApplicationsTable.carNow],
    )
}
