package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.example.data.model.AssetType
import com.example.data.model.MarketItem
import com.example.ui.components.AdBannerView
import com.example.ui.components.AddCustomAssetSheet
import com.example.ui.components.MarketCard
import com.example.ui.components.MoreAppsBottomSheet
import com.example.ui.components.MoreAppsCardButton
import com.example.ui.components.RiskDisclaimerBanner
import com.example.ui.components.VipBadgeTrigger
import com.example.ui.components.VipPassModalSheet
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BullishGreen
import com.example.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MarketViewModel,
    onMarketClick: (MarketItem) -> Unit,
    onNavigateToHealth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.marketItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedAssetFilter.collectAsState()
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    var showAddCustomSheet by remember { mutableStateOf(false) }
    var showMoreAppsSheet by remember { mutableStateOf(false) }
    var showVipSheet by remember { mutableStateOf(false) }

    val filteredItems = items.filter { item ->
        val matchesSearch = item.symbol.contains(searchQuery, ignoreCase = true) ||
                item.baseAsset.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedFilter == null || item.assetType == selectedFilter
        matchesSearch && matchesType
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Markets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(BullishGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Binance • Metals • Forex • Custom Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = BullishGreen,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Relocated Rewarded VIP Pass trigger badge
                    VipBadgeTrigger(
                        onClick = { showVipSheet = true },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    IconButton(
                        onClick = { showAddCustomSheet = true },
                        modifier = Modifier.testTag("top_add_market_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddChart,
                            contentDescription = "Add Market Data",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToHealth,
                        modifier = Modifier.testTag("provider_health_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "System Health",
                            tint = AccentCyan
                        )
                    }
                    IconButton(
                        onClick = { viewModel.loadMarkets() },
                        modifier = Modifier.testTag("refresh_markets_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 12.dp, end = 4.dp)
                    .testTag("fab_add_custom_market_data")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Market Data")
            }
        },
        modifier = modifier.testTag("home_screen_scaffold")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("market_search_field"),
                    placeholder = { Text("Search pair (e.g. BTC, XAU, EUR)...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Consolidated "More Apps" category button (Replaces multiple individual cards & bulky banners)
            item {
                MoreAppsCardButton(
                    onClick = { showMoreAppsSheet = true }
                )
            }

            // Google AdMob Banner Ad
            item {
                AdBannerView(modifier = Modifier.padding(vertical = 2.dp))
            }

            // Asset Type Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { viewModel.setAssetFilter(null) },
                        label = { Text("All Markets", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )

                    AssetType.values().forEach { type ->
                        FilterChip(
                            selected = selectedFilter == type,
                            onClick = { viewModel.setAssetFilter(type) },
                            label = { Text(type.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("filter_${type.name.lowercase()}")
                        )
                    }
                }
            }

            // Risk Disclaimer Banner
            item {
                RiskDisclaimerBanner()
            }

            // Market Cards List
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoading) "Loading market feeds..." else "No market pairs match query.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredItems, key = { it.symbol }) { item ->
                    val isFav = watchlistItems.any { it.symbol == item.symbol && it.isFavorite }
                    MarketCard(
                        item = item,
                        onClick = { onMarketClick(item) },
                        onFavoriteToggle = { viewModel.toggleFavorite(item.symbol, item.assetType) },
                        isFavorite = isFav,
                        onDeleteCustom = if (item.isCustom) {
                            { viewModel.deleteCustomAsset(item.symbol) }
                        } else null
                    )
                }
            }
        }

        if (showAddCustomSheet) {
            AddCustomAssetSheet(
                onDismiss = { showAddCustomSheet = false },
                onSaveAsset = { symbol, price, change24h, assetType, high24h, low24h, volume24h, notes ->
                    viewModel.addOrUpdateCustomAsset(
                        symbol = symbol,
                        price = price,
                        change24h = change24h,
                        assetType = assetType,
                        high24h = high24h,
                        low24h = low24h,
                        volume24h = volume24h,
                        notes = notes
                    )
                }
            )
        }

        if (showMoreAppsSheet) {
            MoreAppsBottomSheet(
                onDismiss = { showMoreAppsSheet = false }
            )
        }

        if (showVipSheet) {
            VipPassModalSheet(
                onDismiss = { showVipSheet = false }
            )
        }
    }
}
