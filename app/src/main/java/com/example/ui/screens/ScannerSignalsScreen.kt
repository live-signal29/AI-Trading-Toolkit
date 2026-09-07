package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScannerResult
import com.example.data.model.SignalItem
import com.example.ui.components.AdBannerView
import com.example.ui.components.RewardedAdCard
import com.example.ui.components.RiskDisclaimerBanner
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.util.Formatters
import com.example.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerSignalsScreen(
    viewModel: MarketViewModel,
    onSelectSymbol: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Scanner, 1 = Signals
    val scannerResults by viewModel.scannerResults.collectAsState()
    val signals by viewModel.signals.collectAsState()

    var scannerFilter by remember { mutableStateOf<String?>(null) } // null = All, "Oversold", "Overbought", "Bullish", "Bearish"

    val filteredScanner = scannerResults.filter { item ->
        when (scannerFilter) {
            "Oversold" -> item.momentum == "Oversold" || item.rsi <= 35.0
            "Overbought" -> item.momentum == "Overbought" || item.rsi >= 65.0
            "Bullish" -> item.trend.contains("Bullish")
            "Bearish" -> item.trend.contains("Bearish")
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "Market Scanner" else "Technical Signals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.testTag("scanner_signals_screen")
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
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scanner (${scannerResults.size})")
                        }
                    },
                    modifier = Modifier.testTag("tab_scanner")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SignalCellularAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Signals (${signals.size})")
                        }
                    },
                    modifier = Modifier.testTag("tab_signals")
                )
            }

            if (selectedTab == 0) {
                // Scanner Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Filter Chips for Scanner Presets
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = scannerFilter == null,
                                onClick = { scannerFilter = null },
                                label = { Text("All", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = scannerFilter == "Bullish",
                                onClick = { scannerFilter = "Bullish" },
                                label = { Text("Bullish", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedLabelColor = BullishGreen)
                            )
                            FilterChip(
                                selected = scannerFilter == "Bearish",
                                onClick = { scannerFilter = "Bearish" },
                                label = { Text("Bearish", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedLabelColor = BearishRed)
                            )
                            FilterChip(
                                selected = scannerFilter == "Oversold",
                                onClick = { scannerFilter = "Oversold" },
                                label = { Text("RSI Oversold", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedLabelColor = AccentCyan)
                            )
                            FilterChip(
                                selected = scannerFilter == "Overbought",
                                onClick = { scannerFilter = "Overbought" },
                                label = { Text("RSI Overbought", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedLabelColor = AccentGold)
                            )
                        }
                    }

                    item {
                        RiskDisclaimerBanner()
                    }

                    if (filteredScanner.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No scanner setups match the selected criteria.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredScanner, key = { it.symbol }) { item ->
                            ScannerResultCard(
                                result = item,
                                onClick = { onSelectSymbol(item.symbol) }
                            )
                        }
                    }
                }
            } else {
                // Signals Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Rule-based technical analysis setups derived strictly from live candle price action. Signals are educational only.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 11.sp
                            )
                        }
                    }

                    item {
                        RewardedAdCard(
                            title = "Unlock VIP Real-Time Signals",
                            subtitle = "Watch a brief sponsored video to activate VIP setups & ad-free experience for 2 hours"
                        )
                    }

                    item {
                        AdBannerView(modifier = Modifier.padding(vertical = 2.dp))
                    }

                    item {
                        RiskDisclaimerBanner()
                    }

                    if (signals.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Signal unavailable because reliable market data is currently unavailable.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(signals, key = { it.id }) { signal ->
                            SignalItemCard(
                                signal = signal,
                                onClick = { onSelectSymbol(signal.symbol) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerResultCard(
    result: ScannerResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBullish = result.trend.contains("Bullish")
    val trendColor = if (isBullish) BullishGreen else if (result.trend.contains("Bearish")) BearishRed else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("scanner_card_${result.symbol.replace("/", "_")}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = result.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(trendColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = result.trend,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }

                Text(
                    text = "$${Formatters.formatPrice(result.price)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RSI(14): ${String.format("%.1f", result.rsi)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Momentum: ${result.momentum}",
                    fontSize = 11.sp,
                    color = AccentCyan
                )
                Text(
                    text = "Quality: ${result.setupQuality}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.setupQuality == "High") BullishGreen else AccentGold
                )
            }

            if (result.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    result.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(text = tag, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignalItemCard(
    signal: SignalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBuy = signal.direction.uppercase() == "BUY"
    val dirColor = if (isBuy) BullishGreen else BearishRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("signal_card_${signal.symbol.replace("/", "_")}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Symbol, Timeframe, Direction Badge, Reliable Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentCyan.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(text = signal.timeframe, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(dirColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = signal.direction,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = dirColor
                    )
                }
            }

            // Price Targets Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Entry", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${Formatters.formatPrice(signal.entry)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column {
                    Text("Stop Loss", fontSize = 10.sp, color = BearishRed)
                    Text("$${Formatters.formatPrice(signal.stopLoss)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BearishRed)
                }
                Column {
                    Text("TP 1", fontSize = 10.sp, color = BullishGreen)
                    Text("$${Formatters.formatPrice(signal.tp1)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BullishGreen)
                }
                Column {
                    Text("R:R", fontSize = 10.sp, color = AccentGold)
                    Text("1:${String.format("%.2f", signal.riskReward)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentGold)
                }
            }

            // Analysis reasoning
            Text(
                text = signal.analysis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            // Footer: Source and Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${signal.dataSource}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = signal.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentCyan
                )
            }
        }
    }
}
