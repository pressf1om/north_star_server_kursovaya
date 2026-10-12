package com.northstar.server.analytics

object TripCostCalculator {
    fun calculate(
        platonKm: Int?,
        ollKm: Int?,
        platonCost: Double?,
        driverSalary: Double?,
        autodorCost: Int?,
        fuelCost: Int?,
    ): Double {
        val platonKmValue = (platonKm ?: 0).toDouble()
        val ollKmValue = (ollKm ?: 0).toDouble()
        val platonCostValue = platonCost ?: 0.0
        val driverSalaryValue = driverSalary ?: 0.0
        val autodor = (autodorCost ?: 0).toDouble()
        val fuel = (fuelCost ?: 0).toDouble()
        return (platonKmValue * platonCostValue) + autodor + (driverSalaryValue * ollKmValue) + fuel
    }
}
