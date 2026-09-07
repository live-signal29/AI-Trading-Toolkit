package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.util.Formatters
import com.example.util.TechnicalAnalysisEngine
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    showEMA20: Boolean = true,
    showEMA50: Boolean = false,
    showBollingerBands: Boolean = false,
    showVolume: Boolean = true
) {
    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    var crosshairPos by remember { mutableStateOf<Offset?>(null) }

    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp
                    )
                    Text(
                        text = "Loading live candlestick data...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Loading real-time market data...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val closes = remember(candles) { candles.map { it.close } }
    val ema20List = remember(candles) { TechnicalAnalysisEngine.calculateEMA(closes, 20) }
    val ema50List = remember(candles) { TechnicalAnalysisEngine.calculateEMA(closes, 50) }
    val bbResult = remember(candles) { TechnicalAnalysisEngine.calculateBollingerBands(closes, 20) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        // Selected Candle Info Overlay (OHLCV crosshair inspection)
        val inspectedCandle = selectedCandleIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()
        if (inspectedCandle != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "O: ${Formatters.formatPrice(inspectedCandle.open)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "H: ${Formatters.formatPrice(inspectedCandle.high)}",
                        fontSize = 11.sp,
                        color = BullishGreen
                    )
                    Text(
                        text = "L: ${Formatters.formatPrice(inspectedCandle.low)}",
                        fontSize = 11.sp,
                        color = BearishRed
                    )
                    Text(
                        text = "C: ${Formatters.formatPrice(inspectedCandle.close)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (inspectedCandle.close >= inspectedCandle.open) BullishGreen else BearishRed
                    )
                }
                Text(
                    text = Formatters.formatTimestamp(inspectedCandle.time),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Main Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .pointerInput(candles) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            change.consume()
                            val widthPerCandle = size.width / candles.size
                            val index = (change.position.x / widthPerCandle).toInt().coerceIn(0, candles.size - 1)
                            selectedCandleIndex = index
                            crosshairPos = change.position
                        },
                        onDragEnd = {
                            crosshairPos = null
                            selectedCandleIndex = null
                        }
                    )
                }
                .pointerInput(candles) {
                    detectTapGestures(
                        onTap = { pos ->
                            val widthPerCandle = size.width / candles.size
                            val index = (pos.x / widthPerCandle).toInt().coerceIn(0, candles.size - 1)
                            selectedCandleIndex = index
                            crosshairPos = pos
                        }
                    )
                }
                .testTag("candlestick_chart_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val minPrice = candles.minOf { it.low }
                val maxPrice = candles.maxOf { it.high }
                val priceRange = if (maxPrice == minPrice) 1.0 else (maxPrice - minPrice)

                val maxVolume = candles.maxOf { it.volume }.coerceAtLeast(1.0)

                val chartHeight = if (showVolume) size.height * 0.78f else size.height
                val volumeTop = size.height * 0.82f
                val volumeHeight = size.height * 0.18f

                val candleWidth = (size.width / candles.size)
                val bodyWidth = (candleWidth * 0.7f).coerceAtLeast(2f)

                // Draw Horizontal Grid Lines & Price Labels
                val gridSteps = 4
                for (i in 0..gridSteps) {
                    val y = chartHeight * (i.toFloat() / gridSteps)
                    val price = maxPrice - (priceRange * (i.toDouble() / gridSteps))
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Candlesticks and Volume
                candles.forEachIndexed { i, candle ->
                    val x = i * candleWidth + (candleWidth / 2)

                    val isBullish = candle.close >= candle.open
                    val candleColor = if (isBullish) BullishGreen else BearishRed

                    // Wick line
                    val wickYHigh = chartHeight - (((candle.high - minPrice) / priceRange) * chartHeight).toFloat()
                    val wickYLow = chartHeight - (((candle.low - minPrice) / priceRange) * chartHeight).toFloat()

                    drawLine(
                        color = candleColor,
                        start = Offset(x, wickYHigh),
                        end = Offset(x, wickYLow),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Candle Body
                    val bodyYOpen = chartHeight - (((candle.open - minPrice) / priceRange) * chartHeight).toFloat()
                    val bodyYClose = chartHeight - (((candle.close - minPrice) / priceRange) * chartHeight).toFloat()

                    val top = min(bodyYOpen, bodyYClose)
                    val height = max(abs(bodyYClose - bodyYOpen), 2f)

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(x - bodyWidth / 2, top),
                        size = Size(bodyWidth, height)
                    )

                    // Volume Bars
                    if (showVolume) {
                        val volNormalized = (candle.volume / maxVolume).toFloat()
                        val volBarHeight = (volNormalized * volumeHeight).coerceAtLeast(1f)
                        val volY = size.height - volBarHeight
                        drawRect(
                            color = candleColor.copy(alpha = 0.35f),
                            topLeft = Offset(x - bodyWidth / 2, volY),
                            size = Size(bodyWidth, volBarHeight)
                        )
                    }
                }

                // Draw EMA 20 Overlay Line
                if (showEMA20 && ema20List.isNotEmpty()) {
                    drawIndicatorLine(
                        data = ema20List,
                        totalPoints = candles.size,
                        minPrice = minPrice,
                        priceRange = priceRange,
                        chartHeight = chartHeight,
                        color = AccentCyan
                    )
                }

                // Draw EMA 50 Overlay Line
                if (showEMA50 && ema50List.isNotEmpty()) {
                    drawIndicatorLine(
                        data = ema50List,
                        totalPoints = candles.size,
                        minPrice = minPrice,
                        priceRange = priceRange,
                        chartHeight = chartHeight,
                        color = AccentGold
                    )
                }

                // Draw Bollinger Bands (Upper and Lower bands)
                if (showBollingerBands && bbResult.upper.isNotEmpty()) {
                    drawIndicatorLine(
                        data = bbResult.upper,
                        totalPoints = candles.size,
                        minPrice = minPrice,
                        priceRange = priceRange,
                        chartHeight = chartHeight,
                        color = Color(0xFF9333EA)
                    )
                    drawIndicatorLine(
                        data = bbResult.lower,
                        totalPoints = candles.size,
                        minPrice = minPrice,
                        priceRange = priceRange,
                        chartHeight = chartHeight,
                        color = Color(0xFF9333EA)
                    )
                }

                // Draw Crosshair if active
                crosshairPos?.let { pos ->
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    // Vertical crosshair
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(pos.x, 0f),
                        end = Offset(pos.x, size.height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = pathEffect
                    )
                    // Horizontal crosshair
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(0f, pos.y),
                        end = Offset(size.width, pos.y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }
            }
        }

        // Indicator Legend Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showEMA20) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.dp)
                            .background(AccentCyan)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "EMA 20", fontSize = 10.sp, color = AccentCyan)
                }
            }
            if (showEMA50) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.dp)
                            .background(AccentGold)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "EMA 50", fontSize = 10.sp, color = AccentGold)
                }
            }
            if (showBollingerBands) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.dp)
                            .background(Color(0xFF9333EA))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Bollinger Bands", fontSize = 10.sp, color = Color(0xFF9333EA))
                }
            }
        }
    }
}

private fun DrawScope.drawIndicatorLine(
    data: List<Double>,
    totalPoints: Int,
    minPrice: Double,
    priceRange: Double,
    chartHeight: Float,
    color: Color
) {
    if (data.size < 2) return
    val stepX = size.width / totalPoints
    val offsetIndex = totalPoints - data.size

    val path = Path()
    data.forEachIndexed { i, value ->
        val x = (i + offsetIndex) * stepX + (stepX / 2)
        val y = chartHeight - (((value - minPrice) / priceRange) * chartHeight).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 1.5.dp.toPx())
    )
}
