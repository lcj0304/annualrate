package com.jwxy.annualrate.ui

import androidx.lifecycle.ViewModel
import com.jwxy.annualrate.domain.AprCalculator
import com.jwxy.annualrate.domain.AprInputs
import com.jwxy.annualrate.domain.AprResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CalculationMode {
    BY_INSTALLMENT,
    BY_TOTAL_INTEREST,
    BY_INTEREST_RATE
}

class AprViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AprUiState())
    val uiState: StateFlow<AprUiState> = _uiState.asStateFlow()

    fun updatePrincipal(value: String) = update { copy(principalInput = value) }
    fun updatePeriods(value: String) = update { copy(periodsInput = value) }
    fun updateInstallment(value: String) = update { copy(installmentInput = value) }
    fun updateTotalInterest(value: String) = update { copy(totalInterestInput = value) }
    fun updateInterestRatePercent(value: String) = update { copy(interestRatePercentInput = value) }
    fun setCalculationMode(mode: CalculationMode) = update { copy(calculationMode = mode, result = null, errorMessage = null) }

    fun calculate() {
        val principal = _uiState.value.principalInput.toDoubleOrNull()
        val periods = _uiState.value.periodsInput.toIntOrNull()
        val periodsPerYear = _uiState.value.periodsPerYear

        if (principal == null || periods == null || periods <= 0) {
            update { copy(errorMessage = "请输入有效的贷款总金额和分期数", result = null) }
            return
        }

        val installment = when (_uiState.value.calculationMode) {
            CalculationMode.BY_INSTALLMENT -> {
                _uiState.value.installmentInput.toDoubleOrNull()
            }
            CalculationMode.BY_TOTAL_INTEREST -> {
                val totalInterest = _uiState.value.totalInterestInput.toDoubleOrNull()
                if (totalInterest == null) {
                    null
                } else {
                    (principal + totalInterest) / periods
                }
            }
            CalculationMode.BY_INTEREST_RATE -> {
                val ratePercent = _uiState.value.interestRatePercentInput.toDoubleOrNull()
                if (ratePercent == null) {
                    null
                } else {
                    val totalInterest = principal * (ratePercent / 100.0)
                    (principal + totalInterest) / periods
                }
            }
        }

        if (installment == null || installment <= 0) {
            update { copy(errorMessage = "请输入有效的还款金额、总利息或利率", result = null) }
            return
        }

        runCatching {
            AprCalculator.calculate(
                AprInputs(
                    principal = principal,
                    periods = periods,
                    installment = installment,
                    periodsPerYear = periodsPerYear
                )
            )
        }.onSuccess { result ->
            update {
                copy(
                    result = result,
                    errorMessage = null
                )
            }
        }.onFailure { throwable ->
            update { copy(errorMessage = throwable.message, result = null) }
        }
    }

    private inline fun update(block: AprUiState.() -> AprUiState) {
        _uiState.value = block(_uiState.value)
    }
}

data class AprUiState(
    val principalInput: String = "",
    val periodsInput: String = "",
    val installmentInput: String = "",
    val totalInterestInput: String = "",
    val interestRatePercentInput: String = "",
    val periodsPerYear: Int = 12,
    val calculationMode: CalculationMode = CalculationMode.BY_INSTALLMENT,
    val result: AprResult? = null,
    val errorMessage: String? = null
)

