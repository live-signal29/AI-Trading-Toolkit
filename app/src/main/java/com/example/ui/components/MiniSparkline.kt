package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen

@Composable
fun MiniSparkline(
    data: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val strokeColor = if (isPositive) BullishGreen else BearishRed
    val gradientColor = if (isPositive) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f)

    Canvas(modifier = modifier.fillMaxSize()) {
        if (data.size < 2) {
            // Draw a subtle flat line if data is minimal
            drawLine(
                color = strokeColor.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val minVal = data.minOrNull() ?: 0.0
        val maxVal = data.maxOrNull() ?: 1.0
        val range = if (maxVal == minVal) 1.0 else (maxVal - minVal)

        val stepX = size.width / (data.size - 1)

        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val normalized = ((value - minVal) / range).toFloat()
            val y = size.height - (normalized * (size.height * 0.8f) + (size.height * 0.1f))

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientColor, Color.Transparent),
                startY = 0f,
                endY = size.height
            )
        )

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
