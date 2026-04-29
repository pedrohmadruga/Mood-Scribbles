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
import com.example.moodscribbles.ui.UnavailableUiScreen

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
                UnavailableUiScreen(
                    sectionName = stringResource(R.string.prototype_tab_home),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onOpenOfficialEntry = { navController.navigate(AppRoutes.FUNCTIONAL_JOURNAL) },
                )
            }
            MainTab.INSIGHTS.index -> {
                UnavailableUiScreen(
                    sectionName = stringResource(R.string.prototype_tab_insights),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onOpenOfficialEntry = { navController.navigate(AppRoutes.FUNCTIONAL_JOURNAL) },
                )
            }
            MainTab.SETTINGS.index -> {
                UnavailableUiScreen(
                    sectionName = stringResource(R.string.prototype_tab_settings),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onOpenOfficialEntry = { navController.navigate(AppRoutes.FUNCTIONAL_JOURNAL) },
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
