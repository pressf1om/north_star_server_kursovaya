package com.northstar.server.drivermessages

import com.northstar.server.db.DriverMessagesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

interface DriverMessageRepository {
    suspend fun create(message: String, carNumber: String): DriverMessageDto
    suspend fun listAll(): List<DriverMessageDto>
    suspend fun listByCar(carNumber: String): List<DriverMessageDto>
    suspend fun findById(id: Int): DriverMessageDto?
    suspend fun reply(id: Int, reply: String): DriverMessageDto?
}

class ExposedDriverMessageRepository : DriverMessageRepository {

    override suspend fun create(message: String, carNumber: String): DriverMessageDto =
        newSuspendedTransaction(Dispatchers.IO) {
            val id = DriverMessagesTable.insert {
                it[DriverMessagesTable.message] = message
                it[DriverMessagesTable.carNumber] = carNumber
            }[DriverMessagesTable.id]
            DriverMessageDto(id = id, message = message, carNumber = carNumber, reply = null)
        }

    override suspend fun listAll(): List<DriverMessageDto> = newSuspendedTransaction(Dispatchers.IO) {
        DriverMessagesTable.selectAll()
            .orderBy(DriverMessagesTable.id, SortOrder.DESC)
            .map { it.toDto() }
    }

    override suspend fun listByCar(carNumber: String): List<DriverMessageDto> =
        newSuspendedTransaction(Dispatchers.IO) {
            DriverMessagesTable.selectAll()
                .where { DriverMessagesTable.carNumber eq carNumber.trim() }
                .orderBy(DriverMessagesTable.id, SortOrder.DESC)
                .map { it.toDto() }
        }

    override suspend fun findById(id: Int): DriverMessageDto? = newSuspendedTransaction(Dispatchers.IO) {
        DriverMessagesTable.selectAll()
            .where { DriverMessagesTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toDto()
    }

    override suspend fun reply(id: Int, reply: String): DriverMessageDto? =
        newSuspendedTransaction(Dispatchers.IO) {
            val updated = DriverMessagesTable.update({ DriverMessagesTable.id eq id }) {
                it[DriverMessagesTable.reply] = reply
            }
            if (updated == 0) {
                null
            } else {
                DriverMessagesTable.selectAll()
                    .where { DriverMessagesTable.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?.toDto()
            }
        }

    private fun ResultRow.toDto() = DriverMessageDto(
        id = this[DriverMessagesTable.id],
        message = this[DriverMessagesTable.message],
        carNumber = this[DriverMessagesTable.carNumber],
        reply = this[DriverMessagesTable.reply],
    )
}
