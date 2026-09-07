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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketItem
import com.example.ui.components.CandlestickChart
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.util.Formatters
import com.example.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDetailScreen(
    marketItem: MarketItem,
    viewModel: MarketViewModel,
    onBack: () -> Unit,
    onOpenCalculator: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val candles by viewModel.selectedCandles.collectAsState()
    val isCandlesLoading by viewModel.isCandlesLoading.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val analysis by viewModel.chartAnalysis.collectAsState()

    LaunchedEffect(marketItem.symbol, selectedTimeframe) {
        viewModel.loadCandles(marketItem.symbol, selectedTimeframe)
    }

    var showEMA20 by remember { mutableStateOf(true) }
    var showEMA50 by remember { mutableStateOf(true) }
    var showBB by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(true) }

    var showAlertDialog by remember { mutableStateOf(false) }
    var alertPriceInput by remember { mutableStateOf(marketItem.price.toString()) }
    var alertCondition by remember { mutableStateOf("ABOVE") }

    val timeframes = listOf("1m", "5m", "15m", "30m", "1h", "4h", "1d", "1w")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = marketItem.symbol,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(status = marketItem.status)
                        }
                        Text(
                            text = "Source: ${marketItem.source}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAlertDialog = true },
                        modifier = Modifier.testTag("set_alert_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Add Price Alert",
                            tint = AccentGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("chart_detail_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Price & 24h Stats Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$${Formatters.formatPrice(marketItem.price)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = Formatters.formatPercent(marketItem.change24h),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (marketItem.change24h >= 0) BullishGreen else BearishRed
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "24h High: $${Formatters.formatPrice(marketItem.high24h)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "24h Low: $${Formatters.formatPrice(marketItem.low24h)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Volume: ${Formatters.formatNumber(marketItem.volume24h)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Timeframe Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timeframes.forEach { tf ->
                    FilterChip(
                        selected = selectedTimeframe.lowercase() == tf.lowercase(),
                        onClick = { viewModel.selectTimeframe(tf) },
                        label = { Text(tf.uppercase(), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("tf_button_$tf")
                    )
                }
            }

            // Indicator Toggles Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showEMA20,
                    onClick = { showEMA20 = !showEMA20 },
                    label = { Text("EMA 20", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                        selectedLabelColor = AccentCyan
                    )
                )
                FilterChip(
                    selected = showEMA50,
                    onClick = { showEMA50 = !showEMA50 },
                    label = { Text("EMA 50", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                        selectedLabelColor = AccentGold
                    )
                )
                FilterChip(
                    selected = showBB,
                    onClick = { showBB = !showBB },
                    label = { Text("Bollinger", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF9333EA).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF9333EA)
                    )
                )
                FilterChip(
                    selected = showVolume,
                    onClick = { showVolume = !showVolume },
                    label = { Text("Volume", fontSize = 10.sp) }
                )
            }

            // Interactive Candlestick Chart
            CandlestickChart(
                candles = candles,
                isLoading = isCandlesLoading,
                showEMA20 = showEMA20,
                showEMA50 = showEMA50,
                showBollingerBands = showBB,
                showVolume = showVolume
            )

            // Direct Calculator CTA
            Button(
                onClick = { onOpenCalculator(marketItem.price) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculate_risk_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = "Calculator")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculate Position Risk for $${Formatters.formatPrice(marketItem.price)}")
            }

            // Smart Money Concepts & Structure Analysis Card
            analysis?.let { smc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Market Structure (SMC)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (smc.suggestedBias == "BUY") BullishGreen.copy(alpha = 0.15f)
                                        else BearishRed.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Bias: ${smc.suggestedBias}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (smc.suggestedBias == "BUY") BullishGreen else BearishRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = smc.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Suggested levels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Suggested Entry", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${Formatters.formatPrice(smc.potentialEntry)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column {
                                Text("Stop Loss", fontSize = 10.sp, color = BearishRed)
                                Text("$${Formatters.formatPrice(smc.stopLoss)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BearishRed)
                            }
                            Column {
                                Text("Take Profit 1", fontSize = 10.sp, color = BullishGreen)
                                Text("$${Formatters.formatPrice(smc.takeProfit1)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BullishGreen)
                            }
                            Column {
                                Text("R:R Ratio", fontSize = 10.sp, color = AccentCyan)
                                Text("1:${String.format("%.2f", smc.riskReward)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentCyan)
                            }
                        }
                    }
                }
            }
        }
    }

    // Set Price Alert Dialog
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Create Price Alert") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Trigger alert when ${marketItem.symbol} crosses target price:")
                    OutlinedTextField(
                        value = alertPriceInput,
                        onValueChange = { alertPriceInput = it },
                        label = { Text("Target Price ($)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alert_price_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = alertCondition == "ABOVE",
                            onClick = { alertCondition = "ABOVE" },
                            label = { Text("Price >= Target") }
                        )
                        FilterChip(
                            selected = alertCondition == "BELOW",
                            onClick = { alertCondition = "BELOW" },
                            label = { Text("Price <= Target") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = alertPriceInput.toDoubleOrNull() ?: marketItem.price
                        viewModel.createPriceAlert(marketItem.symbol, target, alertCondition)
                        showAlertDialog = false
                    },
                    modifier = Modifier.testTag("save_alert_confirm")
                ) {
                    Text("Save Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
