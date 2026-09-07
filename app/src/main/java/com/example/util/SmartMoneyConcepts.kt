package com.example.util

import com.example.data.model.Candle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class PriceLevel(
    val price: Double,
    val type: String, // "Support", "Resistance", "Order Block", "FVG"
    val strength: String, // "Strong", "Moderate", "Minor"
    val description: String
)

data class StructureEvent(
    val type: String, // "BOS", "CHOCH", "FVG", "ORDER_BLOCK"
    val price: Double,
    val candleIndex: Int,
    val bias: String, // "BULLISH", "BEARISH"
    val description: String
)

data class SMCAnalysisResult(
    val trend: String,
    val structure: String,
    val supportLevels: List<PriceLevel>,
    val resistanceLevels: List<PriceLevel>,
    val structureEvents: List<StructureEvent>,
    val suggestedBias: String,
    val potentialEntry: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val riskReward: Double,
    val momentum: String,
    val summary: String
)

object SmartMoneyConcepts {

    fun analyzeCandles(candles: List<Candle>): SMCAnalysisResult {
        if (candles.size < 10) {
            return SMCAnalysisResult(
                trend = "Neutral",
                structure = "Insufficient data",
                supportLevels = emptyList(),
                resistanceLevels = emptyList(),
                structureEvents = emptyList(),
                suggestedBias = "NEUTRAL",
                potentialEntry = 0.0,
                stopLoss = 0.0,
                takeProfit1 = 0.0,
                takeProfit2 = 0.0,
                riskReward = 0.0,
                momentum = "Neutral",
                summary = "Insufficient candlestick data to formulate a high-probability market structure setup."
            )
        }

        val lastClose = candles.last().close
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }

        // Find swing highs and swing lows (fractal approach: higher than 2 bars on left and 2 on right)
        val swingHighs = mutableListOf<Pair<Int, Double>>()
        val swingLows = mutableListOf<Pair<Int, Double>>()

        for (i in 2 until candles.size - 2) {
            val h = candles[i].high
            if (h > candles[i - 1].high && h > candles[i - 2].high && h > candles[i + 1].high && h > candles[i + 2].high) {
                swingHighs.add(i to h)
            }
            val l = candles[i].low
            if (l < candles[i - 1].low && l < candles[i - 2].low && l < candles[i + 1].low && l < candles[i + 2].low) {
                swingLows.add(i to l)
            }
        }

        val structureEvents = mutableListOf<StructureEvent>()

        // Detect Fair Value Gaps (FVG)
        // Bullish FVG: candle[i-2].high < candle[i].low (gap between candle 0 high and candle 2 low)
        // Bearish FVG: candle[i-2].low > candle[i].high
        for (i in 2 until candles.size) {
            val cPrev = candles[i - 2]
            val cCurr = candles[i]
            if (cCurr.low > cPrev.high) {
                structureEvents.add(
                    StructureEvent(
                        type = "FVG",
                        price = (cCurr.low + cPrev.high) / 2.0,
                        candleIndex = i,
                        bias = "BULLISH",
                        description = "Bullish Fair Value Gap imbalance between ${String.format("%.2f", cPrev.high)} and ${String.format("%.2f", cCurr.low)}"
                    )
                )
            } else if (cCurr.high < cPrev.low) {
                structureEvents.add(
                    StructureEvent(
                        type = "FVG",
                        price = (cCurr.high + cPrev.low) / 2.0,
                        candleIndex = i,
                        bias = "BEARISH",
                        description = "Bearish Fair Value Gap imbalance between ${String.format("%.2f", cPrev.low)} and ${String.format("%.2f", cCurr.high)}"
                    )
                )
            }
        }

        // Detect Break of Structure (BOS) and Change of Character (CHOCH)
        var trend = "Neutral"
        if (swingHighs.size >= 2 && swingLows.size >= 2) {
            val lastTwoHighs = swingHighs.takeLast(2)
            val lastTwoLows = swingLows.takeLast(2)

            val higherHighs = lastTwoHighs[1].second > lastTwoHighs[0].second
            val higherLows = lastTwoLows[1].second > lastTwoLows[0].second
            val lowerHighs = lastTwoHighs[1].second < lastTwoHighs[0].second
            val lowerLows = lastTwoLows[1].second < lastTwoLows[0].second

            if (higherHighs && higherLows) {
                trend = "Bullish"
                structureEvents.add(
                    StructureEvent(
                        type = "BOS",
                        price = lastTwoHighs[1].second,
                        candleIndex = lastTwoHighs[1].first,
                        bias = "BULLISH",
                        description = "Bullish Break of Structure confirming higher highs"
                    )
                )
            } else if (lowerHighs && lowerLows) {
                trend = "Bearish"
                structureEvents.add(
                    StructureEvent(
                        type = "BOS",
                        price = lastTwoLows[1].second,
                        candleIndex = lastTwoLows[1].first,
                        bias = "BEARISH",
                        description = "Bearish Break of Structure confirming lower lows"
                    )
                )
            } else if (higherHighs && lowerLows) {
                trend = "Volatile Expanding"
                structureEvents.add(
                    StructureEvent(
                        type = "CHOCH",
                        price = lastTwoHighs[1].second,
                        candleIndex = lastTwoHighs[1].first,
                        bias = "BULLISH",
                        description = "Change of Character (CHOCH) shift detected"
                    )
                )
            } else {
                trend = "Range-Bound / Neutral"
            }
        }

