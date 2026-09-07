package com.example.data.model

data class SignalItem(
    val id: String,
    val symbol: String,
    val direction: String, // "BUY" or "SELL"
    val entry: Double,
    val stopLoss: Double,
    val tp1: Double,
    val tp2: Double,
    val riskReward: Double,
    val timeframe: String,
    val analysis: String,
    val createdTime: Long = System.currentTimeMillis(),
    val dataSource: String,
    val isReliable: Boolean = true,
    val label: String = "Technical Analysis"
)

data class ScannerResult(
    val symbol: String,
    val price: Double,
    val trend: String, // "Bullish", "Bearish", "Neutral", "Strong Momentum"
    val rsi: Double,
    val momentum: String, // "Oversold", "Overbought", "Breakout", "Neutral"
    val setupQuality: String, // "High", "Medium", "Developing"
    val change24h: Double,
    val tags: List<String>,
    val assetType: AssetType
)

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val category: String, // Forex, Gold, Crypto, Markets, Central Banks, Economy
    val publishedAt: Long,
    val url: String? = null
)

data class CalendarItem(
    val id: String,
    val event: String,
    val currency: String,
    val country: String,
    val impact: String, // "High", "Medium", "Low"
    val date: String,
    val time: String,
    val forecast: String,
    val previous: String,
    val actual: String? = null
)

data class ProviderHealth(
    val name: String,
    val status: String, // "available", "configured", "not_configured", "unavailable"
    val description: String,
    val isOptional: Boolean = false
)
