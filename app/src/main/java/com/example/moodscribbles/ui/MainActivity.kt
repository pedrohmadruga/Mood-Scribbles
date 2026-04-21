package com.example.moodscribbles.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodscribbles.R
import com.example.moodscribbles.ui.journal.JournalScreen
import com.example.moodscribbles.ui.theme.MoodScribblesTheme
import org.koin.androidx.compose.KoinAndroidContext

private object MainDestinations {
    const val HOME = "home"
    const val JOURNAL = "journal"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinAndroidContext {
                MoodScribblesTheme {
                    val navController = rememberNavController()
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = MainDestinations.HOME,
                            modifier = Modifier.padding(innerPadding),
                        ) {
                            composable(MainDestinations.HOME) {
                                HomeScreen(
                                    onOpenJournal = { navController.navigate(MainDestinations.JOURNAL) },
                                )
                            }
                            composable(MainDestinations.JOURNAL) {
                                JournalScreen(
                                    onNavigateUp = { navController.popBackStack() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = stringResource(id = R.string.home_subtitle),
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onOpenJournal) {
            Text(text = stringResource(id = R.string.home_create_first_entry))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MoodScribblesTheme {
        HomeScreen(onOpenJournal = {})
    }
}
