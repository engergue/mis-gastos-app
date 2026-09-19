package com.misgastos.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun BarChart(
    labels: List<String>,
    values: List<Double>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (values.isEmpty()) return@Canvas
            val slot = size.width / values.size
            val barWidth = slot * 0.5f
            val radius = (barWidth / 2.5f).coerceAtMost(14f)
            values.forEachIndexed { i, v ->
                val barHeight = (v / maxValue * (size.height - 6.dp.toPx())).toFloat().coerceAtLeast(4f)
                val x = i * slot + (slot - barWidth) / 2f
                val isLast = i == values.lastIndex
                drawRoundRect(
                    color = if (isLast) barColor else barColor.copy(alpha = 0.35f),
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            labels.forEach {
                Text(
                    text = it.take(3),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }
    }
}

/**
 * Gráfico de dona (donut). A diferencia de un pie chart tradicional, deja un hueco
 * en el centro pensado para superponer contenido (ej. el total) usando un Box.
 */
@Composable
fun DonutChart(
    values: List<Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidthDp: Float = 26f,
    gapDegrees: Float = 2.5f
) {
    val total = values.sum().coerceAtLeast(0.0001)
    Canvas(modifier = modifier.size(200.dp)) {
        val strokePx = strokeWidthDp.dp.toPx()
        val diameter = min(size.width, size.height) - strokePx
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f
        values.forEachIndexed { i, v ->
            val sweep = (v / total * 360f).toFloat()
            if (sweep > 0f) {
                val visualSweep = (sweep - gapDegrees).coerceAtLeast(1f)
                drawArc(
                    color = colors.getOrElse(i) { Color.Gray },
                    startAngle = startAngle + gapDegrees / 2f,
                    sweepAngle = visualSweep,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }
            startAngle += sweep
        }
    }
}

@Composable
fun LegendRow(color: Color, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(10.dp)) {
                drawRect(color = color, size = size)
            }
            Spacer(modifier = Modifier.padding(start = 6.dp))
            Text(text = label, fontSize = 12.sp)
        }
        Text(text = value, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    }
}

/** Barra de progreso delgada y redondeada, usada en la lista de categorías del dashboard. */
@Composable
fun ProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = color.copy(alpha = 0.15f)
) {
    Canvas(modifier = modifier.height(8.dp)) {
        val r = size.height / 2f
        drawRoundRect(
            color = trackColor,
            size = size,
            cornerRadius = CornerRadius(r, r)
        )
        val w = (size.width * progress.coerceIn(0f, 1f))
        if (w > 0f) {
            drawRoundRect(
                color = color,
                size = Size(w, size.height),
                cornerRadius = CornerRadius(r, r)
            )
        }
    }
}
