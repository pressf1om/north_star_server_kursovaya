package com.northstar.server.applications

import com.northstar.server.db.CompletedApplicationsTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface CompletedApplicationRepository {
    suspend fun listAll(): List<ApplicationDto>
    suspend fun listByCar(carNumber: String): List<ApplicationDto>
}

class ExposedCompletedApplicationRepository : CompletedApplicationRepository {

    override suspend fun listAll(): List<ApplicationDto> = newSuspendedTransaction(Dispatchers.IO) {
        CompletedApplicationsTable.selectAll()
            .orderBy(CompletedApplicationsTable.id, SortOrder.DESC)
            .map { it.toApplicationDto() }
    }

    override suspend fun listByCar(carNumber: String): List<ApplicationDto> =
        newSuspendedTransaction(Dispatchers.IO) {
            CompletedApplicationsTable.selectAll()
                .where { CompletedApplicationsTable.carNow eq carNumber.trim() }
                .orderBy(CompletedApplicationsTable.id, SortOrder.DESC)
                .map { it.toApplicationDto() }
        }

    private fun ResultRow.toApplicationDto() = ApplicationDto(
        id = this[CompletedApplicationsTable.id],
        coordStart = this[CompletedApplicationsTable.coordStart],
        coordEnd = this[CompletedApplicationsTable.coordEnd],
        dateOfStart = this[CompletedApplicationsTable.dateOfStart],
        dateOfEnd = this[CompletedApplicationsTable.dateOfEnd],
        status = this[CompletedApplicationsTable.status],
        weight = this[CompletedApplicationsTable.weight],
        carNow = this[CompletedApplicationsTable.carNow],
    )
}
