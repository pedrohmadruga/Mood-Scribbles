package com.example.moodscribbles.ui.prototype

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.moodscribbles.R

private enum class MainTab(val index: Int) {
    HOME(0),
    INSIGHTS(1),
    SETTINGS(2),
}

/**
 * Bottom navigation shell: Calendar home, Insights, Settings. Full-screen routes use [navController].
 */
@Composable
fun MainTabsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(MainTab.HOME.index) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(containerColor = PrototypeColors.surface) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.HOME.index,
                    onClick = { selectedTab = MainTab.HOME.index },
                    icon = { Text("⌂") },
                    label = { Text(stringResource(R.string.prototype_tab_home)) },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.INSIGHTS.index,
                    onClick = { selectedTab = MainTab.INSIGHTS.index },
                    icon = { Text("📈") },
                    label = { Text(stringResource(R.string.prototype_tab_insights)) },
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.SETTINGS.index,
                    onClick = { selectedTab = MainTab.SETTINGS.index },
                    icon = { Text("⚙") },
                    label = { Text(stringResource(R.string.prototype_tab_settings)) },
                )
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.HOME.index -> {
                CalendarHomePrototype(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onOpenMoodEntry = { navController.navigate(AppRoutes.MOOD_ENTRY) },
                    onOpenJournalStep = { navController.navigate(AppRoutes.JOURNAL_STEP) },
                    onOpenFunctionalJournal = { navController.navigate(AppRoutes.FUNCTIONAL_JOURNAL) },
                )
            }
            MainTab.INSIGHTS.index -> {
                InsightsPrototype(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
            MainTab.SETTINGS.index -> {
                SettingsTabPrototype(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onOpenFunctionalJournal = { navController.navigate(AppRoutes.FUNCTIONAL_JOURNAL) },
                    onOpenMoodEntry = { navController.navigate(AppRoutes.MOOD_ENTRY) },
                    onOpenJournalStep = { navController.navigate(AppRoutes.JOURNAL_STEP) },
                )
            }
        }
    }
}

object AppRoutes {
    const val MAIN_TABS = "main_tabs"
    const val MOOD_ENTRY = "mood_entry"
    const val JOURNAL_STEP = "journal_step"
    const val FUNCTIONAL_JOURNAL = "functional_journal"
}
