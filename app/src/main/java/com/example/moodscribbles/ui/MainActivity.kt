package com.example.moodscribbles.ui

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moodscribbles.R
import com.example.moodscribbles.data.preferences.ThemePreferenceRepository
import com.example.moodscribbles.notifications.MoodReminderIntentExtras
import com.example.moodscribbles.ui.calendar.CalendarDayDetailScreen
import com.example.moodscribbles.ui.journal.JournalScreen
import com.example.moodscribbles.ui.prototype.AppRoutes
import com.example.moodscribbles.ui.prototype.MainTabsScreen
import com.example.moodscribbles.ui.security.AppLockGate
import com.example.moodscribbles.ui.theme.MoodScribblesAppTheme
import org.koin.android.ext.android.getKoin
import org.koin.androidx.compose.KoinAndroidContext
import java.time.LocalDate

class MainActivity : FragmentActivity() {

    private val pendingNavRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        pendingNavRoute.value = extractNavRoute(intent)

        val themePreferenceRepository = getKoin().get<ThemePreferenceRepository>()

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                KoinAndroidContext {
                    MoodScribblesAppTheme(themePreferenceRepository = themePreferenceRepository) {
                        AppLockGate(activity = this@MainActivity) {
                            val navController = rememberNavController()
                            val routeToHandle = pendingNavRoute.value
                            LaunchedEffect(routeToHandle) {
                                val route = routeToHandle ?: return@LaunchedEffect
                                if (route == AppRoutes.FUNCTIONAL_JOURNAL) {
                                    navController.navigate(AppRoutes.functionalJournalRoute()) {
                                        popUpTo(AppRoutes.MAIN_TABS) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                pendingNavRoute.value = null
                            }
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
                                    composable(
                                        route = AppRoutes.CALENDAR_DAY_DETAIL_PATTERN,
                                        arguments = listOf(
                                            navArgument("date") { type = NavType.StringType },
                                        ),
                                    ) { backStackEntry ->
                                        val parsed = backStackEntry.arguments
                                            ?.getString("date")
                                            ?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() }
                                        if (parsed == null) {
                                            LaunchedEffect(Unit) {
                                                navController.popBackStack()
                                            }
                                            Box(modifier = Modifier.fillMaxSize())
                                        } else {
                                            CalendarDayDetailScreen(
                                                date = parsed,
                                                onNavigateUp = { navController.popBackStack() },
                                                onOpenJournalForDate = { date ->
                                                    navController.navigate(
                                                        AppRoutes.functionalJournalRoute(date),
                                                    )
                                                },
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
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

        setContentView(composeView)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNavRoute.value = extractNavRoute(intent)
    }

    private fun extractNavRoute(intent: Intent?): String? {
        return intent?.getStringExtra(MoodReminderIntentExtras.EXTRA_NAV_ROUTE)
    }
}
