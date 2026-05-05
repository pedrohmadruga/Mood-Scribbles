package com.example.moodscribbles.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moodscribbles.R
import com.example.moodscribbles.ui.journal.JournalScreen
import com.example.moodscribbles.ui.prototype.AppRoutes
import com.example.moodscribbles.ui.prototype.MainTabsScreen
import com.example.moodscribbles.ui.theme.MoodScribblesTheme
import org.koin.androidx.compose.KoinAndroidContext
import java.time.LocalDate

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
                                UnavailableUiScreen(
                                    sectionName = stringResource(R.string.prototype_nav_mood_entry),
                                    modifier = Modifier.fillMaxSize(),
                                    onOpenOfficialEntry = {
                                        navController.navigate(AppRoutes.functionalJournalRoute())
                                    },
                                )
                            }
                            composable(AppRoutes.JOURNAL_STEP) {
                                UnavailableUiScreen(
                                    sectionName = stringResource(R.string.prototype_nav_journal_step),
                                    modifier = Modifier.fillMaxSize(),
                                    onOpenOfficialEntry = {
                                        navController.navigate(AppRoutes.functionalJournalRoute())
                                    },
                                )
                            }
                            composable(
                                route = AppRoutes.FUNCTIONAL_JOURNAL_PATTERN,
                                arguments = listOf(
                                    navArgument("date") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    },
                                ),
                            ) { backStackEntry ->
                                val initialDate = backStackEntry.arguments
                                    ?.getString("date")
                                    ?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() }
                                JournalScreen(
                                    onNavigateUp = { navController.popBackStack() },
                                    initialDate = initialDate,
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
