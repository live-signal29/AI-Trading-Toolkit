package com.example

import com.example.data.local.JournalEntry
import com.example.data.model.AssetType
import com.example.data.model.Candle
import com.example.data.model.DataStatus
import com.example.data.remote.BinancePublicProvider
import com.example.data.remote.ForexDataProvider
import com.example.data.remote.GoldApiProvider
import com.example.util.SmartMoneyConcepts
import com.example.util.TechnicalAnalysisEngine
import com.example.util.TradingCalculators
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TradingToolkitUnitTests {

    @Test
    fun testRiskCalculatorAccuracy() {
        // Balance = $10,000, Risk = 2% ($200), Entry = 100, Stop Loss = 95, Take Profit = 110
        val result = TradingCalculators.calculateRisk(
            accountBalance = 10_000.0,
            riskPercent = 2.0,
            entryPrice = 100.0,
            stopLoss = 95.0,
            takeProfit = 110.0,
            direction = "BUY",
            contractSize = 1.0
        )

        assertEquals(200.0, result.riskAmount, 0.001)
        assertEquals(200.0, result.potentialLoss, 0.001)
        assertEquals(400.0, result.potentialProfit, 0.001)
        assertEquals(2.0, result.riskRewardRatio, 0.001)
        assertEquals(40.0, result.positionSizeUnits, 0.001)
    }

    @Test
    fun testLotSizeCalculatorAccuracy() {
        // Balance = $10,000, Risk = 1% ($100), Entry = 1.0850, SL = 1.0800 (50 pips)
        val result = TradingCalculators.calculateLotSize(
            balance = 10_000.0,
            riskPercent = 1.0,
            entryPrice = 1.0850,
            stopLoss = 1.0800,
            pipSize = 0.0001,
            contractSize = 100_000.0
        )

        assertEquals(100.0, result.riskAmount, 0.001)
        assertEquals(50.0, result.pipDifference, 0.001)
        assertEquals(0.20, result.lotSize, 0.01)
        assertEquals(100.0, result.potentialLoss, 0.01)
    }

    @Test
    fun testPipCalculatorAccuracy() {
        val result = TradingCalculators.calculatePips(
            entryPrice = 1.0800,
            exitPrice = 1.0850,
            lotSize = 1.0,
            pair = "EUR/USD",
            contractSize = 100_000.0
        )

        assertEquals(50.0, result.pips, 0.001)
        assertEquals(500.0, result.totalProfitLoss, 0.01)
    }

    @Test
    fun testJournalAnalyticsMetrics() {
        val entries = listOf(
            JournalEntry(id = 1, symbol = "BTC/USDT", direction = "BUY", entryPrice = 60000.0, stopLoss = 59000.0, takeProfit = 62000.0, exitPrice = 62000.0, lotSize = 1.0, riskPercent = 1.0, strategy = "Breakout", session = "London", result = "WIN", profitAmount = 200.0, rMultiple = 2.0),
            JournalEntry(id = 2, symbol = "ETH/USDT", direction = "BUY", entryPrice = 3000.0, stopLoss = 2950.0, takeProfit = 3100.0, exitPrice = 2950.0, lotSize = 1.0, riskPercent = 1.0, strategy = "SMC", session = "NY", result = "LOSS", profitAmount = -100.0, rMultiple = -1.0),
            JournalEntry(id = 3, symbol = "BTC/USDT", direction = "BUY", entryPrice = 61000.0, stopLoss = 60000.0, takeProfit = 63000.0, exitPrice = 63000.0, lotSize = 1.0, riskPercent = 1.0, strategy = "Breakout", session = "London", result = "WIN", profitAmount = 200.0, rMultiple = 2.0)
        )

        val stats = TradingCalculators.computeJournalStatistics(entries)
        assertEquals(3, stats.totalTrades)
        assertEquals(2, stats.wins)
        assertEquals(1, stats.losses)
        assertEquals(66.66, stats.winRate, 0.5)
        assertEquals(300.0, stats.netPnl, 0.01)
        assertEquals(4.0, stats.profitFactor, 0.01) // 400 profit / 100 loss = 4.0
        assertEquals("BTC/USDT", stats.bestSymbol)
        assertEquals("Breakout", stats.bestStrategy)
    }

    @Test
    fun testTechnicalAnalysisIndicators() {
        val prices = listOf(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0)
        val sma5 = TechnicalAnalysisEngine.calculateSMA(prices, 5)
        assertTrue(sma5.isNotEmpty())
        assertEquals(18.0, sma5.last(), 0.01) // (16+17+18+19+20)/5 = 18.0

        val ema5 = TechnicalAnalysisEngine.calculateEMA(prices, 5)
        assertTrue(ema5.isNotEmpty())

        val rsi = TechnicalAnalysisEngine.calculateRSI(prices, 5)
        assertTrue(rsi.isNotEmpty())
        assertTrue(rsi.last() > 70.0) // Steady uptrend has high RSI
    }

    @Test
    fun testSmartMoneyConceptsEngine() {
        val candles = mutableListOf<Candle>()
        var price = 100.0
        for (i in 0..30) {
            val open = price
            val close = price + (if (i % 2 == 0) 2.0 else -1.0)
            val high = maxOf(open, close) + 1.0
            val low = minOf(open, close) - 1.0
            candles.add(Candle(time = i * 3600000L, open = open, high = high, low = low, close = close, volume = 1000.0))
            price = close
        }

        val smc = SmartMoneyConcepts.analyzeCandles(candles)
        assertNotNull(smc)
        assertTrue(smc.suggestedBias == "BUY" || smc.suggestedBias == "SELL" || smc.suggestedBias == "NEUTRAL")
        assertTrue(smc.potentialEntry > 0)
    }

    @Test
    fun testProvidersGracefulNoKeyHandling() = runBlocking {
        val goldProvider = GoldApiProvider()
        val items = goldProvider.getMarketItems()
        assertTrue(items.isNotEmpty())
        assertEquals("XAU/USD", items.first().symbol)
        assertTrue(items.first().price > 0)
        assertEquals(DataStatus.LIVE, items.first().status)

        val forexProvider = ForexDataProvider()
        val fxItems = forexProvider.getMarketItems()
        assertTrue(fxItems.isNotEmpty())
        assertTrue(fxItems.first().price > 0)
        assertEquals(DataStatus.LIVE, fxItems.first().status)
    }

    @Test
    fun testExternalLinksConfig() {
        assertTrue(com.example.util.ExternalLinks.APP_1_PLAY_STORE.contains("play.google.com"))
        assertTrue(com.example.util.ExternalLinks.APP_2_PLAY_STORE.contains("play.google.com"))
        assertTrue(com.example.util.ExternalLinks.DEVELOPER_PAGE.contains("play.google.com"))
        assertTrue(com.example.util.ExternalLinks.EXNESS_PARTNER_LINK.contains("exness"))
    }

    @Test
    fun testAdMobProductionConfiguration() {
        assertEquals("ca-app-pub-1895906484640218~5320396350", com.example.util.AdMobConfig.ADMOB_APP_ID)
        assertEquals("ca-app-pub-1895906484640218/4398925873", com.example.util.AdMobConfig.BANNER_AD_UNIT_ID)
        assertEquals("ca-app-pub-1895906484640218/1694695310", com.example.util.AdMobConfig.INTERSTITIAL_AD_UNIT_ID)
        assertEquals("ca-app-pub-1895906484640218/5536018777", com.example.util.AdMobConfig.REWARDED_AD_UNIT_ID)

        // Verify rewarded pro pass logic
        com.example.util.AdMobConfig.isProUser = false
        com.example.util.AdMobConfig.rewardedProExpiryTimestamp = 0L
        assertFalse(com.example.util.AdMobConfig.isRewardedProActive)

        // Simulate granting 2 hours
        val twoHoursFuture = System.currentTimeMillis() + 7_200_000L
        com.example.util.AdMobConfig.rewardedProExpiryTimestamp = twoHoursFuture
        assertTrue(com.example.util.AdMobConfig.isRewardedProActive)

        // Reset
        com.example.util.AdMobConfig.rewardedProExpiryTimestamp = 0L
    }

    @Test
    fun testCandlestickDataAcrossAssetClasses() = runBlocking {
        val repo = com.example.data.repository.MarketRepository()

        // 1. Crypto (BTC/USDT)
        val cryptoCandles = repo.getCandles("BTC/USDT", "1h", 20, 80000.0)
        assertTrue(cryptoCandles.isNotEmpty())
        assertTrue(cryptoCandles.first().close > 0)
        assertTrue(cryptoCandles.first().high >= cryptoCandles.first().low)

        // 2. Metals (XAU/USD)
        val goldCandles = repo.getCandles("XAU/USD", "1h", 20, 2900.0)
        assertTrue(goldCandles.isNotEmpty())
        assertTrue(goldCandles.first().close > 0)

        // 3. Forex (EUR/USD)
        val forexCandles = repo.getCandles("EUR/USD", "1h", 20, 1.08)
        assertTrue(forexCandles.isNotEmpty())
        assertTrue(forexCandles.first().close > 0)

        // 4. Index (US30)
        val indexCandles = repo.getCandles("US30", "1h", 20, 44000.0)
        assertTrue(indexCandles.isNotEmpty())
        assertTrue(indexCandles.first().close > 0)
    }
}
