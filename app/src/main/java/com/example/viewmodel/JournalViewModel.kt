package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.JournalEntry
import com.example.util.JournalStats
import com.example.util.TradingCalculators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val journalDao = database.journalDao()

    val entries: StateFlow<List<JournalEntry>> = journalDao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _stats = MutableStateFlow<JournalStats>(
        TradingCalculators.computeJournalStatistics(emptyList())
    )
    val stats: StateFlow<JournalStats> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            entries.collect { list ->
                _stats.value = TradingCalculators.computeJournalStatistics(list)
            }
        }
    }

    fun addTrade(
        symbol: String,
        direction: String,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        exitPrice: Double?,
        lotSize: Double,
        riskPercent: Double,
        strategy: String,
        session: String,
        notes: String
    ) {
        viewModelScope.launch {
            val isClosed = exitPrice != null && exitPrice > 0
            val profit: Double
            val result: String
            val rMultiple: Double

            if (isClosed) {
                val diff = if (direction == "BUY") (exitPrice!! - entryPrice) else (entryPrice - exitPrice!!)
                val riskDist = abs(entryPrice - stopLoss)
                profit = diff * lotSize * 100_000.0 // Standard estimation for display or unit size
                rMultiple = if (riskDist > 0) diff / riskDist else 0.0
                result = when {
                    profit > 0.001 -> "WIN"
                    profit < -0.001 -> "LOSS"
                    else -> "BREAKEVEN"
                }
            } else {
                profit = 0.0
                result = "OPEN"
                rMultiple = 0.0
            }

            val entry = JournalEntry(
                symbol = symbol.uppercase(),
                direction = direction,
                entryPrice = entryPrice,
                stopLoss = stopLoss,
                takeProfit = takeProfit,
                exitPrice = exitPrice,
                lotSize = lotSize,
                riskPercent = riskPercent,
                strategy = strategy,
                session = session,
                notes = notes,
                result = result,
                profitAmount = profit,
                rMultiple = rMultiple
            )

            journalDao.insertEntry(entry)
        }
    }

    fun deleteTrade(id: Long) {
        viewModelScope.launch {
            journalDao.deleteById(id)
        }
    }
}
