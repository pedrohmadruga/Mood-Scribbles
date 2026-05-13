package com.example.moodscribbles.ui.prototype

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import com.example.moodscribbles.ui.calendar.MonthlyCalendarScreen
import com.example.moodscribbles.ui.insights.InsightsTabScreen
import java.time.LocalDate

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
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
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
                MonthlyCalendarScreen(
                    onOpenDayDetail = { date ->
                        navController.navigate(AppRoutes.calendarDayDetailRoute(date))
                    },
                    onOpenJournalForDate = { date ->
                        navController.navigate(AppRoutes.functionalJournalRoute(date))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
            MainTab.INSIGHTS.index -> {
                InsightsTabScreen(
                    onOpenDayDetail = { date ->
                        navController.navigate(AppRoutes.calendarDayDetailRoute(date))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
            MainTab.SETTINGS.index -> {
                UnavailableUiScreen(
                    sectionName = stringResource(R.string.prototype_tab_settings),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onOpenOfficialEntry = { navController.navigate(AppRoutes.functionalJournalRoute()) },
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
    const val FUNCTIONAL_JOURNAL_PATTERN = "functional_journal?date={date}"

    fun functionalJournalRoute(date: LocalDate? = null): String {
        return if (date == null) FUNCTIONAL_JOURNAL else "$FUNCTIONAL_JOURNAL?date=$date"
    }

    const val CALENDAR_DAY_DETAIL = "calendar_day_detail"
    const val CALENDAR_DAY_DETAIL_PATTERN = "$CALENDAR_DAY_DETAIL/{date}"

    fun calendarDayDetailRoute(date: LocalDate): String = "$CALENDAR_DAY_DETAIL/${date}"
}
