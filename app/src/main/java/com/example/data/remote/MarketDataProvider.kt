package com.example.data.remote

import com.example.data.model.AssetType
import com.example.data.model.Candle
import com.example.data.model.DataStatus
import com.example.data.model.MarketItem
import kotlinx.coroutines.flow.Flow

interface MarketDataProvider {
    val name: String
    val supportedTypes: Set<AssetType>
    val isConfigured: Boolean
    val statusDescription: String

    suspend fun getMarketItems(): List<MarketItem>
    suspend fun getCandles(symbol: String, interval: String, limit: Int = 100): List<Candle>
}
