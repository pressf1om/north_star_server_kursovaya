package com.northstar.server.analytics

import kotlin.test.Test
import kotlin.test.assertEquals

class TripCostCalculatorTest {
    @Test
    fun matchesLegacyFormula() {
        val result = TripCostCalculator.calculate(
            platonKm = 100,
            ollKm = 500,
            platonCost = 3.2,
            driverSalary = 50.0,
            autodorCost = 10_000,
            fuelCost = 5_000,
        )
        assertEquals(10_000.0 + 5_000.0 + 100 * 3.2 + 50.0 * 500, result)
    }
}
