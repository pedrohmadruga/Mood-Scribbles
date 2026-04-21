package com.example.moodscribbles.ui.prototype

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.moodscribbles.R

private data class MoodCell(val labelRes: Int, val emoji: String)

/**
 * Skeleton: greeting, 3×3 mood grid, tag chips (multi-select stub).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodEntryPrototype(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val moods = remember {
        listOf(
            MoodCell(R.string.prototype_mood_ecstatic, "🤩"),
            MoodCell(R.string.prototype_mood_happy, "😊"),
            MoodCell(R.string.prototype_mood_content, "🙂"),
            MoodCell(R.string.prototype_mood_displeased, "😕"),
            MoodCell(R.string.prototype_mood_neutral, "😐"),
            MoodCell(R.string.prototype_mood_frustrated, "😤"),
            MoodCell(R.string.prototype_mood_annoyed, "😒"),
            MoodCell(R.string.prototype_mood_angry, "😠"),
            MoodCell(R.string.prototype_mood_furious, "🤬"),
        )
    }
    val tagLabels = remember {
        listOf(
            R.string.prototype_tag_work,
            R.string.prototype_tag_family,
            R.string.prototype_tag_relationship,
            R.string.prototype_tag_friends,
            R.string.prototype_tag_myself,
            R.string.prototype_tag_school,
            R.string.prototype_tag_coworkers,
            R.string.prototype_tag_health,
            R.string.prototype_tag_college,
        )
    }
    var selectedMoodIndex by remember { mutableStateOf(4) }
    var selectedTags by remember { mutableStateOf(setOf(0, 1)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrototypeColors.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = onClose) {
                Text("✕", color = PrototypeColors.onBackground)
            }
        }
        Text(
            text = stringResource(R.string.prototype_mood_entry_greeting),
            style = MaterialTheme.typography.headlineSmall,
            color = PrototypeColors.onBackground,
        )
        Text(
            text = stringResource(R.string.prototype_mood_entry_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = PrototypeColors.onSurfaceMuted,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(moods.size) { index ->
                val cell = moods[index]
                val selected = index == selectedMoodIndex
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrototypeColors.surfaceCard)
                        .then(
                            if (selected) {
                                Modifier.border(2.dp, PrototypeColors.selectedBorder, RoundedCornerShape(16.dp))
                            } else {
                                Modifier
                            },
                        )
                        .clickable { selectedMoodIndex = index }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = cell.emoji, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = stringResource(cell.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = PrototypeColors.onBackground,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.prototype_mood_entry_tags_heading),
            style = MaterialTheme.typography.titleSmall,
            color = PrototypeColors.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tagLabels.forEachIndexed { index, res ->
                val selected = index in selectedTags
                FilterChip(
                    selected = selected,
                    onClick = {
                        selectedTags = if (selected) selectedTags - index else selectedTags + index
                    },
                    label = { Text(stringResource(res)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrototypeColors.accent.copy(alpha = 0.45f),
                        containerColor = PrototypeColors.surfaceCard,
                        labelColor = PrototypeColors.onBackground,
                    ),
                )
            }
        }
    }
}
