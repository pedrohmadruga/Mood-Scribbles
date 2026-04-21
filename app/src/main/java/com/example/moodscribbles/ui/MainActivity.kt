package com.example.moodscribbles.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moodscribbles.ui.journal.JournalScreen
import com.example.moodscribbles.ui.prototype.AppRoutes
import com.example.moodscribbles.ui.prototype.JournalStepPrototype
import com.example.moodscribbles.ui.prototype.MainTabsScreen
import com.example.moodscribbles.ui.prototype.MoodEntryPrototype
import com.example.moodscribbles.ui.theme.MoodScribblesTheme
import org.koin.androidx.compose.KoinAndroidContext

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
                            startDestination = AppRoutes.MAIN_TABS,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        ) {
                            composable(AppRoutes.MAIN_TABS) {
                                MainTabsScreen(
                                    navController = navController,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            composable(AppRoutes.MOOD_ENTRY) {
                                MoodEntryPrototype(
                                    onClose = { navController.popBackStack() },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            composable(AppRoutes.JOURNAL_STEP) {
                                JournalStepPrototype(
                                    onBack = { navController.popBackStack() },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            composable(AppRoutes.FUNCTIONAL_JOURNAL) {
                                JournalScreen(
                                    onNavigateUp = { navController.popBackStack() },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
