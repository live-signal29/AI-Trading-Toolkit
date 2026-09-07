package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdBannerView
import com.example.ui.components.RiskDisclaimerBanner
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.util.Formatters
import com.example.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Risk, 1 = Lot Size, 2 = Pip, 3 = PnL

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trading Calculators",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("calculators_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Risk", fontSize = 12.sp) }, modifier = Modifier.testTag("tab_calc_risk"))
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Lot Size", fontSize = 12.sp) }, modifier = Modifier.testTag("tab_calc_lot"))
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Pip", fontSize = 12.sp) }, modifier = Modifier.testTag("tab_calc_pip"))
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("P/L", fontSize = 12.sp) }, modifier = Modifier.testTag("tab_calc_pnl"))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> RiskCalculatorView(viewModel)
                    1 -> LotSizeCalculatorView(viewModel)
                    2 -> PipCalculatorView(viewModel)
                    3 -> PnlCalculatorView(viewModel)
                }

                RiskDisclaimerBanner()

                AdBannerView(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun RiskCalculatorView(viewModel: CalculatorViewModel) {
    val balance by viewModel.riskBalance.collectAsState()
    val percent by viewModel.riskPercent.collectAsState()
    val entry by viewModel.riskEntry.collectAsState()
    val stopLoss by viewModel.riskStopLoss.collectAsState()
    val takeProfit by viewModel.riskTakeProfit.collectAsState()
    val direction by viewModel.riskDirection.collectAsState()
    val result by viewModel.riskResult.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Position Risk & Reward Setup", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = direction == "BUY",
                    onClick = {
                        viewModel.riskDirection.value = "BUY"
                        viewModel.computeRisk()
                    },
                    label = { Text("BUY (Long)") }
                )
                FilterChip(
                    selected = direction == "SELL",
                    onClick = {
                        viewModel.riskDirection.value = "SELL"
                        viewModel.computeRisk()
                    },
                    label = { Text("SELL (Short)") }
                )
            }

            OutlinedTextField(
                value = balance,
                onValueChange = {
                    viewModel.riskBalance.value = it
                    viewModel.computeRisk()
                },
                label = { Text("Account Balance ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("risk_input_balance"),
                singleLine = true
            )

            OutlinedTextField(
                value = percent,
                onValueChange = {
                    viewModel.riskPercent.value = it
                    viewModel.computeRisk()
                },
                label = { Text("Risk Percentage (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("risk_input_percent"),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry,
                    onValueChange = {
                        viewModel.riskEntry.value = it
                        viewModel.computeRisk()
                    },
                    label = { Text("Entry Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("risk_input_entry"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stopLoss,
                    onValueChange = {
                        viewModel.riskStopLoss.value = it
                        viewModel.computeRisk()
                    },
                    label = { Text("Stop Loss") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("risk_input_stop_loss"),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = takeProfit,
                onValueChange = {
                    viewModel.riskTakeProfit.value = it
                    viewModel.computeRisk()
                },
                label = { Text("Take Profit Target") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("risk_input_take_profit"),
                singleLine = true
            )
        }
    }

    result?.let { res ->
        Card(
            modifier = Modifier.fillMaxWidth().testTag("risk_result_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Risk Management Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Risk Amount:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(res.riskAmount), fontWeight = FontWeight.Bold, color = BearishRed, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Potential Loss:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(res.potentialLoss), fontWeight = FontWeight.Bold, color = BearishRed, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Potential Profit:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(res.potentialProfit), fontWeight = FontWeight.Bold, color = BullishGreen, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Risk / Reward Ratio:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("1:${String.format("%.2f", res.riskRewardRatio)}", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Position Units:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatNumber(res.positionSizeUnits), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Standard Lot Equivalent:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.3f lots", res.lotSize), fontWeight = FontWeight.Bold, color = AccentGold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LotSizeCalculatorView(viewModel: CalculatorViewModel) {
    val balance by viewModel.lotBalance.collectAsState()
    val riskPercent by viewModel.lotRiskPercent.collectAsState()
    val entry by viewModel.lotEntry.collectAsState()
    val stopLoss by viewModel.lotStopLoss.collectAsState()
    val result by viewModel.lotResult.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Forex / Metals Lot Size Sizing", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            OutlinedTextField(
                value = balance,
                onValueChange = {
                    viewModel.lotBalance.value = it
                    viewModel.computeLotSize()
                },
                label = { Text("Account Balance ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = riskPercent,
                onValueChange = {
                    viewModel.lotRiskPercent.value = it
                    viewModel.computeLotSize()
                },
                label = { Text("Risk Percentage (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry,
                    onValueChange = {
                        viewModel.lotEntry.value = it
                        viewModel.computeLotSize()
                    },
                    label = { Text("Entry Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stopLoss,
                    onValueChange = {
                        viewModel.lotStopLoss.value = it
                        viewModel.computeLotSize()
                    },
                    label = { Text("Stop Loss") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }

    result?.let { res ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Recommended Position Size", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Calculated Lot Size:", fontSize = 12.sp)
                    Text(String.format("%.2f Lots", res.lotSize), fontWeight = FontWeight.ExtraBold, color = BullishGreen, fontSize = 14.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Units:", fontSize = 12.sp)
                    Text(Formatters.formatNumber(res.units), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pip Distance to Stop Loss:", fontSize = 12.sp)
                    Text(String.format("%.1f pips", res.pipDifference), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Max Potential Loss:", fontSize = 12.sp)
                    Text(Formatters.formatCurrency(res.potentialLoss), fontWeight = FontWeight.Bold, color = BearishRed, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PipCalculatorView(viewModel: CalculatorViewModel) {
    val pair by viewModel.pipPair.collectAsState()
    val entry by viewModel.pipEntry.collectAsState()
    val exit by viewModel.pipExit.collectAsState()
    val lots by viewModel.pipLots.collectAsState()
    val result by viewModel.pipResult.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Pip Value & Movement Calculator", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            OutlinedTextField(
                value = pair,
                onValueChange = {
                    viewModel.pipPair.value = it
                    viewModel.computePip()
                },
                label = { Text("Currency Pair (e.g. EUR/USD, USD/JPY)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry,
                    onValueChange = {
                        viewModel.pipEntry.value = it
                        viewModel.computePip()
                    },
                    label = { Text("Entry Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = exit,
                    onValueChange = {
                        viewModel.pipExit.value = it
                        viewModel.computePip()
                    },
                    label = { Text("Exit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = lots,
                onValueChange = {
                    viewModel.pipLots.value = it
                    viewModel.computePip()
                },
                label = { Text("Lot Size") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }

    result?.let { res ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Pip Outcome", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pips:", fontSize = 12.sp)
                    Text(String.format("%.1f Pips", res.pips), fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pip Value (1 Lot):", fontSize = 12.sp)
                    Text(Formatters.formatCurrency(res.pipValuePerLot), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net P/L Result:", fontSize = 12.sp)
                    Text(Formatters.formatCurrency(res.totalProfitLoss), fontWeight = FontWeight.ExtraBold, color = if (res.totalProfitLoss >= 0) BullishGreen else BearishRed, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun PnlCalculatorView(viewModel: CalculatorViewModel) {
    val entry by viewModel.pnlEntry.collectAsState()
    val exit by viewModel.pnlExit.collectAsState()
    val size by viewModel.pnlPositionSize.collectAsState()
    val direction by viewModel.pnlDirection.collectAsState()
    val result by viewModel.pnlResult.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Profit & Loss Calculator", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = direction == "BUY",
                    onClick = {
                        viewModel.pnlDirection.value = "BUY"
                        viewModel.computePnl()
                    },
                    label = { Text("BUY") }
                )
                FilterChip(
                    selected = direction == "SELL",
                    onClick = {
                        viewModel.pnlDirection.value = "SELL"
                        viewModel.computePnl()
                    },
                    label = { Text("SELL") }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry,
                    onValueChange = {
                        viewModel.pnlEntry.value = it
                        viewModel.computePnl()
                    },
                    label = { Text("Entry Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = exit,
                    onValueChange = {
                        viewModel.pnlExit.value = it
                        viewModel.computePnl()
                    },
                    label = { Text("Exit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = size,
                onValueChange = {
                    viewModel.pnlPositionSize.value = it
                    viewModel.computePnl()
                },
                label = { Text("Position Size (Lots/Units)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }

    result?.let { res ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Return Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Return Amount:", fontSize = 12.sp)
                    Text(
                        Formatters.formatCurrency(res.profitLoss),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (res.isProfit) BullishGreen else BearishRed,
                        fontSize = 14.sp
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Percentage Return:", fontSize = 12.sp)
                    Text(
                        Formatters.formatPercent(res.percentageReturn),
                        fontWeight = FontWeight.Bold,
                        color = if (res.isProfit) BullishGreen else BearishRed,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
