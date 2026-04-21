package com.example.moodscribbles.ui.prototype

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.moodscribbles.R

/**
 * Skeleton: Step 2 — energy slider, title, journal body, primary save (no persistence).
 */
@Composable
fun JournalStepPrototype(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var energy by remember { mutableFloatStateOf(65f) }
    var title by remember { mutableStateOf("") }
    var journal by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrototypeColors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(onClick = onBack) {
                Text("←", color = PrototypeColors.onBackground)
            }
        }
        Text(
            text = stringResource(R.string.prototype_journal_step_indicator),
            style = MaterialTheme.typography.labelMedium,
            color = PrototypeColors.onSurfaceMuted,
        )
        Text(
            text = stringResource(R.string.prototype_journal_step_title),
            style = MaterialTheme.typography.headlineSmall,
            color = PrototypeColors.onBackground,
        )
        Spacer(modifier = Modifier.height(20.dp))
        prototypeCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.prototype_journal_energy_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = PrototypeColors.onBackground,
                )
                Text(
                    text = stringResource(R.string.prototype_journal_energy_max_badge),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrototypeColors.accent,
                )
            }
            Slider(
                value = energy,
                onValueChange = { energy = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = PrototypeColors.accent,
                    activeTrackColor = PrototypeColors.accent,
                    inactiveTrackColor = PrototypeColors.surfaceCard,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.prototype_journal_energy_low),
                    style = MaterialTheme.typography.labelSmall,
                    color = PrototypeColors.onSurfaceMuted,
                )
                Text(
                    text = energy.toInt().toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = PrototypeColors.onBackground,
                )
                Text(
                    text = stringResource(R.string.prototype_journal_energy_high),
                    style = MaterialTheme.typography.labelSmall,
                    color = PrototypeColors.onSurfaceMuted,
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        prototypeCard {
            Text(
                text = stringResource(R.string.prototype_journal_title_field_label),
                style = MaterialTheme.typography.titleSmall,
                color = PrototypeColors.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.prototype_journal_title_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrototypeColors.onBackground,
                    unfocusedTextColor = PrototypeColors.onBackground,
                    focusedBorderColor = PrototypeColors.accent,
                    unfocusedBorderColor = PrototypeColors.onSurfaceMuted,
                ),
                singleLine = true,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        prototypeCard {
            Text(
                text = stringResource(R.string.prototype_journal_body_label),
                style = MaterialTheme.typography.titleSmall,
                color = PrototypeColors.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = journal,
                onValueChange = { journal = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                placeholder = { Text(stringResource(R.string.prototype_journal_body_placeholder)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrototypeColors.onBackground,
                    unfocusedTextColor = PrototypeColors.onBackground,
                    focusedBorderColor = PrototypeColors.accent,
                    unfocusedBorderColor = PrototypeColors.onSurfaceMuted,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text("〰", color = PrototypeColors.onSurfaceMuted)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /* prototype stub */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrototypeColors.accent,
                contentColor = PrototypeColors.background,
            ),
        ) {
            Text(stringResource(R.string.prototype_journal_save_entry))
        }
    }
}

@Composable
private fun prototypeCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PrototypeColors.surface,
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}
