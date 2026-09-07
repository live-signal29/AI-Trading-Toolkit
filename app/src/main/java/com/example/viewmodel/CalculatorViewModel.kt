package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.util.LotSizeCalcResult
import com.example.util.PipCalcResult
import com.example.util.PnlCalcResult
import com.example.util.RiskCalcResult
import com.example.util.TradingCalculators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel : ViewModel() {

    // 1. Risk Calculator State
    val riskBalance = MutableStateFlow("10000")
    val riskPercent = MutableStateFlow("1.5")
    val riskEntry = MutableStateFlow("65000")
    val riskStopLoss = MutableStateFlow("64000")
    val riskTakeProfit = MutableStateFlow("67500")
    val riskDirection = MutableStateFlow("BUY")
    val riskContractSize = MutableStateFlow("1") // 1 for crypto, 100 for gold, 100000 for forex

    private val _riskResult = MutableStateFlow<RiskCalcResult?>(null)
    val riskResult: StateFlow<RiskCalcResult?> = _riskResult.asStateFlow()

    // 2. Lot Size Calculator State
    val lotBalance = MutableStateFlow("10000")
    val lotRiskPercent = MutableStateFlow("1.0")
    val lotEntry = MutableStateFlow("1.0850")
    val lotStopLoss = MutableStateFlow("1.0800")
    val lotPipSize = MutableStateFlow("0.0001")
    val lotContractSize = MutableStateFlow("100000")

    private val _lotResult = MutableStateFlow<LotSizeCalcResult?>(null)
    val lotResult: StateFlow<LotSizeCalcResult?> = _lotResult.asStateFlow()

    // 3. Pip Calculator State
    val pipPair = MutableStateFlow("EUR/USD")
    val pipEntry = MutableStateFlow("1.0820")
    val pipExit = MutableStateFlow("1.0865")
    val pipLots = MutableStateFlow("1.0")
    val pipContractSize = MutableStateFlow("100000")

    private val _pipResult = MutableStateFlow<PipCalcResult?>(null)
    val pipResult: StateFlow<PipCalcResult?> = _pipResult.asStateFlow()

    // 4. Profit/Loss Calculator State
    val pnlEntry = MutableStateFlow("2400.00")
    val pnlExit = MutableStateFlow("2425.00")
    val pnlPositionSize = MutableStateFlow("1.0")
    val pnlDirection = MutableStateFlow("BUY")
    val pnlContractSize = MutableStateFlow("100") // e.g. Gold 100 oz
    val pnlStopLoss = MutableStateFlow("2390.00")

    private val _pnlResult = MutableStateFlow<PnlCalcResult?>(null)
    val pnlResult: StateFlow<PnlCalcResult?> = _pnlResult.asStateFlow()

    init {
        computeRisk()
        computeLotSize()
        computePip()
        computePnl()
    }

    fun computeRisk() {
        val bal = riskBalance.value.toDoubleOrNull() ?: 0.0
        val rPct = riskPercent.value.toDoubleOrNull() ?: 0.0
        val entry = riskEntry.value.toDoubleOrNull() ?: 0.0
        val sl = riskStopLoss.value.toDoubleOrNull() ?: 0.0
        val tp = riskTakeProfit.value.toDoubleOrNull() ?: 0.0
        val contract = riskContractSize.value.toDoubleOrNull() ?: 1.0

        _riskResult.value = TradingCalculators.calculateRisk(
            accountBalance = bal,
            riskPercent = rPct,
            entryPrice = entry,
            stopLoss = sl,
            takeProfit = tp,
            direction = riskDirection.value,
            contractSize = contract
        )
    }

    fun computeLotSize() {
        val bal = lotBalance.value.toDoubleOrNull() ?: 0.0
        val rPct = lotRiskPercent.value.toDoubleOrNull() ?: 0.0
        val entry = lotEntry.value.toDoubleOrNull() ?: 0.0
        val sl = lotStopLoss.value.toDoubleOrNull() ?: 0.0
        val pipSize = lotPipSize.value.toDoubleOrNull() ?: 0.0001
        val contract = lotContractSize.value.toDoubleOrNull() ?: 100000.0

        _lotResult.value = TradingCalculators.calculateLotSize(
            balance = bal,
            riskPercent = rPct,
            entryPrice = entry,
            stopLoss = sl,
            pipSize = pipSize,
            contractSize = contract
        )
    }

    fun computePip() {
        val entry = pipEntry.value.toDoubleOrNull() ?: 0.0
        val exit = pipExit.value.toDoubleOrNull() ?: 0.0
        val lots = pipLots.value.toDoubleOrNull() ?: 1.0
        val contract = pipContractSize.value.toDoubleOrNull() ?: 100000.0

        _pipResult.value = TradingCalculators.calculatePips(
            entryPrice = entry,
            exitPrice = exit,
            lotSize = lots,
            pair = pipPair.value,
            contractSize = contract
        )
    }

    fun computePnl() {
        val entry = pnlEntry.value.toDoubleOrNull() ?: 0.0
        val exit = pnlExit.value.toDoubleOrNull() ?: 0.0
        val size = pnlPositionSize.value.toDoubleOrNull() ?: 0.0
        val contract = pnlContractSize.value.toDoubleOrNull() ?: 1.0
        val sl = pnlStopLoss.value.toDoubleOrNull()

        _pnlResult.value = TradingCalculators.calculateProfitLoss(
            entryPrice = entry,
            exitPrice = exit,
            positionSize = size,
            direction = pnlDirection.value,
            contractSize = contract,
            stopLoss = sl
        )
    }
}
