package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AssetType
import com.example.data.model.Candle
import com.example.data.model.DataStatus
import com.example.data.model.MarketItem
import com.example.util.CandleGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GoldApiProvider(
    private val yahooFinanceProvider: YahooFinanceProvider = YahooFinanceProvider()
) : MarketDataProvider {

    override val name: String = "GoldAPI & Metals Feed"
    override val supportedTypes: Set<AssetType> = setOf(AssetType.GOLD)

    // Check if key is configured via backend or injected BuildConfig
    private val apiKey: String = runCatching {
        val field = BuildConfig::class.java.getField("GOLDAPI_KEY")
        field.get(null) as? String ?: ""
    }.getOrDefault("")

    override val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    override val statusDescription: String
        get() = if (isConfigured) "Configured (GoldAPI Feed Active)" else "Live Market Feed (Yahoo Finance Metals)"

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // These are only used if BOTH GoldAPI and Yahoo Finance are unreachable (e.g. no network).
    // They are fixed reference numbers, not real-time prices, so they must NEVER be tagged LIVE -
    // doing so previously made the app show a frozen/wrong gold price under a green "LIVE" badge.
    private val baselineMetals = listOf(
        MarketItem(
            symbol = "XAU/USD",
            baseAsset = "XAU",
            quoteAsset = "USD",
            price = 4476.50,
            change24h = 1.05,
            high24h = 4492.00,
            low24h = 4425.00,
            volume24h = 184500.0,
            assetType = AssetType.GOLD,
            source = "Gold Market Feed (Offline Reference Price)",
            status = DataStatus.DELAYED,
            lastUpdate = System.currentTimeMillis()
        ),
        MarketItem(
            symbol = "XAG/USD",
            baseAsset = "XAG",
            quoteAsset = "USD",
            price = 66.75,
            change24h = 3.20,
            high24h = 67.20,
            low24h = 64.80,
            volume24h = 82400.0,
            assetType = AssetType.GOLD,
            source = "Silver Market Feed (Offline Reference Price)",
            status = DataStatus.DELAYED,
            lastUpdate = System.currentTimeMillis()
        ),
        MarketItem(
            symbol = "XPT/USD",
            baseAsset = "XPT",
            quoteAsset = "USD",
            price = 1826.0,
            change24h = 0.85,
            high24h = 1838.0,
            low24h = 1810.0,
            volume24h = 14200.0,
            assetType = AssetType.GOLD,
            source = "Platinum Market Feed (Offline Reference Price)",
            status = DataStatus.DELAYED,
            lastUpdate = System.currentTimeMillis()
        ),
        MarketItem(
            symbol = "OIL/USD",
            baseAsset = "OIL",
            quoteAsset = "USD",
            price = 91.48,
            change24h = 1.40,
            high24h = 92.50,
            low24h = 90.10,
            volume24h = 240000.0,
            assetType = AssetType.GOLD,
            source = "Crude Oil Market Feed (Offline Reference Price)",
            status = DataStatus.DELAYED,
            lastUpdate = System.currentTimeMillis()
        )
    )

    override suspend fun getMarketItems(): List<MarketItem> = withContext(Dispatchers.IO) {
        // If user configured GoldAPI key, query GoldAPI first
        if (isConfigured) {
            val results = mutableListOf<MarketItem>()
            val metals = listOf("XAU" to "USD", "XAG" to "USD")

            for ((symbol, curr) in metals) {
                try {
                    val request = Request.Builder()
                        .url("https://www.goldapi.io/api/$symbol/$curr")
                        .addHeader("x-access-token", apiKey)
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            if (!body.isNullOrEmpty()) {
                                val json = JSONObject(body)
                                val price = json.optDouble("price", 0.0)
                                // GoldAPI.io renamed "chp" -> "change_percent" (and "ch" -> "change")
                                // in newer API versions. Support both so we don't silently show 0% change.
                                val change = when {
                                    json.has("change_percent") -> json.optDouble("change_percent", 0.0)
                                    json.has("chp") -> json.optDouble("chp", 0.0)
                                    else -> 0.0
                                }
                                val high = json.optDouble("high_price", 0.0)
                                val low = json.optDouble("low_price", 0.0)

                                // GoldAPI can return HTTP 200 with an {"error": "..."} body (e.g. rate
                                // limit or bad key). Skip those instead of showing a $0.00 price.
                                if (json.has("error") || price <= 0.0) {
                                    Log.w("GoldApiProvider", "Skipping invalid GoldAPI response for $symbol: $body")
                                } else {
                                    results.add(
                                        MarketItem(
                                            symbol = "$symbol/$curr",
                                            baseAsset = symbol,
                                            quoteAsset = curr,
                                            price = price,
                                            change24h = change,
                                            high24h = high,
                                            low24h = low,
                                            volume24h = 50000.0,
                                            assetType = AssetType.GOLD,
                                            source = "GoldAPI Live",
                                            status = DataStatus.LIVE,
                                            lastUpdate = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GoldApiProvider", "Error fetching $symbol: ${e.message}")
                }
            }

            if (results.isNotEmpty()) {
                return@withContext results
            }
        }

        // Live metals from Yahoo Finance (GC=F Gold, SI=F Silver, PL=F Platinum, CL=F Oil)
        try {
            val yahooMetals = yahooFinanceProvider.getMetalsItems()
            if (yahooMetals.isNotEmpty()) {
                return@withContext yahooMetals
            }
        } catch (e: Exception) {
            Log.e("GoldApiProvider", "Error fetching metals from Yahoo: ${e.message}")
        }

        baselineMetals
    }

    override suspend fun getCandles(symbol: String, interval: String, limit: Int): List<Candle> {
        val candles = yahooFinanceProvider.getCandles(symbol, interval, limit)
        if (candles.isNotEmpty()) {
            return candles
        }

        // Last resort only: anchor the generated candles to the current quote (live if
        // available) instead of a fixed 2025-era price, so the chart doesn't visibly
        // disagree with the price shown elsewhere in the app.
        val liveMatch = runCatching {
            getMarketItems().find { it.symbol.equals(symbol, ignoreCase = true) || symbol.uppercase().contains(it.baseAsset) }
        }.getOrNull()

        val basePrice = liveMatch?.price?.takeIf { it > 0.0 } ?: when {
            symbol.uppercase().contains("XAU") || symbol.uppercase().contains("GOLD") -> 4476.50
            symbol.uppercase().contains("XAG") || symbol.uppercase().contains("SILVER") -> 66.75
            symbol.uppercase().contains("XPT") || symbol.uppercase().contains("PLATINUM") -> 1826.0
            symbol.uppercase().contains("OIL") -> 91.48
            else -> 100.0
        }
        return CandleGenerator.generateCandles(symbol, basePrice, interval, limit)
    }
}
