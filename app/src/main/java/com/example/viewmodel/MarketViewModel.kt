package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CustomMarketAsset
import com.example.data.local.PriceAlert
import com.example.data.local.WatchlistItem
import com.example.data.model.AssetType
import com.example.data.model.Candle
import com.example.data.model.DataStatus
import com.example.data.model.MarketItem
import com.example.data.model.ProviderHealth
import com.example.data.model.ScannerResult
import com.example.data.model.SignalItem
import com.example.data.repository.MarketRepository
import com.example.util.NotificationHelper
import com.example.util.SMCAnalysisResult
import com.example.util.SmartMoneyConcepts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MarketRepository()
    private val database = AppDatabase.getDatabase(application)
    private val alertDao = database.priceAlertDao()
    private val watchlistDao = database.watchlistDao()
    private val customAssetDao = database.customMarketAssetDao()

    private val _marketItems = MutableStateFlow<List<MarketItem>>(emptyList())
    val marketItems: StateFlow<List<MarketItem>> = _marketItems.asStateFlow()

    val customAssets: StateFlow<List<CustomMarketAsset>> = customAssetDao.getAllCustomAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAssetFilter = MutableStateFlow<AssetType?>(null)
    val selectedAssetFilter: StateFlow<AssetType?> = _selectedAssetFilter.asStateFlow()

    private val _selectedMarketItem = MutableStateFlow<MarketItem?>(null)
    val selectedMarketItem: StateFlow<MarketItem?> = _selectedMarketItem.asStateFlow()

    private val _selectedCandles = MutableStateFlow<List<Candle>>(emptyList())
    val selectedCandles: StateFlow<List<Candle>> = _selectedCandles.asStateFlow()

    private val _isCandlesLoading = MutableStateFlow<Boolean>(false)
    val isCandlesLoading: StateFlow<Boolean> = _isCandlesLoading.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow<String>("1h")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _chartAnalysis = MutableStateFlow<SMCAnalysisResult?>(null)
    val chartAnalysis: StateFlow<SMCAnalysisResult?> = _chartAnalysis.asStateFlow()

    private val _signals = MutableStateFlow<List<SignalItem>>(emptyList())
    val signals: StateFlow<List<SignalItem>> = _signals.asStateFlow()

    private val _scannerResults = MutableStateFlow<List<ScannerResult>>(emptyList())
    val scannerResults: StateFlow<List<ScannerResult>> = _scannerResults.asStateFlow()

    private val _providerHealths = MutableStateFlow<List<ProviderHealth>>(emptyList())
    val providerHealths: StateFlow<List<ProviderHealth>> = _providerHealths.asStateFlow()

    val watchlistItems: StateFlow<List<WatchlistItem>> = watchlistDao.getAllWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val priceAlerts: StateFlow<List<PriceAlert>> = alertDao.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMarkets()
        loadHealth()
    }

    fun loadHealth() {
        _providerHealths.value = repository.getProviderHealthStatuses()
    }

    fun loadMarkets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val providerItems = repository.getCombinedMarketItems()
                val customList = customAssetDao.getAllCustomAssetsList()
                val customMarketItems = customList.map { custom ->
                    val assetTypeEnum = runCatching { AssetType.valueOf(custom.assetType.uppercase()) }.getOrDefault(AssetType.CRYPTO)
                    MarketItem(
                        symbol = custom.symbol,
                        baseAsset = custom.baseAsset,
                        quoteAsset = custom.quoteAsset,
                        price = custom.price,
                        change24h = custom.change24h,
                        high24h = if (custom.high24h > 0) custom.high24h else custom.price * (1.0 + abs(custom.change24h) / 100.0),
                        low24h = if (custom.low24h > 0) custom.low24h else custom.price * (1.0 - abs(custom.change24h) / 100.0),
                        volume24h = custom.volume24h,
                        assetType = assetTypeEnum,
                        source = custom.source,
                        status = DataStatus.LIVE,
                        lastUpdate = custom.updatedAt,
                        isCustom = true,
                        statusMessage = if (custom.notes.isNotBlank()) custom.notes else "Custom user asset"
                    )
                }

                // Custom items merge with provider items; custom items take priority if same symbol
                val combinedItems = customMarketItems + providerItems.filter { p ->
                    customMarketItems.none { c -> c.symbol.equals(p.symbol, ignoreCase = true) }
                }

                _marketItems.value = combinedItems
                checkPriceAlerts(combinedItems)

                // Run scanner and signal generation on active items
                val scannerData = repository.runMarketScanner(combinedItems)
                _scannerResults.value = scannerData

                val signalsData = repository.generateSignals(combinedItems)
                _signals.value = signalsData
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectMarketItem(item: MarketItem) {
        _selectedMarketItem.value = item
        loadCandles(item.symbol, _selectedTimeframe.value)
    }

    fun selectTimeframe(interval: String) {
        _selectedTimeframe.value = interval
        _selectedMarketItem.value?.let {
            loadCandles(it.symbol, interval)
        }
    }

    fun loadCandles(symbol: String, interval: String) {
        viewModelScope.launch {
            _isCandlesLoading.value = true
            try {
                val currentPrice = _selectedMarketItem.value?.price
                    ?: _marketItems.value.find { it.symbol.equals(symbol, ignoreCase = true) }?.price
                val candles = repository.getCandles(symbol, interval, 60, currentPrice)
                _selectedCandles.value = candles
                if (candles.isNotEmpty()) {
                    _chartAnalysis.value = SmartMoneyConcepts.analyzeCandles(candles)
                }
            } catch (e: Exception) {
                Log.e("MarketViewModel", "Error loading candles for $symbol: ${e.message}")
            } finally {
                _isCandlesLoading.value = false
            }
        }
    }

    fun addOrUpdateCustomAsset(
        symbol: String,
        price: Double,
        change24h: Double,
        assetType: AssetType,
        high24h: Double = 0.0,
        low24h: Double = 0.0,
        volume24h: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val cleanSymbol = symbol.trim().uppercase()
            val parts = cleanSymbol.split("/", "-", "_")
            val base = if (parts.size >= 2) parts[0] else cleanSymbol
            val quote = if (parts.size >= 2) parts[1] else "USD"
            val formattedSymbol = if (cleanSymbol.contains("/")) cleanSymbol else "$base/$quote"

            val custom = CustomMarketAsset(
                symbol = formattedSymbol,
                baseAsset = base,
                quoteAsset = quote,
                price = price,
                change24h = change24h,
                high24h = if (high24h > 0) high24h else price * (1.0 + abs(change24h) / 100.0),
                low24h = if (low24h > 0) low24h else price * (1.0 - abs(change24h) / 100.0),
                volume24h = volume24h,
                assetType = assetType.name,
                source = "User Custom Asset",
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
            customAssetDao.insertOrUpdate(custom)
            loadMarkets()
        }
    }

    fun deleteCustomAsset(symbol: String) {
        viewModelScope.launch {
            customAssetDao.deleteBySymbol(symbol)
            loadMarkets()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAssetFilter(filter: AssetType?) {
        _selectedAssetFilter.value = filter
    }

    fun toggleFavorite(symbol: String, assetType: AssetType) {
        viewModelScope.launch {
            val current = watchlistItems.value.find { it.symbol == symbol }
            if (current == null) {
                watchlistDao.insertItem(
                    WatchlistItem(
                        symbol = symbol,
                        assetType = assetType.name,
                        isFavorite = true
                    )
                )
            } else {
                watchlistDao.setFavorite(symbol, !current.isFavorite)
            }
        }
    }

    fun createPriceAlert(symbol: String, targetPrice: Double, condition: String) {
        viewModelScope.launch {
            val currentPrice = _marketItems.value.find { it.symbol == symbol }?.price ?: 0.0
            alertDao.insertAlert(
                PriceAlert(
                    symbol = symbol,
                    targetPrice = targetPrice,
                    condition = condition,
                    initialPrice = currentPrice
                )
            )
        }
    }

    fun deletePriceAlert(alertId: Long) {
        viewModelScope.launch {
            alertDao.deleteById(alertId)
        }
    }

    private fun checkPriceAlerts(currentItems: List<MarketItem>) {
        viewModelScope.launch {
            val activeAlerts = alertDao.getActiveAlerts()
            for (alert in activeAlerts) {
                val item = currentItems.find { it.symbol == alert.symbol }
                if (item != null && item.price > 0) {
                    val triggered = when (alert.condition) {
                        "ABOVE" -> item.price >= alert.targetPrice
                        "BELOW" -> item.price <= alert.targetPrice
                        else -> false
                    }

                    if (triggered) {
                        alertDao.updateAlert(alert.copy(isTriggered = true, isActive = false))
                        NotificationHelper.showPriceAlertNotification(
                            context = getApplication(),
                            title = "Price Alert Triggered: ${alert.symbol}",
                            message = "${alert.symbol} reached target of $${alert.targetPrice} (Current: $${item.price})"
                        )
                    }
                }
            }
        }
    }
}
