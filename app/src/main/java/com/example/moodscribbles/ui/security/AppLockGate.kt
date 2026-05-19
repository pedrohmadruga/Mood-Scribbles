package com.example.moodscribbles.ui.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moodscribbles.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppLockGate(
    activity: FragmentActivity,
    modifier: Modifier = Modifier,
    viewModel: AppLockViewModel = koinViewModel(),
    content: @Composable () -> Unit,
) {
    val isLockEnabled by viewModel.isLockEnabled.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val isPreferenceReady by viewModel.isPreferenceReady.collectAsStateWithLifecycle()

    val promptTitle = stringResource(R.string.biometric_prompt_title)
    val promptSubtitle = stringResource(R.string.biometric_prompt_subtitle)

    val shouldShowLock = isPreferenceReady && isLockEnabled && !isUnlocked

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isLockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && isLockEnabled) {
                viewModel.lockIfEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isLockEnabled, isUnlocked, isPreferenceReady) {
        if (shouldShowLock) {
            viewModel.requestUnlock(
                activity = activity,
                title = promptTitle,
                subtitle = promptSubtitle,
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !isPreferenceReady -> {
                Box(Modifier.fillMaxSize())
            }
            isLockEnabled && !isUnlocked -> {
                AppLockScreen(
                    onUnlock = {
                        viewModel.requestUnlock(
                            activity = activity,
                            title = promptTitle,
                            subtitle = promptSubtitle,
                        )
                    },
                )
            }
            else -> {
                content()
            }
        }
    }
}

@Composable
private fun AppLockScreen(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.app_lock_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.app_lock_message),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Button(onClick = onUnlock) {
                Text(text = stringResource(R.string.app_lock_unlock_button))
            }
        }
    }
}