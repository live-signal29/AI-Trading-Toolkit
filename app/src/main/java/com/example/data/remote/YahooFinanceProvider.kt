package com.example.data.remote

import android.util.Log
import com.example.data.model.AssetType
import com.example.data.model.Candle
import com.example.data.model.DataStatus
import com.example.data.model.MarketItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class YahooFinanceProvider : MarketDataProvider {

    override val name: String = "Yahoo Finance API"
    override val supportedTypes: Set<AssetType> = setOf(AssetType.FOREX, AssetType.INDICES, AssetType.GOLD)
    override val isConfigured: Boolean = true
    override val statusDescription: String = "Available (Public Live Feed)"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Pairs to fetch from Yahoo Finance
    data class YahooSymbolConfig(
        val yahooSymbol: String,
        val displaySymbol: String,
        val baseAsset: String,
        val quoteAsset: String,
        val assetType: AssetType
    )

    private val forexPairs = listOf(
        YahooSymbolConfig("EURUSD=X", "EUR/USD", "EUR", "USD", AssetType.FOREX),
        YahooSymbolConfig("GBPUSD=X", "GBP/USD", "GBP", "USD", AssetType.FOREX),
        YahooSymbolConfig("USDJPY=X", "USD/JPY", "USD", "JPY", AssetType.FOREX),
        YahooSymbolConfig("AUDUSD=X", "AUD/USD", "AUD", "USD", AssetType.FOREX),
        YahooSymbolConfig("USDCAD=X", "USD/CAD", "USD", "CAD", AssetType.FOREX),
        YahooSymbolConfig("USDCHF=X", "USD/CHF", "USD", "CHF", AssetType.FOREX),
        YahooSymbolConfig("GBPJPY=X", "GBP/JPY", "GBP", "JPY", AssetType.FOREX),
        YahooSymbolConfig("EURJPY=X", "EUR/JPY", "EUR", "JPY", AssetType.FOREX)
    )

    private val indicesPairs = listOf(
        YahooSymbolConfig("^DJI", "US30", "US30", "USD", AssetType.INDICES),
        YahooSymbolConfig("^IXIC", "NAS100", "NAS100", "USD", AssetType.INDICES),
        YahooSymbolConfig("^GSPC", "SPX500", "SPX500", "USD", AssetType.INDICES),
        YahooSymbolConfig("^GDAXI", "GER40", "GER40", "EUR", AssetType.INDICES)
    )

    private val metalsPairs = listOf(
        YahooSymbolConfig("GC=F", "XAU/USD", "XAU", "USD", AssetType.GOLD),
        YahooSymbolConfig("SI=F", "XAG/USD", "XAG", "USD", AssetType.GOLD),
        YahooSymbolConfig("PL=F", "XPT/USD", "XPT", "USD", AssetType.GOLD),
        YahooSymbolConfig("CL=F", "OIL/USD", "OIL", "USD", AssetType.GOLD)
    )

    override suspend fun getMarketItems(): List<MarketItem> = withContext(Dispatchers.IO) {
        val targets = forexPairs + indicesPairs
        val deferreds = targets.map { config ->
            async {
                fetchSingleQuote(config)
            }
        }
        val items = deferreds.awaitAll().filterNotNull()
        if (items.isNotEmpty()) {
            items
        } else {
            // If network fails, return fallback with current time
            emptyList()
        }
    }

    suspend fun getMetalsItems(): List<MarketItem> = withContext(Dispatchers.IO) {
        val deferreds = metalsPairs.map { config ->
            async {
                fetchSingleQuote(config)
            }
        }
        deferreds.awaitAll().filterNotNull()
    }

    private fun fetchSingleQuote(config: YahooSymbolConfig): MarketItem? {
        val url = "https://query1.finance.yahoo.com/v8/finance/chart/${config.yahooSymbol}?interval=1h&range=2d"
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w("YahooFinance", "Failed ${config.yahooSymbol}: ${response.code}")
                    return null
                }
                val body = response.body?.string() ?: return null
                val root = JSONObject(body)
                val chart = root.getJSONObject("chart")
                val resultArr = chart.getJSONArray("result")
                if (resultArr.length() == 0) return null
                val firstResult = resultArr.getJSONObject(0)
                val meta = firstResult.getJSONObject("meta")

                val price = meta.optDouble("regularMarketPrice", 0.0)
                val prevClose = if (meta.has("chartPreviousClose")) {
                    meta.optDouble("chartPreviousClose", price)
                } else {
                    meta.optDouble("previousClose", price)
                }

                val changePercent = if (prevClose > 0.0 && price > 0.0) {
                    ((price - prevClose) / prevClose) * 100.0
                } else {
                    0.0
                }

                val high = meta.optDouble("regularMarketDayHigh", price * 1.002)
                val low = meta.optDouble("regularMarketDayLow", price * 0.998)
                val volume = meta.optDouble("regularMarketVolume", 0.0)

                MarketItem(
                    symbol = config.displaySymbol,
                    baseAsset = config.baseAsset,
                    quoteAsset = config.quoteAsset,
                    price = price,
                    change24h = changePercent,
                    high24h = high,
                    low24h = low,
                    volume24h = volume,
                    assetType = config.assetType,
                    source = "Yahoo Finance Live",
                    status = DataStatus.LIVE,
                    lastUpdate = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e("YahooFinance", "Error fetching ${config.yahooSymbol}: ${e.message}")
            null
        }
    }

    override suspend fun getCandles(symbol: String, interval: String, limit: Int): List<Candle> = withContext(Dispatchers.IO) {
        val yahooSymbol = mapToYahooSymbol(symbol) ?: return@withContext emptyList()
        val lowerInterval = interval.lowercase().trim()
        val mappedInterval = when (lowerInterval) {
            "1m" -> "1m"
            "5m" -> "5m"
            "15m" -> "15m"
            "30m" -> "30m"
            "1h" -> "1h"
            "4h" -> "1h" // Aggregate 1h into 4h
            "1d" -> "1d"
            "1w", "1wk" -> "1wk"
            else -> "1h"
        }

        val range = when (mappedInterval) {
            "1m" -> "1d"
            "5m", "15m", "30m" -> "5d"
            "1h" -> if (lowerInterval == "4h") "3mo" else "1mo"
            "1d" -> "6mo"
            "1wk" -> "2y"
            else -> "1mo"
        }

        val primaryUrl = "https://query1.finance.yahoo.com/v8/finance/chart/$yahooSymbol?interval=$mappedInterval&range=$range"
        val fallbackUrl = "https://query2.finance.yahoo.com/v8/finance/chart/$yahooSymbol?interval=$mappedInterval&range=$range"

        for (url in listOf(primaryUrl, fallbackUrl)) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val root = JSONObject(body)
                    val chart = root.optJSONObject("chart") ?: return@use
                    val resultArr = chart.optJSONArray("result") ?: return@use
                    if (resultArr.length() == 0) return@use
                    val firstResult = resultArr.getJSONObject(0)

                    val timestampArr = firstResult.optJSONArray("timestamp") ?: return@use
                    val indicators = firstResult.optJSONObject("indicators") ?: return@use
                    val quoteArr = indicators.optJSONArray("quote") ?: return@use
                    if (quoteArr.length() == 0) return@use
                    val quote = quoteArr.getJSONObject(0)

                    val opens = quote.optJSONArray("open")
                    val highs = quote.optJSONArray("high")
                    val lows = quote.optJSONArray("low")
                    val closes = quote.optJSONArray("close")
                    val volumes = quote.optJSONArray("volume")

                    val validCandles = mutableListOf<Candle>()
                    val totalLength = timestampArr.length()

                    for (i in 0 until totalLength) {
                        val o = opens?.optDouble(i)
                        val h = highs?.optDouble(i)
                        val l = lows?.optDouble(i)
                        val c = closes?.optDouble(i)
                        val v = volumes?.optDouble(i) ?: 0.0
                        val ts = timestampArr.optLong(i, 0L) * 1000L

                        if (o != null && !o.isNaN() && o > 0.0 &&
                            c != null && !c.isNaN() && c > 0.0 &&
                            h != null && !h.isNaN() && h > 0.0 &&
                            l != null && !l.isNaN() && l > 0.0 && ts > 0L) {
                            validCandles.add(
                                Candle(
                                    time = ts,
                                    open = o,
                                    high = maxOf(h, maxOf(o, c)),
                                    low = minOf(l, minOf(o, c)),
                                    close = c,
                                    volume = v
                                )
                            )
                        }
                    }

                    if (validCandles.isNotEmpty()) {
                        if (lowerInterval == "4h") {
                            // Aggregate 1-hour candles into 4-hour candles
                            val aggregated = mutableListOf<Candle>()
                            val chunkSize = 4
                            for (i in validCandles.indices step chunkSize) {
                                val chunk = validCandles.subList(i, minOf(i + chunkSize, validCandles.size))
                                if (chunk.isNotEmpty()) {
                                    aggregated.add(
                                        Candle(
                                            time = chunk.first().time,
                                            open = chunk.first().open,
                                            high = chunk.maxOf { it.high },
                                            low = chunk.minOf { it.low },
                                            close = chunk.last().close,
                                            volume = chunk.sumOf { it.volume }
                                        )
                                    )
                                }
                            }
                            return@withContext aggregated.takeLast(limit)
                        } else {
                            return@withContext validCandles.takeLast(limit)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("YahooFinance", "Error fetching candles for $symbol ($yahooSymbol): ${e.message}")
            }
        }

        // If 1m was requested and returned empty (common during weekend/holidays), retry with 5m
        if (lowerInterval == "1m") {
            return@withContext getCandles(symbol, "5m", limit)
        }

        emptyList()
    }

    private fun mapToYahooSymbol(symbol: String): String? {
        val clean = symbol.uppercase().trim()
        val allConfigs = forexPairs + indicesPairs + metalsPairs
        val match = allConfigs.find { it.displaySymbol.equals(clean, ignoreCase = true) }
        if (match != null) return match.yahooSymbol

        return when {
            clean == "EUR/USD" || clean == "EURUSD" -> "EURUSD=X"
            clean == "GBP/USD" || clean == "GBPUSD" -> "GBPUSD=X"
            clean == "USD/JPY" || clean == "USDJPY" -> "USDJPY=X"
            clean == "AUD/USD" || clean == "AUDUSD" -> "AUDUSD=X"
            clean == "USD/CAD" || clean == "USDCAD" -> "USDCAD=X"
            clean == "USD/CHF" || clean == "USDCHF" -> "USDCHF=X"
            clean == "GBP/JPY" || clean == "GBPJPY" -> "GBPJPY=X"
            clean == "EUR/JPY" || clean == "EURJPY" -> "EURJPY=X"
            clean.contains("US30") || clean.contains("DJI") || clean.contains("DOW") -> "^DJI"
            clean.contains("NAS100") || clean.contains("NASDAQ") || clean.contains("NDX") -> "^IXIC"
            clean.contains("SPX500") || clean.contains("SPX") || clean.contains("S&P") -> "^GSPC"
            clean.contains("GER40") || clean.contains("DAX") -> "^GDAXI"
            clean.contains("XAU") || clean.contains("GOLD") -> "GC=F"
            clean.contains("XAG") || clean.contains("SILVER") -> "SI=F"
            clean.contains("XPT") || clean.contains("PLATINUM") -> "PL=F"
            clean.contains("OIL") || clean.contains("CRUDE") -> "CL=F"
            // Crypto on Yahoo Finance fallback
            clean == "BTC/USDT" || clean == "BTC/USD" || clean == "BTC" -> "BTC-USD"
            clean == "ETH/USDT" || clean == "ETH/USD" || clean == "ETH" -> "ETH-USD"
            clean == "SOL/USDT" || clean == "SOL/USD" || clean == "SOL" -> "SOL-USD"
            clean == "BNB/USDT" || clean == "BNB/USD" || clean == "BNB" -> "BNB-USD"
            clean == "XRP/USDT" || clean == "XRP/USD" || clean == "XRP" -> "XRP-USD"
            clean == "DOGE/USDT" || clean == "DOGE/USD" || clean == "DOGE" -> "DOGE-USD"
            clean == "ADA/USDT" || clean == "ADA/USD" || clean == "ADA" -> "ADA-USD"
            clean == "TRX/USDT" || clean == "TRX/USD" || clean == "TRX" -> "TRX-USD"
            clean.endsWith("/USD") -> "${clean.removeSuffix("/USD")}-USD"
            clean.endsWith("/USDT") -> "${clean.removeSuffix("/USDT")}-USD"
            // Direct equity ticker (e.g. AAPL, TSLA, NVDA)
            !clean.contains("/") && !clean.contains("-") && clean.length in 1..6 -> clean
            else -> null
        }
    }
}
