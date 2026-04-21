package com.example.moodscribbles.ui.prototype

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.moodscribbles.R

/**
 * Skeleton: Insights with mood trend placeholder, top emotions bars, energy distribution strip.
 */
@Composable
fun InsightsPrototype(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrototypeColors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.prototype_insights_title),
            style = MaterialTheme.typography.headlineSmall,
            color = PrototypeColors.onBackground,
        )
        Text(
            text = stringResource(R.string.prototype_insights_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = PrototypeColors.onSurfaceMuted,
        )
        insightCard(title = stringResource(R.string.prototype_insights_mood_trend)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrototypeColors.surfaceCard),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.prototype_chart_placeholder),
                    style = MaterialTheme.typography.bodySmall,
                    color = PrototypeColors.onSurfaceMuted,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("Apr 1", "Apr 8", "Apr 15", "Apr 22", "Apr 29").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = PrototypeColors.onSurfaceMuted,
                    )
                }
            }
        }
        insightCard(title = stringResource(R.string.prototype_insights_top_emotions)) {
            val rows = listOf(
                Triple(stringResource(R.string.prototype_emotion_content), 0.32f, 0.32f),
                Triple(stringResource(R.string.prototype_emotion_happy), 0.24f, 0.24f),
                Triple(stringResource(R.string.prototype_emotion_neutral), 0.18f, 0.18f),
                Triple(stringResource(R.string.prototype_emotion_frustrated), 0.12f, 0.12f),
            )
            rows.forEach { (label, frac, prog) ->
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
                        color = PrototypeColors.onBackground,
                    )
                    LinearProgressIndicator(
                        progress = { prog },
                        modifier = Modifier
                            .weight(0.55f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrototypeColors.accent,
                        trackColor = PrototypeColors.surfaceCard,
                    )
                    Text(
                        text = "${(frac * 100).toInt()}%",
                        modifier = Modifier
                            .weight(0.1f)
                            .padding(start = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = PrototypeColors.onSurfaceMuted,
                    )
                }
            }
        }
        insightCard(title = stringResource(R.string.prototype_insights_energy_distribution)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .height(28.dp)
                        .background(PrototypeColors.accentMuted),
                )
                Box(
                    modifier = Modifier
                        .weight(0.55f)
                        .height(28.dp)
                        .background(PrototypeColors.accent.copy(alpha = 0.6f)),
                )
                Box(
                    modifier = Modifier
                        .weight(0.25f)
                        .height(28.dp)
                        .background(PrototypeColors.surfaceCard),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.prototype_energy_low),
                    style = MaterialTheme.typography.labelSmall,
                    color = PrototypeColors.onSurfaceMuted,
                )
                Text(
                    text = stringResource(R.string.prototype_energy_medium),
                    style = MaterialTheme.typography.labelSmall,
                    color = PrototypeColors.onSurfaceMuted,
                )
                Text(
                    text = stringResource(R.string.prototype_energy_high),
                    style = MaterialTheme.typography.labelSmall,
                    color = PrototypeColors.onSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun insightCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PrototypeColors.surface,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PrototypeColors.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
