package com.jwxy.annualrate.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Calculator for installment-based loans such as credit-card payment plans.
 * The calculator assumes fixed payment amount each period and solves for the
 * implied periodic interest rate that balances the remaining principal.
 */
data class AprInputs(
    val principal: Double,
    val periods: Int,
    val installment: Double,
    val periodsPerYear: Int = 12
)

data class AprResult(
    val totalPaid: Double,
    val totalInterest: Double,
    val periodicRate: Double,
    val nominalApr: Double, // Nominal APR (rate * periods per year)
    val effectiveApr: Double // Effective APR (compounded)
)

object AprCalculator {
    private const val PRECISION = 1e-9
    private const val MAX_ITERATIONS = 10_000

    fun calculate(inputs: AprInputs): AprResult {
        require(inputs.principal > 0) { "Principal must be positive" }
        require(inputs.periods > 0) { "Periods must be positive" }
        require(inputs.installment > 0) { "Installment must be positive" }
        require(inputs.periodsPerYear > 0) { "Periods per year must be positive" }

        val totalPaid = inputs.installment * inputs.periods
        val totalInterest = totalPaid - inputs.principal

        if (totalPaid <= inputs.principal + PRECISION) {
            return AprResult(
                totalPaid = totalPaid,
                totalInterest = totalInterest,
                periodicRate = 0.0,
                nominalApr = 0.0,
                effectiveApr = 0.0
            )
        }

        val periodicRate = solvePeriodicRate(inputs)
        val nominalApr = periodicRate * inputs.periodsPerYear
        val effectiveApr = (1 + periodicRate).pow(inputs.periodsPerYear) - 1

        return AprResult(
            totalPaid = totalPaid,
            totalInterest = totalInterest,
            periodicRate = periodicRate,
            nominalApr = nominalApr,
            effectiveApr = effectiveApr
        )
    }

    private fun solvePeriodicRate(inputs: AprInputs): Double {
        var low = 0.0
        var high = 1.0

        // The npv function is monotonically increasing with the rate.
        // We need to find a 'high' rate for which npv is positive.
        // The root (where npv is 0) will be between a 'low' where npv is negative and a 'high' where npv is positive.
        while (npv(high, inputs) < 0) {
            low = high
            high *= 2
            // Set a reasonable ceiling to prevent infinite loops on invalid inputs.
            if (high > 1e6) { // Corresponds to an APR of 100,000,000%
                throw IllegalStateException("Could not find an upper bound for the interest rate.")
            }
        }

        repeat(MAX_ITERATIONS) {
            val mid = (low + high) / 2
            if (mid == low || mid == high) { // Break if we are not making progress
                return mid
            }
            val balance = npv(mid, inputs)
            if (abs(balance) < PRECISION) {
                return mid
            }
            // If balance is negative, the guessed rate 'mid' is too low.
            if (balance < 0) {
                low = mid
            } else {
                high = mid
            }
        }

        return (low + high) / 2
    }

    private fun npv(rate: Double, inputs: AprInputs): Double {
        if (rate == 0.0) {
            return inputs.principal - inputs.installment * inputs.periods
        }
        // This is the standard Net Present Value (NPV) formula for an annuity.
        // We are looking for the rate where the present value of all installments equals the principal.
        return inputs.principal - inputs.installment * (1 - (1 + rate).pow(-inputs.periods)) / rate
    }
}
