package com.jwxy.annualrate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jwxy.annualrate.R
import com.jwxy.annualrate.domain.AprResult
import com.jwxy.annualrate.ui.theme.AnnualrateTheme
import java.text.NumberFormat

@Composable
fun AprScreen(viewModel: AprViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    AprContent(
        uiState = uiState,
        onPrincipalChange = viewModel::updatePrincipal,
        onPeriodsChange = viewModel::updatePeriods,
        onInstallmentChange = viewModel::updateInstallment,
        onTotalInterestChange = viewModel::updateTotalInterest,
        onInterestRatePercentChange = viewModel::updateInterestRatePercent,
        onModeChange = viewModel::setCalculationMode,
        onCalculate = viewModel::calculate
    )
}

@Composable
fun AprContent(
    uiState: AprUiState,
    onPrincipalChange: (String) -> Unit,
    onPeriodsChange: (String) -> Unit,
    onInstallmentChange: (String) -> Unit,
    onTotalInterestChange: (String) -> Unit,
    onInterestRatePercentChange: (String) -> Unit,
    onModeChange: (CalculationMode) -> Unit,
    onCalculate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.title_apr_calculator),
                style = MaterialTheme.typography.headlineSmall
            )

            LoanInputField(
                label = stringResource(id = R.string.label_principal),
                value = uiState.principalInput,
                onValueChange = onPrincipalChange,
                keyboardType = KeyboardType.Number
            )

            LoanInputField(
                label = stringResource(id = R.string.label_periods),
                value = uiState.periodsInput,
                onValueChange = onPeriodsChange,
                keyboardType = KeyboardType.Number
            )

            CalculationModeSwitcher(
                currentMode = uiState.calculationMode,
                onModeChange = onModeChange
            )

            when (uiState.calculationMode) {
                CalculationMode.BY_INSTALLMENT -> {
                    LoanInputField(
                        label = stringResource(id = R.string.label_installment),
                        value = uiState.installmentInput,
                        onValueChange = onInstallmentChange,
                        keyboardType = KeyboardType.Number
                    )
                }
                CalculationMode.BY_TOTAL_INTEREST -> {
                    LoanInputField(
                        label = stringResource(id = R.string.label_total_interest),
                        value = uiState.totalInterestInput,
                        onValueChange = onTotalInterestChange,
                        keyboardType = KeyboardType.Number
                    )
                }
                CalculationMode.BY_INTEREST_RATE -> {
                    LoanInputField(
                        label = stringResource(id = R.string.label_interest_rate_percent),
                        value = uiState.interestRatePercentInput,
                        onValueChange = onInterestRatePercentChange,
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(text = stringResource(id = R.string.action_calculate))
            }

            uiState.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            uiState.result?.let { result ->
                ResultCard(result)
            }
        }
    }
}

@Composable
private fun CalculationModeSwitcher(
    currentMode: CalculationMode,
    onModeChange: (CalculationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = CalculationMode.entries.toTypedArray()
    TabRow(
        selectedTabIndex = modes.indexOf(currentMode),
        modifier = modifier.fillMaxWidth()
    ) {
        modes.forEach { mode ->
            Tab(
                selected = mode == currentMode,
                onClick = { onModeChange(mode) },
                text = {
                    val textId = when (mode) {
                        CalculationMode.BY_INSTALLMENT -> R.string.mode_by_installment
                        CalculationMode.BY_TOTAL_INTEREST -> R.string.mode_by_total_interest
                        CalculationMode.BY_INTEREST_RATE -> R.string.mode_by_interest_rate
                    }
                    Text(stringResource(id = textId))
                }
            )
        }
    }
}

@Composable
private fun LoanInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType)
    )
}

@Composable
private fun ResultCard(result: AprResult) {
    val currencyFormat = NumberFormat.getCurrencyInstance()
    val percentFormat = NumberFormat.getPercentInstance().apply {
        maximumFractionDigits = 2
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.label_results), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ResultRow(label = stringResource(id = R.string.result_total_paid), value = currencyFormat.format(result.totalPaid))
            ResultRow(label = stringResource(id = R.string.result_total_interest), value = currencyFormat.format(result.totalInterest))
            ResultRow(label = stringResource(id = R.string.result_period_rate), value = percentFormat.format(result.periodicRate))
            ResultRow(label = stringResource(id = R.string.result_nominal_apr), value = percentFormat.format(result.nominalApr))
            ResultRow(label = stringResource(id = R.string.result_effective_apr), value = percentFormat.format(result.effectiveApr))
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun AprScreenPreview() {
    AnnualrateTheme {
        AprScreen()
    }
}
