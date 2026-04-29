package com.example.moodscribbles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.moodscribbles.R

@Composable
fun UnavailableUiScreen(
    sectionName: String,
    modifier: Modifier = Modifier,
    onOpenOfficialEntry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.unavailable_ui_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.unavailable_ui_message, sectionName),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (onOpenOfficialEntry != null) {
            Button(
                onClick = onOpenOfficialEntry,
                modifier = Modifier.padding(top = 20.dp),
            ) {
                Text(text = stringResource(R.string.unavailable_ui_open_entry))
            }
        }
    }
}
