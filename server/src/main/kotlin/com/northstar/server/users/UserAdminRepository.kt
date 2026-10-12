package com.northstar.server.users

import com.northstar.server.auth.UserNotFoundException
import com.northstar.server.auth.UserPublicDto
import com.northstar.server.auth.UserStatus
import com.northstar.server.db.CarsTable
import com.northstar.server.db.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

interface UserAdminRepository {
    suspend fun listAllPublic(): List<UserPublicDto>
    suspend fun updateRole(userId: Int, status: String, carId: Int?): UserPublicDto
}

class ExposedUserAdminRepository : UserAdminRepository {

    override suspend fun listAllPublic(): List<UserPublicDto> = newSuspendedTransaction(Dispatchers.IO) {
        val carsByDriverId = CarsTable.selectAll()
            .where { CarsTable.carDriverId.isNotNull() }
            .associate { row ->
                row[CarsTable.carDriverId]!! to row[CarsTable.carNumber]
            }

        UsersTable.selectAll()
            .orderBy(UsersTable.id, SortOrder.ASC)
            .map { row ->
                val id = row[UsersTable.id]
                UserPublicDto(
                    id = id,
                    email = row[UsersTable.email],
                    username = row[UsersTable.username],
                    status = row[UsersTable.status],
                    carNumber = carsByDriverId[id],
                    carModel = null,
                )
            }
    }

    override suspend fun updateRole(userId: Int, status: String, carId: Int?): UserPublicDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val userRow = UsersTable.selectAll()
                .where { UsersTable.id eq userId }
                .limit(1)
                .firstOrNull()
                ?: throw UserNotFoundException()

            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.status] = status
            }

            CarsTable.update({ CarsTable.carDriverId eq userId }) {
                it[carDriverId] = null
            }

            if (status == UserStatus.DRIVER && carId != null) {
                CarsTable.update({ CarsTable.id eq carId }) {
                    it[carDriverId] = userId
                }
            }

            val carRow = CarsTable.selectAll()
                .where { CarsTable.carDriverId eq userId }
                .limit(1)
                .firstOrNull()

            UserPublicDto(
                id = userId,
                email = userRow[UsersTable.email],
                username = userRow[UsersTable.username],
                status = status,
                carNumber = carRow?.get(CarsTable.carNumber),
                carModel = carRow?.get(CarsTable.model),
            )
        }
}
