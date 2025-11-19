package com.jwxy.annualrate.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AprCalculatorTest {
    @Test
    fun `calculate returns zero rates when payments equal principal`() {
        val inputs = AprInputs(principal = 1000.0, periods = 10, installment = 100.0)
        val result = AprCalculator.calculate(inputs)
        assertEquals(1000.0, result.totalPaid, 1e-6)
        assertEquals(0.0, result.totalInterest, 1e-6)
        assertEquals(0.0, result.periodicRate, 1e-9)
        assertEquals(0.0, result.nominalApr, 1e-9)
        assertEquals(0.0, result.effectiveApr, 1e-9)
    }

    @Test
    fun `calculate computes positive rates for higher payments`() {
        val inputs = AprInputs(principal = 1000.0, periods = 12, installment = 95.0)
        val result = AprCalculator.calculate(inputs)
        assertTrue(result.totalInterest > 0)
        assertTrue(result.periodicRate > 0)
        assertTrue(result.nominalApr > 0)
        assertTrue(result.effectiveApr > 0)
    }

    @Test
    fun `effective APR is higher when periods per year increases`() {
        val monthly = AprCalculator.calculate(
            AprInputs(principal = 1000.0, periods = 12, installment = 95.0, periodsPerYear = 12)
        )
        val weekly = AprCalculator.calculate(
            AprInputs(principal = 1000.0, periods = 12, installment = 95.0, periodsPerYear = 52)
        )
        assertTrue(weekly.effectiveApr > monthly.effectiveApr)
    }
}
