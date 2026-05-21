package com.example.moodscribbles.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.BuildConfig
import com.example.moodscribbles.R
import com.example.moodscribbles.data.preferences.ThemeMode
import com.example.moodscribbles.notifications.MoodReminderPermissionHelper
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabScreen(
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val reminderSettings by viewModel.moodReminderSettings.collectAsStateWithLifecycle()
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var permissionDeniedMessage by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.applyReminderEnabledAfterPermissionGranted()
            permissionDeniedMessage = false
        } else {
            permissionDeniedMessage = true
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderSettings.hour,
            initialMinute = reminderSettings.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(text = stringResource(R.string.settings_reminder_time_picker_title)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setReminderTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text(text = stringResource(R.string.settings_reminder_time_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(text = stringResource(R.string.settings_reminder_time_picker_cancel))
                }
            },
        )
    }

    if (showAbout) {
        val appName = stringResource(R.string.app_name)
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(text = stringResource(R.string.settings_about_dialog_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.settings_about_dialog_body,
                        appName,
                        BuildConfig.VERSION_NAME,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text(text = stringResource(R.string.settings_about_ok))
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_screen_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            )
        }

        // --- Actions ---
        item {
            Text(
                text = stringResource(R.string.settings_section_actions),
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.settings_new_journal_entry))
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_new_journal_entry_summary),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenJournal),
                colors = ListItemDefaults.colors(
                    containerColor = colorScheme.surface,
                ),
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- Appearance ---
        item {
            Text(
                text = stringResource(R.string.settings_section_appearance),
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.settings_theme_headline))
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_theme_supporting),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = colorScheme.surface,
                ),
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(text = themeModeLabel(mode)) },
                    )
                }
            }
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- Notifications ---
        item {
            Text(
                text = stringResource(R.string.settings_notifications_title),
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.settings_reminder_enabled))
                },
                supportingContent = {
                    val supporting = when {
                        permissionDeniedMessage ->
                            stringResource(R.string.settings_reminder_permission_required)
                        else -> stringResource(R.string.settings_notifications_summary)
                    }
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = reminderSettings.enabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                permissionDeniedMessage = false
                                viewModel.setReminderEnabled(
                                    enabled = false,
                                    hasNotificationPermission = true,
                                )
                                return@Switch
                            }
                            val hasPermission = MoodReminderPermissionHelper
                                .hasNotificationPermission(context)
                            if (hasPermission) {
                                permissionDeniedMessage = false
                                viewModel.setReminderEnabled(
                                    enabled = true,
                                    hasNotificationPermission = true,
                                )
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS,
                                )
                            } else {
                                viewModel.setReminderEnabled(
                                    enabled = true,
                                    hasNotificationPermission = true,
                                )
                            }
                        },
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = colorScheme.surface,
                ),
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.settings_reminder_time))
                },
                supportingContent = {
                    Text(
                        text = stringResource(
                            R.string.settings_reminder_time_value,
                            reminderSettings.hour,
                            reminderSettings.minute,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = reminderSettings.enabled) {
                        showTimePicker = true
                    },
                colors = ListItemDefaults.colors(
                    containerColor = colorScheme.surface,
                ),
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- Privacy ---
        item {
            Text(
                text = stringResource(R.string.settings_section_privacy),
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.settings_biometric_title))
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_feature_coming_soon),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = colorScheme.surface,
                ),
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // --- About ---
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.settings_about_title))
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_about_summary),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAbout = true },
                colors = ListItemDefaults.colors(
                    containerColor = colorScheme.surface,
                ),
            )
        }
    }
}

@Composable
private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}
