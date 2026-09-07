package com.example.data.model

enum class AssetType {
    CRYPTO,
    GOLD,
    FOREX,
    INDICES
}

enum class DataStatus {
    LIVE,
    DELAYED,
    UNAVAILABLE
}

data class MarketItem(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val price: Double,
    val change24h: Double,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24h: Double = 0.0,
    val assetType: AssetType,
    val source: String,
    val status: DataStatus,
    val lastUpdate: Long = System.currentTimeMillis(),
    val sparkline: List<Double> = emptyList(),
    val statusMessage: String? = null,
    val isCustom: Boolean = false
)

data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)
