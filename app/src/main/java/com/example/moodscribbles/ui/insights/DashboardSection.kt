package com.example.moodscribbles.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moodscribbles.R
import com.example.moodscribbles.domain.metrics.DashboardMetrics
import com.example.moodscribbles.domain.metrics.EmotionFrequency
import com.example.moodscribbles.ui.localizedEmotionLabel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
@Composable
fun DashboardSection(
    metrics: DashboardMetrics,
    modifier: Modifier = Modifier,
    showPageHeader: Boolean = true,
) {
    val colorScheme = MaterialTheme.colorScheme
    val periodFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    }
    val subtitle = remember(metrics.periodStart, metrics.periodEnd) {
        val start = metrics.periodStart
        val end = metrics.periodEnd
        if (start != null && end != null) {
            "${start.format(periodFormatter)} – ${end.format(periodFormatter)}"
        } else {
            ""
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showPageHeader) {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onBackground,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
        if (metrics.entryCount == 0) {
            Text(
                text = stringResource(R.string.dashboard_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryCard(
                    title = stringResource(R.string.dashboard_avg_mood),
                    value = stringResource(
                        R.string.dashboard_avg_mood_value,
                        metrics.averageMoodScore ?: 0f,
                    ),
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    title = stringResource(R.string.dashboard_avg_energy),
                    value = stringResource(
                        R.string.dashboard_avg_energy_value,
                        (metrics.averageEnergy ?: 0f).roundToInt(),
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
            insightCard(
                title = stringResource(R.string.dashboard_mood_trend),
                colorScheme = colorScheme,
            ) {
                MoodTrendLineChart(
                    metrics = metrics,
                    colorScheme = colorScheme,
                )
            }
            insightCard(
                title = stringResource(R.string.dashboard_top_emotions),
                colorScheme = colorScheme,
            ) {
                metrics.emotionFrequencies.forEach { row ->
                    EmotionFrequencyRow(row = row, colorScheme = colorScheme)
                }
                if (metrics.emotionFrequencies.isEmpty()) {
                    Text(
                        text = stringResource(R.string.dashboard_no_emotions),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
            insightCard(
                title = stringResource(R.string.dashboard_energy_distribution),
                colorScheme = colorScheme,
            ) {
                EnergyGaugeBlock(
                    metrics = metrics,
                    colorScheme = colorScheme,
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
        ),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun insightCard(
    title: String,
    colorScheme: ColorScheme,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MoodTrendLineChart(
    metrics: DashboardMetrics,
    colorScheme: ColorScheme,
) {
    val trend = metrics.moodTrend
    if (trend.isEmpty()) {
        Text(
            text = stringResource(R.string.dashboard_chart_placeholder),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )
        return
    }
    val shortFmt = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    }
    val firstLabel = trend.first().date.format(shortFmt)
    val lastLabel = trend.last().date.format(shortFmt)

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surface),
        ) {
            val padH = 20.dp.toPx()
            val padV = 18.dp.toPx()
            val w = size.width
            val h = size.height
            val minScore = 1f
            val maxScore = 5f
            val n = trend.size
            fun xAt(i: Int): Float =
                if (n <= 1) {
                    w / 2f
                } else {
                    padH + (w - 2 * padH) * i / (n - 1).toFloat()
                }
            fun yAt(score: Float): Float {
                val t = (score - minScore) / (maxScore - minScore)
                val clamped = t.coerceIn(0f, 1f)
                return h - padV - clamped * (h - 2 * padV)
            }
            val gridColor = colorScheme.outline.copy(alpha = 0.28f)
            val strokeHair = 1.dp.toPx()
            for (g in 1..3) {
                val ty = h - padV - (h - 2 * padV) * g / 4f
                drawLine(
                    color = gridColor,
                    start = Offset(padH, ty),
                    end = Offset(w - padH, ty),
                    strokeWidth = strokeHair,
                )
            }
            val lineColor = colorScheme.primary
            val path = Path()
            trend.forEachIndexed { i, p ->
                val x = xAt(i)
                val y = yAt(p.moodScore)
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
            val dotOuter = 5.dp.toPx()
            val dotInner = 2.2.dp.toPx()
            trend.forEachIndexed { i, p ->
                val c = Offset(xAt(i), yAt(p.moodScore))
                drawCircle(color = lineColor, radius = dotOuter, center = c)
                drawCircle(color = colorScheme.surface, radius = dotInner, center = c)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = firstLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
            Text(
                text = lastLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EnergyGaugeBlock(
    metrics: DashboardMetrics,
    colorScheme: ColorScheme,
) {
    val fraction = ((metrics.averageEnergy ?: 0f).coerceIn(0f, 100f)) / 100f
    val pct = (metrics.averageEnergy ?: 0f).roundToInt().coerceIn(0, 100)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 14.dp.toPx()
                val pad = 10.dp.toPx()
                val ovalW = (size.width - 2 * pad).coerceAtLeast(40f)
                val ovalSize = Size(ovalW, ovalW)
                val topLeft = Offset((size.width - ovalW) / 2f, pad)
                val trackColor = colorScheme.outline.copy(alpha = 0.35f)
                drawArc(
                    color = trackColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = ovalSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )
                if (fraction > 0.001f) {
                    drawArc(
                        color = colorScheme.primary,
                        startAngle = 180f,
                        sweepAngle = 180f * fraction,
                        useCenter = false,
                        topLeft = topLeft,
                        size = ovalSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    )
                }
                if (fraction > 0.001f) {
                    val sweepDeg = 180.0 * fraction
                    val angleRad = Math.toRadians(180.0 + sweepDeg)
                    val cx = topLeft.x + ovalSize.width / 2f
                    val cy = topLeft.y + ovalSize.height / 2f
                    val rx = ovalSize.width / 2f
                    val ry = ovalSize.height / 2f
                    val ix = (cx + rx * cos(angleRad)).toFloat()
                    val iy = (cy + ry * sin(angleRad)).toFloat()
                    drawCircle(color = colorScheme.primary, radius = 7.dp.toPx(), center = Offset(ix, iy))
                    drawCircle(color = colorScheme.surface, radius = 3.5.dp.toPx(), center = Offset(ix, iy))
                }
            }
            Text(
                text = stringResource(R.string.dashboard_energy_gauge_center, pct),
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center).padding(bottom = 12.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.dashboard_gauge_low_short),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
            )
            Text(
                text = stringResource(R.string.dashboard_gauge_mid_short),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.dashboard_gauge_high_short),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun EmotionFrequencyRow(
    row: EmotionFrequency,
    colorScheme: ColorScheme,
) {
    val label = localizedEmotionLabel(row.emotionName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.35f),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurface,
        )
        LinearProgressIndicator(
            progress = { row.share },
            modifier = Modifier
                .weight(0.55f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = colorScheme.primary,
            trackColor = colorScheme.surface,
        )
        Text(
            text = stringResource(R.string.dashboard_emotion_percent, (row.share * 100).toInt()),
            modifier = Modifier
                .weight(0.1f)
                .padding(start = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

