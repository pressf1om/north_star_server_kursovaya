package com.northstar.server.cars

import com.northstar.server.db.CarsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class CarNumberAlreadyExistsException(val carNumber: String) : RuntimeException("Номер уже есть в парке")

interface CarAdminRepository {
    suspend fun listAll(status: String? = null): List<CarPublicDto>
    suspend fun findById(id: Int): CarPublicDto?
    suspend fun create(carNumber: String, model: String): CarPublicDto
    suspend fun clearDriverFromAllCars(driverUserId: Int)
    suspend fun assignDriver(carId: Int, driverUserId: Int)
}

class ExposedCarAdminRepository : CarAdminRepository {

    override suspend fun listAll(status: String?): List<CarPublicDto> = newSuspendedTransaction(Dispatchers.IO) {
        val base = if (status != null) {
            CarsTable.selectAll().where { CarsTable.status eq status }
        } else {
            CarsTable.selectAll()
        }
        base
            .orderBy(CarsTable.id, SortOrder.ASC)
            .map { row ->
                CarPublicDto(
                    id = row[CarsTable.id],
                    carNumber = row[CarsTable.carNumber],
                    model = row[CarsTable.model],
                    status = row[CarsTable.status],
                )
            }
    }

    override suspend fun create(carNumber: String, model: String): CarPublicDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val number = carNumber.trim()
            val carModel = model.trim()
            if (number.isEmpty() || carModel.isEmpty()) {
                throw com.northstar.server.auth.ValidationException("Укажите номер и модель")
            }
            val exists = CarsTable.selectAll()
                .where { CarsTable.carNumber eq number }
                .limit(1)
                .any()
            if (exists) {
                throw CarNumberAlreadyExistsException(number)
            }
            val id = CarsTable.insert {
                it[CarsTable.carNumber] = number
                it[CarsTable.model] = carModel
                it[CarsTable.createdDate] = Instant.now()
                it[CarsTable.status] = CarStatus.FREE
                it[CarsTable.carDriverId] = null
            }[CarsTable.id]
            CarPublicDto(
                id = id,
                carNumber = number,
                model = carModel,
                status = CarStatus.FREE,
            )
        }

    override suspend fun findById(id: Int): CarPublicDto? = newSuspendedTransaction(Dispatchers.IO) {
        CarsTable.selectAll()
            .where { CarsTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?.let { row ->
                CarPublicDto(
                    id = row[CarsTable.id],
                    carNumber = row[CarsTable.carNumber],
                    model = row[CarsTable.model],
                    status = row[CarsTable.status],
                )
            }
    }

    override suspend fun clearDriverFromAllCars(driverUserId: Int) {
        newSuspendedTransaction(Dispatchers.IO) {
            CarsTable.update({ CarsTable.carDriverId eq driverUserId }) {
                it[carDriverId] = null
            }
        }
    }

    override suspend fun assignDriver(carId: Int, driverUserId: Int) {
        newSuspendedTransaction(Dispatchers.IO) {
            CarsTable.update({ CarsTable.id eq carId }) {
                it[carDriverId] = driverUserId
            }
        }
    }
}
