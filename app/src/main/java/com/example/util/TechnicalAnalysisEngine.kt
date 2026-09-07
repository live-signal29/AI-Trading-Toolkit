package com.example.util

import com.example.data.model.Candle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class MacdResult(
    val macd: List<Double>,
    val signal: List<Double>,
    val histogram: List<Double>
)

data class BollingerBandsResult(
    val upper: List<Double>,
    val middle: List<Double>,
    val lower: List<Double>
)

object TechnicalAnalysisEngine {

    /**
     * Calculates Simple Moving Average (SMA)
     */
    fun calculateSMA(prices: List<Double>, period: Int): List<Double> {
        if (prices.size < period) return emptyList()
        val sma = mutableListOf<Double>()
        var sum = 0.0
        for (i in prices.indices) {
            sum += prices[i]
            if (i >= period) {
                sum -= prices[i - period]
            }
            if (i >= period - 1) {
                sma.add(sum / period)
            }
        }
        return sma
    }

    /**
     * Calculates Exponential Moving Average (EMA)
     */
    fun calculateEMA(prices: List<Double>, period: Int): List<Double> {
        if (prices.size < period) return emptyList()
        val ema = mutableListOf<Double>()
        val multiplier = 2.0 / (period + 1)

        // First EMA is SMA of first 'period' items
        var initialSma = 0.0
        for (i in 0 until period) {
            initialSma += prices[i]
        }
        initialSma /= period
        ema.add(initialSma)

        var currentEma = initialSma
        for (i in period until prices.size) {
            currentEma = (prices[i] - currentEma) * multiplier + currentEma
            ema.add(currentEma)
        }
        return ema
    }

    /**
     * Calculates Relative Strength Index (RSI)
     */
    fun calculateRSI(prices: List<Double>, period: Int = 14): List<Double> {
        if (prices.size <= period) return emptyList()
        val rsiList = mutableListOf<Double>()

        var gainSum = 0.0
        var lossSum = 0.0

        for (i in 1..period) {
            val change = prices[i] - prices[i - 1]
            if (change > 0) gainSum += change else lossSum += abs(change)
        }

        var avgGain = gainSum / period
        var avgLoss = lossSum / period

        val firstRsi = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + (avgGain / avgLoss)))
        rsiList.add(firstRsi)

        for (i in (period + 1) until prices.size) {
            val change = prices[i] - prices[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period

            val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
            val rsi = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + rs))
            rsiList.add(rsi)
        }

        return rsiList
    }

    /**
     * Calculates Moving Average Convergence Divergence (MACD)
     */
    fun calculateMACD(
        prices: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): MacdResult {
        if (prices.size < slowPeriod) {
            return MacdResult(emptyList(), emptyList(), emptyList())
        }
        val fastEma = calculateEMA(prices, fastPeriod)
        val slowEma = calculateEMA(prices, slowPeriod)

        val offset = slowPeriod - fastPeriod
        val macdLine = mutableListOf<Double>()
        for (i in slowEma.indices) {
            macdLine.add(fastEma[i + offset] - slowEma[i])
        }

        val signalLine = calculateEMA(macdLine, signalPeriod)
        val signalOffset = macdLine.size - signalLine.size
        val histogram = mutableListOf<Double>()

        for (i in signalLine.indices) {
            histogram.add(macdLine[i + signalOffset] - signalLine[i])
        }

        return MacdResult(macdLine, signalLine, histogram)
    }

    /**
     * Calculates Bollinger Bands
     */
    fun calculateBollingerBands(
        prices: List<Double>,
        period: Int = 20,
        multiplier: Double = 2.0
    ): BollingerBandsResult {
        if (prices.size < period) {
            return BollingerBandsResult(emptyList(), emptyList(), emptyList())
        }
        val sma = calculateSMA(prices, period)
        val upper = mutableListOf<Double>()
        val lower = mutableListOf<Double>()

        for (i in sma.indices) {
            val window = prices.subList(i, i + period)
            val mean = sma[i]
            val variance = window.sumOf { (it - mean) * (it - mean) } / period
            val stdDev = sqrt(variance)
            upper.add(mean + multiplier * stdDev)
            lower.add(mean - multiplier * stdDev)
        }

        return BollingerBandsResult(upper, sma, lower)
    }

    /**
     * Calculates Average True Range (ATR)
     */
    fun calculateATR(candles: List<Candle>, period: Int = 14): List<Double> {
        if (candles.size <= period) return emptyList()
        val trueRanges = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val c = candles[i]
            val prevClose = candles[i - 1].close
            val tr = max(c.high - c.low, max(abs(c.high - prevClose), abs(c.low - prevClose)))
            trueRanges.add(tr)
        }

        val atr = mutableListOf<Double>()
        var initialAtr = trueRanges.take(period).average()
        atr.add(initialAtr)

        var currentAtr = initialAtr
        for (i in period until trueRanges.size) {
            currentAtr = (currentAtr * (period - 1) + trueRanges[i]) / period
            atr.add(currentAtr)
        }
        return atr
    }
}
