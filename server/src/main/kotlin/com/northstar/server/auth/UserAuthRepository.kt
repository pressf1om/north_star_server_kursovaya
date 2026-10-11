package com.northstar.server.auth

import com.northstar.server.db.CarsTable
import com.northstar.server.db.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

data class UserRecord(
    val id: Int,
    val email: String,
    val username: String,
    val passwordHash: String,
    val status: String,
)

data class UserCarInfo(
    val carNumber: String?,
    val carModel: String?,
)

interface UserAuthRepository {
    suspend fun findByUsername(username: String): UserRecord?
    suspend fun findById(id: Int): UserRecord?
    suspend fun findCarByDriverId(driverId: Int): UserCarInfo?
    suspend fun create(
        email: String,
        username: String,
        passwordHash: String,
        status: String,
    ): UserRecord
}

class ExposedUserAuthRepository : UserAuthRepository {

    override suspend fun findByUsername(username: String): UserRecord? =
        newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.selectAll()
                .where { UsersTable.username eq username.trim() }
                .limit(1)
                .firstOrNull()
                ?.toUserRecord()
        }

    override suspend fun findById(id: Int): UserRecord? =
        newSuspendedTransaction(Dispatchers.IO) {
            UsersTable.selectAll()
                .where { UsersTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.toUserRecord()
        }

    override suspend fun findCarByDriverId(driverId: Int): UserCarInfo? =
        newSuspendedTransaction(Dispatchers.IO) {
            CarsTable.selectAll()
                .where { CarsTable.carDriverId eq driverId }
                .limit(1)
                .firstOrNull()
                ?.let { row ->
                    UserCarInfo(
                        carNumber = row[CarsTable.carNumber],
                        carModel = row[CarsTable.model],
                    )
                }
        }

    override suspend fun create(
        email: String,
        username: String,
        passwordHash: String,
        status: String,
    ): UserRecord = newSuspendedTransaction(Dispatchers.IO) {
        val now = Instant.now()
        val id = UsersTable.insert {
            it[UsersTable.email] = email.trim()
            it[UsersTable.username] = username.trim()
            it[UsersTable.password] = passwordHash
            it[UsersTable.status] = status
            it[UsersTable.createdDate] = now
        }[UsersTable.id]

        UserRecord(
            id = id,
            email = email.trim(),
            username = username.trim(),
            passwordHash = passwordHash,
            status = status,
        )
    }

    private fun ResultRow.toUserRecord() = UserRecord(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        username = this[UsersTable.username],
        passwordHash = this[UsersTable.password],
        status = this[UsersTable.status],
    )
}

fun UserRecord.toPublicDto(car: UserCarInfo? = null) = UserPublicDto(
    id = id,
    email = email,
    username = username,
    status = status,
    carNumber = car?.carNumber,
    carModel = car?.carModel,
)
