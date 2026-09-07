package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.data.model.DataStatus
import com.example.data.model.MarketItem
import com.example.ui.components.AdBannerView
import com.example.ui.components.CandlestickChart
import com.example.ui.components.RewardedAdCard
import com.example.ui.components.RiskDisclaimerBanner
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.util.Formatters
import com.example.util.SmartMoneyConcepts
import com.example.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    viewModel: MarketViewModel,
    onNavigateToRiskCalculator: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.marketItems.collectAsState()
    val liveItems = items.filter { it.status == DataStatus.LIVE }

    var selectedItem by remember(liveItems) {
        mutableStateOf(liveItems.firstOrNull())
    }

    val candles by viewModel.selectedCandles.collectAsState()
    val isCandlesLoading by viewModel.isCandlesLoading.collectAsState()
    val smcAnalysis by viewModel.chartAnalysis.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()

    LaunchedEffect(selectedItem?.symbol, selectedTimeframe) {
        selectedItem?.let {
            viewModel.loadCandles(it.symbol, selectedTimeframe)
        }
    }

    var isAnalyzing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Analyzer",
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Chart Analyzer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("analyzer_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI API Key: Not configured (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Local algorithmic Smart Money Concepts & structural analysis active.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Asset Selection Chips
            Text(
                text = "Select Asset to Analyze:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                liveItems.forEach { item ->
                    FilterChip(
                        selected = selectedItem?.symbol == item.symbol,
                        onClick = {
                            selectedItem = item
                            viewModel.selectMarketItem(item)
                        },
                        label = { Text(item.symbol, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("analyzer_select_${item.symbol.replace("/", "_")}")
                    )
                }
            }

            // Timeframe Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("15m", "30m", "1h", "4h", "1d").forEach { tf ->
                    FilterChip(
                        selected = selectedTimeframe.lowercase() == tf.lowercase(),
                        onClick = { viewModel.selectTimeframe(tf) },
                        label = { Text(tf.uppercase(), fontSize = 11.sp) }
                    )
                }
            }

            // Candlestick Chart View
            CandlestickChart(
                candles = candles,
                isLoading = isCandlesLoading,
                showEMA20 = true,
                showEMA50 = true,
                showVolume = true
            )

            // Analyze Action Button
            Button(
                onClick = {
                    selectedItem?.let {
                        isAnalyzing = true
                        viewModel.loadCandles(it.symbol, selectedTimeframe)
                        isAnalyzing = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_analysis_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Technical & SMC Pattern Analysis")
                }
            }

            // Analysis Results Card
            smcAnalysis?.let { analysis ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analysis_results_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title & Bias Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Market Setup Rationale",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (analysis.suggestedBias == "BUY") BullishGreen.copy(alpha = 0.2f)
                                        else BearishRed.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "BIAS: ${analysis.suggestedBias}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (analysis.suggestedBias == "BUY") BullishGreen else BearishRed
                                )
                            }
                        }

                        Text(
                            text = analysis.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Key Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Trend", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(analysis.trend, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Momentum", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(analysis.momentum, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Quality", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("High Probability", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BullishGreen)
                            }
                        }

                        // Order Plan (Entry, SL, TP1, TP2, R:R)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Execution Plan",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Entry Target", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$${Formatters.formatPrice(analysis.potentialEntry)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Column {
                                        Text("Stop Loss", fontSize = 10.sp, color = BearishRed)
                                        Text("$${Formatters.formatPrice(analysis.stopLoss)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BearishRed)
                                    }
                                    Column {
                                        Text("Take Profit 1", fontSize = 10.sp, color = BullishGreen)
                                        Text("$${Formatters.formatPrice(analysis.takeProfit1)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BullishGreen)
                                    }
                                    Column {
                                        Text("Risk / Reward", fontSize = 10.sp, color = AccentCyan)
                                        Text("1:${String.format("%.2f", analysis.riskReward)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentCyan)
                                    }
                                }
                            }
                        }

                        // Structure Events Identified
                        if (analysis.structureEvents.isNotEmpty()) {
                            Text(
                                text = "Detected Structural Events:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            analysis.structureEvents.forEach { event ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "[${event.type}] ${event.description}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Calculate Risk CTA
                        Button(
                            onClick = { onNavigateToRiskCalculator(analysis.potentialEntry) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Risk")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Risk Calculator for this Setup")
                        }
                    }
                }
            }

            RewardedAdCard(
                title = "Unlock AI Pro Insights",
                subtitle = "Watch a brief sponsored video to unlock ad-free mode & VIP signals for 2 hours"
            )

            AdBannerView(modifier = Modifier.padding(vertical = 2.dp))

            RiskDisclaimerBanner()
        }
    }
}
