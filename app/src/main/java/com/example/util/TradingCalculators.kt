package com.example.util

import com.example.data.local.JournalEntry
import kotlin.math.abs
import kotlin.math.max

data class RiskCalcResult(
    val riskAmount: Double,
    val potentialLoss: Double,
    val potentialProfit: Double,
    val riskRewardRatio: Double,
    val positionSizeUnits: Double,
    val lotSize: Double
)

data class LotSizeCalcResult(
    val riskAmount: Double,
    val lotSize: Double,
    val units: Double,
    val potentialLoss: Double,
    val pipDifference: Double
)

data class PipCalcResult(
    val pips: Double,
    val pipValuePerLot: Double,
    val totalProfitLoss: Double
)

data class PnlCalcResult(
    val profitLoss: Double,
    val percentageReturn: Double,
    val isProfit: Boolean,
    val riskReward: Double?
)

data class JournalStats(
    val totalTrades: Int,
    val wins: Int,
    val losses: Int,
    val breakevens: Int,
    val winRate: Double,
    val lossRate: Double,
    val netPnl: Double,
    val averageWin: Double,
    val averageLoss: Double,
    val profitFactor: Double,
    val averageR: Double,
    val winningStreak: Int,
    val losingStreak: Int,
    val maxDrawdown: Double,
    val bestSymbol: String,
    val bestStrategy: String,
    val bestSession: String
)

object TradingCalculators {

