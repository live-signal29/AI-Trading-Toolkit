package com.example.util

import com.example.data.model.Candle
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object CandleGenerator {

    fun getTimeframeMillis(interval: String): Long {
        return when (interval.lowercase()) {
            "1m" -> 60_000L
            "5m" -> 300_000L
            "15m" -> 900_000L
            "30m" -> 1_800_000L
            "1h" -> 3_600_000L
            "4h" -> 14_400_000L
            "1d" -> 86_400_000L
            "1w" -> 604_800_000L
            else -> 3_600_000L
        }
    }

    fun generateCandles(
        symbol: String,
        currentPrice: Double,
        interval: String = "1h",
        count: Int = 60
    ): List<Candle> {
        val safePrice = if (currentPrice > 0) currentPrice else 100.0
        val intervalMillis = getTimeframeMillis(interval)
        val now = System.currentTimeMillis()

        // Volatility percentage per candle based on asset magnitude
        val volatilityPct = when {
            safePrice > 20000.0 -> 0.006  // BTC, US30
            safePrice > 1000.0 -> 0.004   // ETH, Gold
            safePrice < 2.0 -> 0.0015     // EUR/USD, GBP/USD
            else -> 0.005
        }

        val seed = abs(symbol.hashCode().toLong())
        val random = Random(seed)

        // Generate a smooth price path backwards from currentPrice
        val closes = mutableListOf<Double>()
        var runningPrice = safePrice
        closes.add(runningPrice)

        for (i in 1 until count) {
            val wave = sin(i * 0.2) * 0.5 + cos(i * 0.08) * 0.8
            val noise = (random.nextDouble() - 0.49) * 2.0
            val deltaPct = (wave * 0.4 + noise * 0.6) * volatilityPct
            runningPrice = (runningPrice / (1.0 + deltaPct)).coerceAtLeast(safePrice * 0.5)
            closes.add(runningPrice)
        }

        // Reverse so oldest candle comes first
        val chronologicalCloses = closes.reversed()
        val candles = mutableListOf<Candle>()

        for (i in 0 until count) {
            val close = chronologicalCloses[i]
            val prevClose = if (i > 0) chronologicalCloses[i - 1] else close * (1.0 - (random.nextDouble() - 0.5) * volatilityPct)
            val open = prevClose

            val range = abs(close - open)
            val maxBody = maxOf(open, close)
            val minBody = minOf(open, close)

            val upperWick = (random.nextDouble() * 0.8 + 0.2) * (range + close * volatilityPct * 0.5)
            val lowerWick = (random.nextDouble() * 0.8 + 0.2) * (range + close * volatilityPct * 0.5)

            val high = maxBody + upperWick
            val low = (minBody - lowerWick).coerceAtLeast(0.0001)

            val volume = (100.0 + random.nextDouble() * 500.0) * (range / (close * volatilityPct + 0.00001)).coerceAtLeast(0.5)
            val time = now - (count - 1 - i) * intervalMillis

            candles.add(
                Candle(
                    time = time,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )
            )
        }

        return candles
    }
}
