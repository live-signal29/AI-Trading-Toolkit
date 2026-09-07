package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.data.model.AssetType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BullishGreen

data class AssetPreset(
    val symbol: String,
    val price: Double,
    val change: Double,
    val type: AssetType,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomAssetSheet(
    onDismiss: () -> Unit,
    onSaveAsset: (
        symbol: String,
        price: Double,
        change24h: Double,
        assetType: AssetType,
        high24h: Double,
        low24h: Double,
        volume24h: Double,
        notes: String
    ) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var symbol by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var changeText by remember { mutableStateOf("0.0") }
    var highText by remember { mutableStateOf("") }
    var lowText by remember { mutableStateOf("") }
    var volumeText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AssetType.CRYPTO) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presets = listOf(
        AssetPreset("XAU/USD", 2658.0, 0.75, AssetType.GOLD, "Gold Spot"),
        AssetPreset("XAG/USD", 31.80, 1.20, AssetType.GOLD, "Silver Spot"),
        AssetPreset("EUR/USD", 1.0860, 0.25, AssetType.FOREX, "Euro"),
        AssetPreset("GBP/USD", 1.2980, -0.20, AssetType.FOREX, "British Pound"),
        AssetPreset("US30", 42850.0, 0.45, AssetType.INDICES, "Dow 30"),
        AssetPreset("NAS100", 20390.0, 0.85, AssetType.INDICES, "Nasdaq"),
        AssetPreset("BTC/USDT", 67800.0, 2.30, AssetType.CRYPTO, "Bitcoin"),
        AssetPreset("SOL/USDT", 178.0, 4.10, AssetType.CRYPTO, "Solana"),
        AssetPreset("CRUDE OIL", 71.50, -0.80, AssetType.GOLD, "Crude Oil")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_custom_asset_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddChart,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Add Market Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add custom instruments & quotes directly",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Presets
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                symbol = preset.symbol
                                priceText = preset.price.toString()
                                changeText = preset.change.toString()
                                selectedType = preset.type
                                errorMessage = null
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = preset.symbol,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.label,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Asset Type Chips
            Text(
                text = "Asset Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssetType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Symbol & Price fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = {
                        symbol = it.uppercase()
                        errorMessage = null
                    },
                    label = { Text("Pair / Symbol") },
                    placeholder = { Text("e.g. XAU/USD") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_symbol_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        errorMessage = null
                    },
                    label = { Text("Current Price") },
                    placeholder = { Text("e.g. 2650.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_price_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 24h Change & High/Low fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = changeText,
                    onValueChange = { changeText = it },
                    label = { Text("24h Change %") },
                    placeholder = { Text("e.g. 1.25 or -0.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_change_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = volumeText,
                    onValueChange = { volumeText = it },
                    label = { Text("24h Volume (opt)") },
                    placeholder = { Text("e.g. 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_volume_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes / Strategy
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Description (Optional)") },
                placeholder = { Text("e.g. Tracking key resistance at 2670") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_notes_input"),
                shape = RoundedCornerShape(10.dp)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val cleanSym = symbol.trim().uppercase()
                        val price = priceText.toDoubleOrNull()
                        val change = changeText.toDoubleOrNull() ?: 0.0
                        val high = highText.toDoubleOrNull() ?: 0.0
                        val low = lowText.toDoubleOrNull() ?: 0.0
                        val vol = volumeText.toDoubleOrNull() ?: 0.0

                        if (cleanSym.isBlank()) {
                            errorMessage = "Please enter a symbol (e.g. XAU/USD, BTC/USDT)"
                            return@Button
                        }
                        if (price == null || price <= 0) {
                            errorMessage = "Please enter a valid positive price"
                            return@Button
                        }

                        onSaveAsset(
                            cleanSym,
                            price,
                            change,
                            selectedType,
                            high,
                            low,
                            vol,
                            notes
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_custom_asset_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Add to Markets")
                }
            }
        }
    }
}
