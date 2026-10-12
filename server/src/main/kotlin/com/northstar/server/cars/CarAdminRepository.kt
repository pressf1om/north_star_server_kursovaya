package com.northstar.server.cars

import com.northstar.server.db.CarsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

interface CarAdminRepository {
    suspend fun listAll(): List<CarPublicDto>
    suspend fun findById(id: Int): CarPublicDto?
    suspend fun clearDriverFromAllCars(driverUserId: Int)
    suspend fun assignDriver(carId: Int, driverUserId: Int)
}

class ExposedCarAdminRepository : CarAdminRepository {

    override suspend fun listAll(): List<CarPublicDto> = newSuspendedTransaction(Dispatchers.IO) {
        CarsTable.selectAll()
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
