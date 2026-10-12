package com.northstar.server.users

import com.northstar.server.auth.UserPublicDto
import com.northstar.server.db.CarsTable
import com.northstar.server.db.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface UserAdminRepository {
    suspend fun listAllPublic(): List<UserPublicDto>
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
                val userId = row[UsersTable.id]
                UserPublicDto(
                    id = userId,
                    email = row[UsersTable.email],
                    username = row[UsersTable.username],
                    status = row[UsersTable.status],
                    carNumber = carsByDriverId[userId],
                    carModel = null,
                )
            }
    }
}
