package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JournalEntry
import com.example.ui.components.RiskDisclaimerBanner
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.util.Formatters
import com.example.util.JournalStats
import com.example.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trading Journal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_trade_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Trade")
            }
        },
        modifier = modifier.testTag("journal_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Analytics Dashboard Card
            item {
                JournalAnalyticsCard(stats)
            }

            item {
                RiskDisclaimerBanner()
            }

            item {
                Text(
                    text = "Trade History (${entries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No trades recorded yet. Tap '+' to log a trade.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    TradeEntryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteTrade(entry.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTradeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { symbol, dir, entry, sl, tp, exit, lot, risk, strat, session, notes ->
                viewModel.addTrade(symbol, dir, entry, sl, tp, exit, lot, risk, strat, session, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun JournalAnalyticsCard(stats: JournalStats) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("journal_analytics_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Performance Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Primary Stats Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Win Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.1f%%", stats.winRate), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BullishGreen)
                }
                Column {
                    Text("Net P/L", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        Formatters.formatCurrency(stats.netPnl),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (stats.netPnl >= 0) BullishGreen else BearishRed
                    )
                }
                Column {
                    Text("Profit Factor", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.2f", stats.profitFactor), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AccentCyan)
                }
                Column {
                    Text("Avg R:R", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.2fR", stats.averageR), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AccentGold)
                }
            }

            // Secondary Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ${stats.totalTrades} (${stats.wins}W / ${stats.losses}L)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Winning Streak: ${stats.winningStreak}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Max DD: $${String.format("%.2f", stats.maxDrawdown)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Best Performers
            if (stats.bestSymbol != "N/A") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Best Symbol: ${stats.bestSymbol}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AccentCyan)
                    Text("Best Session: ${stats.bestSession}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AccentCyan)
                }
            }
        }
    }
}

@Composable
fun TradeEntryCard(
    entry: JournalEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBuy = entry.direction.uppercase() == "BUY"
    val resultColor = when (entry.result) {
        "WIN" -> BullishGreen
        "LOSS" -> BearishRed
        "BREAKEVEN" -> AccentGold
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .testTag("journal_entry_${entry.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header: Symbol, Direction, Result Badge, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = entry.symbol, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isBuy) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = entry.direction, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isBuy) BullishGreen else BearishRed)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(resultColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = entry.result, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = resultColor)
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp).testTag("delete_trade_${entry.id}")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = BearishRed, modifier = Modifier.size(16.dp))
                }
            }

            // Price Details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Entry: $${Formatters.formatPrice(entry.entryPrice)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("SL: $${Formatters.formatPrice(entry.stopLoss)}", fontSize = 11.sp, color = BearishRed)
                Text("TP: $${Formatters.formatPrice(entry.takeProfit)}", fontSize = 11.sp, color = BullishGreen)
                if (entry.exitPrice != null && entry.exitPrice > 0) {
                    Text("Exit: $${Formatters.formatPrice(entry.exitPrice)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Profit & Strategy info
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Strat: ${entry.strategy.ifEmpty { "Price Action" }}", fontSize = 10.sp, color = AccentCyan)
                Text("Session: ${entry.session}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.result != "OPEN") {
                    Text("P/L: ${Formatters.formatCurrency(entry.profitAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = resultColor)
                }
            }

            if (entry.notes.isNotBlank()) {
                Text(text = "Notes: ${entry.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AddTradeDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        symbol: String,
        direction: String,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        exitPrice: Double?,
        lotSize: Double,
        riskPercent: Double,
        strategy: String,
        session: String,
        notes: String
    ) -> Unit
) {
    var symbol by remember { mutableStateOf("BTC/USDT") }
    var direction by remember { mutableStateOf("BUY") }
    var entryPrice by remember { mutableStateOf("") }
    var stopLoss by remember { mutableStateOf("") }
    var takeProfit by remember { mutableStateOf("") }
    var exitPrice by remember { mutableStateOf("") }
    var lotSize by remember { mutableStateOf("1.0") }
    var riskPercent by remember { mutableStateOf("1.5") }
    var strategy by remember { mutableStateOf("SMC Order Block") }
    var session by remember { mutableStateOf("London") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Trade") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = symbol, onValueChange = { symbol = it }, label = { Text("Symbol") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = direction == "BUY", onClick = { direction = "BUY" }, label = { Text("BUY") })
                    FilterChip(selected = direction == "SELL", onClick = { direction = "SELL" }, label = { Text("SELL") })
                }
                OutlinedTextField(value = entryPrice, onValueChange = { entryPrice = it }, label = { Text("Entry Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = stopLoss, onValueChange = { stopLoss = it }, label = { Text("Stop Loss") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = takeProfit, onValueChange = { takeProfit = it }, label = { Text("Take Profit") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = exitPrice, onValueChange = { exitPrice = it }, label = { Text("Exit Price (optional if closed)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = lotSize, onValueChange = { lotSize = it }, label = { Text("Lot / Units Size") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = strategy, onValueChange = { strategy = it }, label = { Text("Strategy / Setup") }, singleLine = true)
                OutlinedTextField(value = session, onValueChange = { session = it }, label = { Text("Session (Asian, London, NY)") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Trade Notes") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entry = entryPrice.toDoubleOrNull() ?: 0.0
                    val sl = stopLoss.toDoubleOrNull() ?: 0.0
                    val tp = takeProfit.toDoubleOrNull() ?: 0.0
                    val exit = exitPrice.toDoubleOrNull()
                    val lot = lotSize.toDoubleOrNull() ?: 1.0
                    val risk = riskPercent.toDoubleOrNull() ?: 1.0

                    if (symbol.isNotBlank() && entry > 0) {
                        onConfirm(symbol, direction, entry, sl, tp, exit, lot, risk, strategy, session, notes)
                    }
                },
                modifier = Modifier.testTag("save_trade_confirm")
            ) {
                Text("Save Trade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