    /**
     * Calculates position risk and position size based on account balance and stop loss distance.
     */
    fun calculateRisk(
        accountBalance: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        direction: String = "BUY",
        contractSize: Double = 100_000.0 // Default forex standard lot, 1 for crypto, 100 for gold
    ): RiskCalcResult {
        if (accountBalance <= 0.0 || riskPercent <= 0.0 || entryPrice <= 0.0 || stopLoss <= 0.0) {
            return RiskCalcResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val riskAmount = accountBalance * (riskPercent / 100.0)
        val priceDiffLoss = abs(entryPrice - stopLoss)
        val priceDiffProfit = abs(takeProfit - entryPrice)

        if (priceDiffLoss == 0.0) {
            return RiskCalcResult(riskAmount, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val riskReward = if (priceDiffLoss > 0) priceDiffProfit / priceDiffLoss else 0.0

        // Units = Risk Amount / Price Difference per unit
        val positionUnits = riskAmount / priceDiffLoss
        val lotSize = if (contractSize > 0) positionUnits / contractSize else positionUnits

        val potentialLoss = positionUnits * priceDiffLoss
        val potentialProfit = positionUnits * priceDiffProfit

        return RiskCalcResult(
            riskAmount = riskAmount,
            potentialLoss = potentialLoss,
            potentialProfit = potentialProfit,
            riskRewardRatio = riskReward,
            positionSizeUnits = positionUnits,
            lotSize = lotSize
        )
    }

    /**
     * Calculates lot size for standard, mini, or micro forex/commodities contracts.
     */
    fun calculateLotSize(
        balance: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLoss: Double,
        pipSize: Double = 0.0001,
        contractSize: Double = 100_000.0
    ): LotSizeCalcResult {
        if (balance <= 0.0 || riskPercent <= 0.0 || entryPrice <= 0.0 || stopLoss <= 0.0 || pipSize <= 0.0) {
            return LotSizeCalcResult(0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val riskAmount = balance * (riskPercent / 100.0)
        val distance = abs(entryPrice - stopLoss)
        val pips = distance / pipSize

        if (pips == 0.0) {
            return LotSizeCalcResult(riskAmount, 0.0, 0.0, 0.0, 0.0)
        }

        // Pip value for 1 standard lot = contractSize * pipSize
        val pipValuePerLot = contractSize * pipSize
        val lotSize = riskAmount / (pips * pipValuePerLot)
        val units = lotSize * contractSize
        val potentialLoss = pips * pipValuePerLot * lotSize

        return LotSizeCalcResult(
            riskAmount = riskAmount,
            lotSize = lotSize,
            units = units,
            potentialLoss = potentialLoss,
            pipDifference = pips
        )
    }

    /**
     * Calculates Pip count, pip value, and total profit/loss for currency pairs.
     */
    fun calculatePips(
        entryPrice: Double,
        exitPrice: Double,
        lotSize: Double,
        pair: String = "EUR/USD",
        contractSize: Double = 100_000.0
    ): PipCalcResult {
        val isJpy = pair.uppercase().contains("JPY")
        val pipMultiplier = if (isJpy) 0.01 else 0.0001

        val priceDifference = abs(exitPrice - entryPrice)
        val pips = if (pipMultiplier > 0) priceDifference / pipMultiplier else 0.0

        val pipValuePerLot = contractSize * pipMultiplier
        val totalProfitLoss = pips * pipValuePerLot * lotSize

        return PipCalcResult(
            pips = pips,
            pipValuePerLot = pipValuePerLot,
            totalProfitLoss = totalProfitLoss
        )
    }

    /**
     * Calculates Profit / Loss and return percentage for any market asset.
     */
    fun calculateProfitLoss(
        entryPrice: Double,
        exitPrice: Double,
        positionSize: Double,
        direction: String = "BUY",
        contractSize: Double = 1.0,
        stopLoss: Double? = null
    ): PnlCalcResult {
        if (entryPrice <= 0.0 || positionSize <= 0.0) {
            return PnlCalcResult(0.0, 0.0, false, null)
        }

        val effectiveUnits = positionSize * contractSize
        val diff = if (direction.uppercase() == "BUY") (exitPrice - entryPrice) else (entryPrice - exitPrice)
        val pnl = diff * effectiveUnits
        val invested = entryPrice * effectiveUnits
        val percentage = if (invested > 0) (pnl / invested) * 100.0 else 0.0

        var rr: Double? = null
        if (stopLoss != null && stopLoss > 0) {
            val riskDiff = abs(entryPrice - stopLoss)
            if (riskDiff > 0) {
                rr = diff / riskDiff
            }
        }

        return PnlCalcResult(
            profitLoss = pnl,
            percentageReturn = percentage,
            isProfit = pnl >= 0.0,
            riskReward = rr
        )
    }

    /**
     * Computes complete analytics from journal entries.
     */
    fun computeJournalStatistics(entries: List<JournalEntry>): JournalStats {
        val closedEntries = entries.filter { it.result != "OPEN" }
        if (closedEntries.isEmpty()) {
            return JournalStats(
                totalTrades = entries.size,
                wins = 0,
                losses = 0,
                breakevens = 0,
                winRate = 0.0,
                lossRate = 0.0,
                netPnl = 0.0,
                averageWin = 0.0,
                averageLoss = 0.0,
                profitFactor = 0.0,
                averageR = 0.0,
                winningStreak = 0,
                losingStreak = 0,
                maxDrawdown = 0.0,
                bestSymbol = "N/A",
                bestStrategy = "N/A",
                bestSession = "N/A"
            )
        }

        val wins = closedEntries.filter { it.result == "WIN" }
        val losses = closedEntries.filter { it.result == "LOSS" }
        val breakevens = closedEntries.filter { it.result == "BREAKEVEN" }

        val winRate = (wins.size.toDouble() / closedEntries.size) * 100.0
        val lossRate = (losses.size.toDouble() / closedEntries.size) * 100.0

        val totalProfit = wins.sumOf { max(0.0, it.profitAmount) }
        val totalLoss = losses.sumOf { abs(it.profitAmount) }
        val netPnl = closedEntries.sumOf { it.profitAmount }

        val avgWin = if (wins.isNotEmpty()) totalProfit / wins.size else 0.0
        val avgLoss = if (losses.isNotEmpty()) totalLoss / losses.size else 0.0
        val profitFactor = if (totalLoss > 0.0) totalProfit / totalLoss else if (totalProfit > 0) totalProfit else 0.0
        val avgR = if (closedEntries.isNotEmpty()) closedEntries.sumOf { it.rMultiple } / closedEntries.size else 0.0

        // Streak calculation
        var maxWinStreak = 0
        var currentWinStreak = 0
        var maxLossStreak = 0
        var currentLossStreak = 0

        for (e in closedEntries.reversed()) { // chronological
            if (e.result == "WIN") {
                currentWinStreak++
                currentLossStreak = 0
                if (currentWinStreak > maxWinStreak) maxWinStreak = currentWinStreak
            } else if (e.result == "LOSS") {
                currentLossStreak++
                currentWinStreak = 0
                if (currentLossStreak > maxLossStreak) maxLossStreak = currentLossStreak
            } else {
                currentWinStreak = 0
                currentLossStreak = 0
            }
        }

        // Drawdown calculation
        var peak = 0.0
        var cumulative = 0.0
        var maxDd = 0.0

        for (e in closedEntries.reversed()) {
            cumulative += e.profitAmount
            if (cumulative > peak) {
                peak = cumulative
            }
            val dd = peak - cumulative
            if (dd > maxDd) {
                maxDd = dd
            }
        }

        // Best symbol, strategy, and session
        val bestSymbol = closedEntries.groupBy { it.symbol }
            .maxByOrNull { group -> group.value.sumOf { it.profitAmount } }?.key ?: "N/A"

        val bestStrategy = closedEntries.groupBy { it.strategy }
            .filter { it.key.isNotBlank() }
            .maxByOrNull { group -> group.value.sumOf { it.profitAmount } }?.key ?: "N/A"

        val bestSession = closedEntries.groupBy { it.session }
            .filter { it.key.isNotBlank() }
            .maxByOrNull { group -> group.value.sumOf { it.profitAmount } }?.key ?: "N/A"

        return JournalStats(
            totalTrades = entries.size,
            wins = wins.size,
            losses = losses.size,
            breakevens = breakevens.size,
            winRate = winRate,
            lossRate = lossRate,
            netPnl = netPnl,
            averageWin = avgWin,
            averageLoss = avgLoss,
            profitFactor = profitFactor,
            averageR = avgR,
            winningStreak = maxWinStreak,
            losingStreak = maxLossStreak,
            maxDrawdown = maxDd,
            bestSymbol = bestSymbol,
            bestStrategy = bestStrategy,
            bestSession = bestSession
        )
    }
}
