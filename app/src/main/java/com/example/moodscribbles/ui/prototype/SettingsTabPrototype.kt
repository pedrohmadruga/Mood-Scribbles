package com.example.moodscribbles.ui.prototype

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.moodscribbles.R

@Composable
fun SettingsTabPrototype(
    onOpenFunctionalJournal: () -> Unit,
    onOpenMoodEntry: () -> Unit,
    onOpenJournalStep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrototypeColors.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.prototype_settings_title),
            style = MaterialTheme.typography.headlineSmall,
            color = PrototypeColors.onBackground,
        )
        Text(
            text = stringResource(R.string.prototype_settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = PrototypeColors.onSurfaceMuted,
        )
        TextButton(onClick = onOpenMoodEntry) {
            Text(stringResource(R.string.prototype_nav_mood_entry), color = PrototypeColors.accent)
        }
        TextButton(onClick = onOpenJournalStep) {
            Text(stringResource(R.string.prototype_nav_journal_step), color = PrototypeColors.accent)
        }
        TextButton(onClick = onOpenFunctionalJournal) {
            Text(stringResource(R.string.prototype_nav_functional_journal), color = PrototypeColors.accent)
        }
    }
}