        // Detect Order Blocks (Last opposing candle before an explosive displacement move)
        for (i in 1 until candles.size - 2) {
            val c = candles[i]
            val nextC = candles[i + 1]
            // Bullish Order Block: Down candle followed by sharp upside break
            if (c.close < c.open && nextC.close > c.high && (nextC.close - nextC.open) > (c.open - c.close) * 1.5) {
                structureEvents.add(
                    StructureEvent(
                        type = "ORDER_BLOCK",
                        price = (c.open + c.close) / 2.0,
                        candleIndex = i,
                        bias = "BULLISH",
                        description = "Bullish Demand Order Block at ${String.format("%.2f", c.low)}"
                    )
                )
            }
            // Bearish Order Block: Up candle followed by sharp downside break
            if (c.close > c.open && nextC.close < c.low && (nextC.open - nextC.close) > (c.close - c.open) * 1.5) {
                structureEvents.add(
                    StructureEvent(
                        type = "ORDER_BLOCK",
                        price = (c.open + c.close) / 2.0,
                        candleIndex = i,
                        bias = "BEARISH",
                        description = "Bearish Supply Order Block at ${String.format("%.2f", c.high)}"
                    )
                )
            }
        }

        // Determine key Support and Resistance levels from swing points
        val supports = swingLows.map {
            PriceLevel(it.second, "Support", "Strong", "Swing Low support at ${String.format("%.2f", it.second)}")
        }.sortedByDescending { it.price }.take(3)

        val resistances = swingHighs.map {
            PriceLevel(it.second, "Resistance", "Strong", "Swing High resistance at ${String.format("%.2f", it.second)}")
        }.sortedBy { it.price }.take(3)

        // Formulate potential trade setup
        val isBullish = trend.contains("Bullish") || lastClose > (candles.map { it.close }.average())
        val suggestedBias = if (isBullish) "BUY" else "SELL"

        val nearestSupport = supports.firstOrNull { it.price < lastClose }?.price ?: (lastClose * 0.98)
        val nearestResistance = resistances.firstOrNull { it.price > lastClose }?.price ?: (lastClose * 1.02)

        val potentialEntry = lastClose
        val stopLoss: Double
        val tp1: Double
        val tp2: Double

        if (suggestedBias == "BUY") {
            stopLoss = nearestSupport * 0.995
            val risk = max(0.001, potentialEntry - stopLoss)
            tp1 = potentialEntry + risk * 1.5
            tp2 = potentialEntry + risk * 2.5
        } else {
            stopLoss = nearestResistance * 1.005
            val risk = max(0.001, stopLoss - potentialEntry)
            tp1 = potentialEntry - risk * 1.5
            tp2 = potentialEntry - risk * 2.5
        }

        val riskDist = abs(potentialEntry - stopLoss)
        val rewardDist = abs(tp1 - potentialEntry)
        val riskReward = if (riskDist > 0) rewardDist / riskDist else 1.5

        val summary = if (isBullish) {
            "Structure favors Bullish continuation. Key demand zones identified below current price. Recommend monitoring for pullbacks into Bullish Order Blocks with defined risk."
        } else {
            "Market structure shows Bearish pressure. Key resistance and supply pools overhead. Monitor price action around premium levels for mitigation."
        }

        return SMCAnalysisResult(
            trend = trend,
            structure = if (structureEvents.isNotEmpty()) "${structureEvents.size} structural patterns identified" else "Standard Market Structure",
            supportLevels = supports,
            resistanceLevels = resistances,
            structureEvents = structureEvents.takeLast(6),
            suggestedBias = suggestedBias,
            potentialEntry = potentialEntry,
            stopLoss = stopLoss,
            takeProfit1 = tp1,
            takeProfit2 = tp2,
            riskReward = riskReward,
            momentum = if (isBullish) "Positive Momentum" else "Negative Momentum",
            summary = summary
        )
    }
}
